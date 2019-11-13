package zkstrata.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOGGER = LogManager.getLogger(SemanticAnalyzer.class);

    private final Set<Method> implicationRules;
    private final Set<Method> contradictionRules;

    private Set<Inference> inferences;
    private Map<WitnessVariable, Set<Inference>> inferenceMapping;

    public SemanticAnalyzer() {
        this.implicationRules = ReflectionHelper.getMethodsAnnotatedWith(Implication.class);
        this.contradictionRules = ReflectionHelper.getMethodsAnnotatedWith(Contradiction.class);
    }

    // TODO: Herleitungs-History wäre interessant (Implication Object) --> vor allem in Fehlermeldung

    public void process(Statement statement) {
        List<Gadget> gadgets = new ArrayList<>(statement.getGadgets());
        statement.getPremises().forEach(premise -> gadgets.addAll(premise.getGadgets()));

        LOGGER.debug("Starting semantic analysis on {} gadgets", gadgets.size());

        this.inferences = new HashSet<>();
        this.inferenceMapping = new HashMap<>();

        // find inferences by executing methods annotated with @Implication
        findImplications(gadgets);

        // check for semantic errors by executing methods annotated with @Contradiction
        checkContradictions();

        statement.setInferences(inferences);

        LOGGER.debug("Finishing analysis: Found a total of {} inferences", inferences.size());
    }

    private void checkContradictions() {
        for (Method contradictionRule : contradictionRules) {
            Class<? extends Gadget>[] context = contradictionRule.getAnnotation(Contradiction.class).propositions();

            List<List<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(
                    List.of(context),
                    inferences.stream().map(Inference::getConclusion).collect(Collectors.toList())
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

    private void findImplications(List<Gadget> gadgets) {
        // represent gadgets assembled from statements as inferences of themselves
        Map<WitnessVariable, Set<Inference>> newInferences = processInferences(gadgets.stream()
                .map(g -> new Inference(Set.of(g), g, Collections.emptySet()))
                .collect(Collectors.toSet()));

        while (!newInferences.isEmpty()) {
            Set<Inference> round = new HashSet<>();

            for (Map.Entry<WitnessVariable, Set<Inference>> newInference : newInferences.entrySet())
                for (Inference assumption : newInference.getValue())
                    round.addAll(runImplicationRules(assumption, inferenceMapping.getOrDefault(newInference.getKey(), Collections.emptySet())));

            newInferences = processInferences(simplify(round));
        }
    }

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

    private Set<Inference> simplify(Set<Inference> inferences) {
        return inferences.stream()
                .filter(inference -> Stream.concat(inferences.stream().filter(i -> !inference.equals(i)), this.inferences.stream())
                        .noneMatch(inference::canBeImpliedFrom))
                .collect(Collectors.toSet());
    }

    private Set<Inference> runImplicationRules(Inference assumption, Set<Inference> assumptions) {
        Map<Gadget, Inference> assumptionMapping = assumptions.stream().collect(Collectors.toMap(Inference::getConclusion, i -> i));
        Class<? extends Gadget> type = assumption.getConclusion().getClass();

        Set<Inference> newInferences = new HashSet<>();
        for (Method implicationRule : implicationRules) {
            List<Class<? extends Gadget>> context = new ArrayList<>(List.of(implicationRule.getAnnotation(Implication.class).assumption()));
            if (context.contains(type)) {
                int index = context.indexOf(type);
                context.remove(index);

                List<List<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(context, assumptionMapping.keySet());
                for (List<Gadget> contextCombination : contextCombinations) {
                    contextCombination.add(index, assumption.getConclusion());
                    invokeImplication(implicationRule, contextCombination.toArray())
                            .ifPresent(g -> newInferences.add(
                                    Inference.from(contextCombination.stream()
                                            .map(assumptionMapping::get)
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toSet()), g)
                            ));
                }
            }
        }
        return newInferences;
    }

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
