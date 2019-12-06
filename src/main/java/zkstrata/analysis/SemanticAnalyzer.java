package zkstrata.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.domain.Proposition;
import zkstrata.domain.Statement;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.utils.CombinatoricsUtils;
import zkstrata.utils.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SemanticAnalyzer {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private final Set<Method> implicationRules;
    private final Set<Method> contradictionRules;

    private Set<Inference> inferences;
    private Map<WitnessVariable, Set<Inference>> inferenceMapping;

    public SemanticAnalyzer() {
        this.implicationRules = ReflectionHelper.getMethodsAnnotatedWith(Implication.class);
        this.contradictionRules = ReflectionHelper.getMethodsAnnotatedWith(Contradiction.class);
    }

    public void process(Statement statement) {
        LOGGER.debug("Starting semantic analysis");

        // combine the given statement with all provided premises
        Proposition rootConstituent = statement.getClaim();
        for (Statement premise : statement.getPremises()) {
            rootConstituent = rootConstituent.combine(premise.getClaim());
        }

        List<List<Gadget>> evaluationPaths = rootConstituent.getEvaluationPaths();

        LOGGER.debug("Found {} logically distinct paths to evaluate the given statement", evaluationPaths.size());

        for (List<Gadget> evaluationPath : evaluationPaths) {
            this.inferences = new HashSet<>();
            this.inferenceMapping = new HashMap<>();

            // find inferences by executing methods annotated with @Implication
            findImplications(evaluationPath);

            LOGGER.debug("Found {} inferences for this evaluation path", inferences.size());

            // check for semantic errors by executing methods annotated with @Contradiction
            checkContradictions();
        }

        LOGGER.debug("Finishing semantic analysis");
    }

    /**
     * Executes all methods annotated as {@link Contradiction} on gadget combinations formed from {@link SemanticAnalyzer#inferences}.
     */
    private void checkContradictions() {
        for (Method contradictionRule : contradictionRules) {
            Class<? extends Gadget>[] context = contradictionRule.getAnnotation(Contradiction.class).propositions();

            Set<List<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(
                    List.of(context),
                    inferences.stream().map(Inference::getConclusion).collect(Collectors.toSet())
            );

            for (List<Gadget> contextCombination : contextCombinations) {
                try {
                    contradictionRule.invoke(null, contextCombination.toArray());
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof CompileTimeException)
                        throw (CompileTimeException) cause;
                    else
                        throw new InternalCompilerException(cause, "Invalid exception %s thrown by %s in %s.",
                                cause.getClass().getSimpleName(), contradictionRule.getName(), contradictionRule.getDeclaringClass());
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    throw new InternalCompilerException(e, "Invalid implementation of @Contradiction annotated method %s in %s: "
                            + "Ensure the method is static and its parameters are matching the annotation.",
                            contradictionRule.getName(), contradictionRule.getDeclaringClass());
                }
            }
        }
    }

    /**
     * Executes all methods annotated as {@link Implication} on the gadgets in the given list {@code gadgets} and all
     * inferences that can be drawn from it, until there is no new realization.
     * <p>
     * To reduce the implication rules that will be run, the {@link SemanticAnalyzer#inferenceMapping}, a mapping of
     * witness variables to the inferences they occur in, is used. This way only inferences containing common witnesses
     * (thus, related inferences) are being used to run implication rules.
     * <p>
     * Any gadget that can be inferred will be stored in {@link SemanticAnalyzer#inferences} and the mapping
     * {@link SemanticAnalyzer#inferenceMapping} which maps {@link WitnessVariable} to all gadgets in which they occur.
     *
     * @param gadgets list of gadgets to search inferences for
     */
    private void findImplications(List<Gadget> gadgets) {
        Map<WitnessVariable, Set<Inference>> newInferences = drawSelfInferences(gadgets);

        while (!newInferences.isEmpty()) {
            Set<Inference> round = new HashSet<>();

            for (Map.Entry<WitnessVariable, Set<Inference>> newInference : newInferences.entrySet())
                for (Inference assumption : newInference.getValue())
                    round.addAll(runImplicationRules(assumption, inferenceMapping.getOrDefault(newInference.getKey(), Collections.emptySet())));

            newInferences = processInferences(simplify(round));
        }
    }

    /**
     * Transforms the given list of {@link Gadget} into a list of {@link Inference}, where each inference is drawn from
     * the gadget itself.
     *
     * @param gadgets list of gadgets
     * @return list of inferences drawn from the gadgets with no additional assumption
     */
    private Map<WitnessVariable, Set<Inference>> drawSelfInferences(List<Gadget> gadgets) {
        return processInferences(gadgets.stream()
                .map(g -> new Inference(Set.of(g), g, Collections.emptySet()))
                .collect(Collectors.toSet()));
    }

    /**
     * Transforms the given {@link Set} of {@link Inference} into a mapping of {@link WitnessVariable} to all
     * {@link Inference} in which they occur.
     *
     * @param inferences set of inferences
     * @return mapping of witness variables to the inferences they are part of
     */
    private Map<WitnessVariable, Set<Inference>> processInferences(Set<Inference> inferences) {
        this.inferences.addAll(inferences);

        Map<WitnessVariable, Set<Inference>> delta = new HashMap<>();
        for (Inference inference : inferences)
            for (Object var : inference.getConclusion().getVariables())
                if (var instanceof WitnessVariable) {
                    this.inferenceMapping.computeIfAbsent((WitnessVariable) var, s -> new HashSet<>()).add(inference);
                    delta.computeIfAbsent((WitnessVariable) var, s -> new HashSet<>()).add(inference);
                }

        return delta;
    }

    /**
     * Filters the given set of {@link Inference} for such that have been implied before using less or the same assumptions.
     * <p>
     * This method is used to prevent recursive implications by staying with the most basic inference (least assumptions).
     *
     * @param inferences set of inferences to simplify
     * @return the given set of inferences without elements that have been implied before
     */
    private Set<Inference> simplify(Set<Inference> inferences) {
        return inferences.stream()
                .filter(curr -> Stream.concat(inferences.stream().filter(i -> !curr.equals(i)), this.inferences.stream())
                        .noneMatch(curr::canBeImpliedFrom))
                .collect(Collectors.toSet());
    }

    /**
     * Invokes all methods annotated as {@link Implication} for the given {@code assumption} using the given set of
     * {@link Inference} of related inferences as basic assumptions.
     *
     * @param assumption  {@link Inference} to include into implication rule invocations
     * @param assumptions set of {@link Inference} to complete implication rule invocations with
     * @return set of newly drawn {@link Inference}
     */
    private Set<Inference> runImplicationRules(Inference assumption, Set<Inference> assumptions) {
        Map<Gadget, List<Inference>> assumptionMapping = new HashMap<>();
        assumptions.forEach(inference -> assumptionMapping.computeIfAbsent(inference.getConclusion(), s -> new ArrayList<>()).add(inference));

        Class<? extends Gadget> type = assumption.getConclusion().getClass();

        Set<Inference> newInferences = new HashSet<>();
        for (Method implicationRule : implicationRules) {
            List<Class<? extends Gadget>> context = new ArrayList<>(List.of(implicationRule.getAnnotation(Implication.class).assumption()));
            if (context.contains(type)) {
                int index = context.indexOf(type);
                context.remove(index);

                Set<List<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(context, assumptionMapping.keySet());
                for (List<Gadget> contextCombination : contextCombinations) {
                    contextCombination.add(index, assumption.getConclusion());
                    invokeImplication(implicationRule, contextCombination.toArray())
                            .ifPresent(g ->
                                    CombinatoricsUtils.computeCartesianProduct(contextCombination.stream()
                                            .map(assumptionMapping::get)
                                            .collect(Collectors.toList()))
                                            .forEach(list -> newInferences.add(
                                                    Inference.from(new HashSet<>(list), g)
                                            ))
                            );
                }
            }
        }
        return newInferences;
    }

    /**
     * Invokes the given {@link Method} using the provided {@code args}.
     *
     * @param implication {@link Method} to invoke
     * @param args        arguments to pass to the method
     * @return {@link Optional} {@link Gadget} returned by the invoked method
     */
    private Optional<Gadget> invokeImplication(Method implication, Object[] args) {
        if (!ReflectionHelper.checkReturnType(implication, Optional.class, Gadget.class))
            throw new InternalCompilerException("Invalid implementation of @Implication annotated method %s in %s. "
                    + "Return type must be Optional<Gadget>.", implication.getName(), implication.getDeclaringClass());

        try {
            @SuppressWarnings("unchecked")
            Optional<Gadget> inference = (Optional<Gadget>) implication.invoke(null, args);

            return inference;
        } catch (ReflectiveOperationException | IllegalArgumentException | ClassCastException e) {
            throw new InternalCompilerException(e, "Invalid implementation of @Implication annotated method %s in %s: "
                    + "Ensure the method is static and its parameters are matching the annotation.",
                    implication.getName(), implication.getDeclaringClass());
        }
    }
}
