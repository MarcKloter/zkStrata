package zkstrata.utils;

import zkstrata.analysis.Implication;
import zkstrata.analysis.Inference;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.InternalCompilerException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImplicationHelper {
    private static final Set<Method> IMPLICATION_RULES = ReflectionHelper.getMethodsAnnotatedWith(Implication.class);

    private ImplicationHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Executes all methods annotated as {@link Implication} on the given list of {@link Gadget} objects {@code gadgets}
     * and the inferences that can be drawn from it, until there is no new implication to be drawn.
     *
     * @param targets list of {@link Gadget} objects to draw inferences for
     * @return set of {@link Inference} returned by the executed implication rules
     */
    public static Set<Inference> drawInferences(List<Gadget> targets) {
        Set<Inference> inferences = drawSelfInferences(targets);

        return drawAllInferences(inferences, inferences);
    }

    /**
     * Executes all methods annotated as {@link Implication} on the given list of {@link Gadget} objects {@code gadgets}
     * and the inferences that can be drawn from it, until there is no new implication to be drawn.
     *
     * @param targets list of {@link Gadget} objects to draw inferences for
     * @param existing set of {@link Inference} that have been drawn previously
     * @return set of {@link Inference} returned by the executed implication rules
     */
    public static Set<Inference> drawInferences(List<Gadget> targets, Set<Inference> existing) {
        Set<Inference> inferences = drawSelfInferences(targets);

        return drawAllInferences(inferences, Stream.concat(inferences.stream(), existing.stream()).collect(Collectors.toSet()));
    }

    /**
     * Executes all methods annotated as {@link Implication} on the {@link Gadget} objects in the given list
     * {@code gadgets} and the inferences that can be drawn from it, until there is no new implication to be drawn.
     * <p>
     * To reduce the implication rules that will be run, a mapping of witness variables to the inferences they occur in
     * is used. This way only inferences containing common witnesses (thus, related inferences) are being used to run
     * implication rules.
     *
     * @param targets {@link Inference} objects to use as targets in implication rule invocations
     * @param context {@link Inference} objects to use as context in implication rule invocations
     * @return set of {@link Inference} returned by the executed implication rules
     */
    private static Set<Inference> drawAllInferences(Set<Inference> targets, Set<Inference> context) {
        Set<Inference> allInferences = new HashSet<>(context);
        Map<WitnessVariable, Set<Inference>> targetMapping = createWitnessToInferenceMap(targets);
        Map<WitnessVariable, Set<Inference>> contextMapping = createWitnessToInferenceMap(allInferences);
        while (!targetMapping.isEmpty()) {
            Set<Inference> newInferences = simplify(drawDirectInferences(targetMapping, contextMapping), allInferences);
            targetMapping = createWitnessToInferenceMap(newInferences);
            targetMapping.forEach((var, inf) -> contextMapping.computeIfAbsent(var, s -> new HashSet<>()).addAll(inf));
            allInferences.addAll(newInferences);
        }
        return allInferences;
    }

    /**
     * Triggers {@link ImplicationHelper#runAllImplicationRules(Set, Set)} for all common inference sets in the provided
     * {@code targetMapping}. This will lead to the implication of all direct inferences (implications that can be drawn
     * from the current state).
     *
     * @param targetMapping  inferences to use as targets in implication rule invocations
     * @param contextMapping inferences to use as context in implication rule invocations
     * @return set of inferences returned by the executed implication rules
     */
    private static Set<Inference> drawDirectInferences(
            Map<WitnessVariable, Set<Inference>> targetMapping,
            Map<WitnessVariable, Set<Inference>> contextMapping
    ) {
        Set<Inference> inferences = new HashSet<>();

        for (Map.Entry<WitnessVariable, Set<Inference>> commonInferences : targetMapping.entrySet()) {
            Set<Inference> targets = commonInferences.getValue();
            // limit context to all gadgets that use the same witness variable
            Set<Inference> context = contextMapping.getOrDefault(commonInferences.getKey(), Collections.emptySet());
            inferences.addAll(runAllImplicationRules(targets, context));
        }

        return inferences;
    }

    /**
     * Triggers the execution of all methods annotated as {@link Implication} for the inferences in {@code targets}
     * using the provided context objects {@code context}.
     *
     * @param targets objects to run implication rules for
     * @param context objects to use as context in implication rule invocations
     * @return set of inferences returned by the executed implication rules
     */
    private static Set<Inference> runAllImplicationRules(Set<Inference> targets, Set<Inference> context) {
        Set<Inference> inferences = new HashSet<>();
        for (Inference target : targets) {
            inferences.addAll(runImplicationRules(target, context));
        }
        return inferences;
    }

    /**
     * Transforms the given list of {@link Gadget} into a set of {@link Inference}, where each inference is drawn from
     * the gadget itself.
     *
     * @param gadgets list of gadgets
     * @return set of inferences drawn from the gadgets with no additional assumption
     */
    private static Set<Inference> drawSelfInferences(List<Gadget> gadgets) {
        return gadgets.stream()
                .map(g -> new Inference(Set.of(g), g, Collections.emptySet()))
                .collect(Collectors.toSet());
    }

    /**
     * Transforms the given set of {@link Inference} into a mapping of {@link WitnessVariable} to all
     * {@link Inference} objects they occur in.
     *
     * @param inferences set of inferences
     * @return mapping of witness variables to the inferences they are part of
     */
    private static Map<WitnessVariable, Set<Inference>> createWitnessToInferenceMap(Set<Inference> inferences) {
        Map<WitnessVariable, Set<Inference>> inferenceMapping = new HashMap<>();
        for (Inference inference : inferences)
            for (Variable var : inference.getConclusion().getVariables())
                if (var instanceof WitnessVariable)
                    inferenceMapping.computeIfAbsent((WitnessVariable) var, s -> new HashSet<>()).add(inference);

        return inferenceMapping;
    }

    /**
     * Filters the given set of {@link Inference} for such that have been implied before using less or the same assumptions.
     * <p>
     * This method is used to prevent recursive implications by staying with the most basic inference (least assumptions).
     *
     * @param newInferences      set of inferences to simplify
     * @param existingInferences set of inferences that have been implied before
     * @return the given set of inferences without elements that have been implied before
     */
    private static Set<Inference> simplify(Set<Inference> newInferences, Set<Inference> existingInferences) {
        return newInferences.stream()
                .filter(curr -> Stream.concat(newInferences.stream().filter(i -> !curr.equals(i)), existingInferences.stream())
                        .noneMatch(curr::canBeImpliedFrom))
                .collect(Collectors.toSet());
    }

    /**
     * Invokes all provided methods annotated as {@link Implication} for the given {@code assumption} using the given
     * set of {@link Inference} of related inferences as basic assumptions.
     *
     * @param target  {@link Inference} to include into implication rule invocations
     * @param context set of {@link Inference} to complete implication rule invocations with
     * @return set of newly drawn {@link Inference}
     */
    private static Set<Inference> runImplicationRules(Inference target, Set<Inference> context) {
        // create mapping to store gadgets to the inferences was drawn from
        Map<Gadget, List<Inference>> inferenceMapping = new HashMap<>();
        // initially fill map with given inferences (a gadget could already be drawn from different inferences)
        for (Inference inference : context)
            inferenceMapping.computeIfAbsent(inference.getConclusion(), s -> new ArrayList<>()).add(inference);

        Set<Inference> newInferences = new HashSet<>();
        for (Method method : IMPLICATION_RULES) {
            Set<List<Gadget>> setOfArguments = prepareArguments(method, target.getConclusion(), inferenceMapping.keySet());
            for (List<Gadget> arguments : setOfArguments) {
                Optional<Gadget> impliedGadget = invokeImplicationRule(method, arguments.toArray());
                if (impliedGadget.isPresent()) {
                    List<Inference> inferences = createInferences(impliedGadget.get(), arguments, inferenceMapping);
                    newInferences.addAll(inferences);
                }
            }
        }
        return newInferences;
    }

    /**
     * Takes an implication rule ({@code method}) and a {@link Gadget} to call the implication rule for.
     * If the provided method takes the gadget as argument and requires more parameters, use the {@code context} to
     * satisfy the method signature list.
     *
     * @param method  {@link Method} annotated as {@link Implication} to prepare arguments for
     * @param gadget  gadget to draw implications for
     * @param context set of {@link Gadget} as context for {@code gadget} to satisfy the given method with
     * @return set of arguments satisfying the given {@code implicationRule} or empty set
     */
    private static Set<List<Gadget>> prepareArguments(Method method, Gadget gadget, Set<Gadget> context) {
        Class<? extends Gadget> type = gadget.getClass();
        List<Class<? extends Gadget>> parameterTypes = new ArrayList<>(List.of(method.getAnnotation(Implication.class).assumption()));
        if (parameterTypes.contains(type)) {
            int index = parameterTypes.indexOf(type);
            parameterTypes.remove(index);

            Set<List<Gadget>> contextCombinations = CombinatoricsUtils.getCombinations(parameterTypes, context);
            for (List<Gadget> contextCombination : contextCombinations)
                contextCombination.add(index, gadget);

            return contextCombinations;
        }

        return Collections.emptySet();
    }

    /**
     * Create {@link Inference} objects based on all possible assumptions that could have lead to them.
     *
     * @param impliedGadget    gadget that was implied
     * @param arguments        list of {@link Gadget} that were used to imply the {@code gadget}
     * @param inferenceMapping mapping of gadgets to inferences they were based on, used to lookup the {@code arguments}
     * @return list of new {@link Inference} objects, where the conclusion is {@code impliedGadget} and assumptions
     * combinations of inferences that lead to the {@code arguments}
     */
    private static List<Inference> createInferences(
            Gadget impliedGadget,
            List<Gadget> arguments,
            Map<Gadget, List<Inference>> inferenceMapping
    ) {
        // the gadgets used as arguments could have been drawn from different inferences
        List<List<Inference>> impliedUsing = arguments.stream()
                .map(inferenceMapping::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // create cartesian product of all possible inferences the gadgets could originate from
        return CombinatoricsUtils.computeCartesianProduct(impliedUsing).stream()
                .map(list -> Inference.from(new HashSet<>(list), impliedGadget))
                .collect(Collectors.toList());
    }

    /**
     * Invokes the given {@link Method} using the provided {@code args}.
     * The method is expected to be a {@link Implication} annotated method that returns an {@link Optional} of
     * {@link Gadget} which can be implied from the given {@code args}.
     *
     * @param implicationRule {@link Method} to invoke
     * @param args            arguments to pass to the method
     * @return {@link Optional} {@link Gadget} returned by the invoked method
     */
    private static Optional<Gadget> invokeImplicationRule(Method implicationRule, Object[] args) {
        if (!ReflectionHelper.checkReturnType(implicationRule, Optional.class, Gadget.class))
            throw new InternalCompilerException("Invalid implementation of @Implication annotated method %s in %s. "
                    + "Return type must be Optional<Gadget>.", implicationRule.getName(), implicationRule.getDeclaringClass());

        try {
            @SuppressWarnings("unchecked")
            Optional<Gadget> impliedGadget = (Optional<Gadget>) implicationRule.invoke(null, args);

            return impliedGadget;
        } catch (ReflectiveOperationException | IllegalArgumentException | ClassCastException e) {
            throw new InternalCompilerException(e, "Invalid implementation of @Implication annotated method %s in %s: "
                    + "Ensure the method is static and its parameters are matching the annotation.",
                    implicationRule.getName(), implicationRule.getDeclaringClass());
        }
    }
}
