package zkstrata.optimizer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Inference;
import zkstrata.domain.Proposition;
import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.conjunctions.Conjunction;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.Position;
import zkstrata.utils.CombinatoricsUtils;
import zkstrata.utils.ImplicationHelper;
import zkstrata.utils.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static zkstrata.utils.CombinatoricsUtils.getCombinations;

public class Optimizer {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final Set<Class<? extends Gadget>> GADGET_TYPES = ReflectionHelper.getAllGadgets();
    private static final Set<Class<? extends Conjunction>> CONJUNCTION_TYPES = ReflectionHelper.getAllConjunctions();
    private static final List<SubstitutionRule> SUBSTITUTION_RULES = prepareSubstitutionRules();

    private Optimizer() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Applies methods annotated as {@link Substitution} on the given {@code claim} using the provided {@code premises}
     * as assumptions to remove implications with.
     *
     * @param claim    {@link Proposition} to optimize
     * @param premises {@link Proposition} of premises that were proven before
     * @return a semantically equal {@link Proposition} to the union of {@code claim} and {@code premises}, which has
     * the same or less {@link Proposition#getCostEstimate()}.
     */
    public static Proposition process(Proposition claim, Proposition premises) {
        LOGGER.debug("Starting optimization");

        Set<Inference> baseAssumptions = determineBaseAssumptions(premises);
        Proposition optimizedClaim = dispatch(claim, baseAssumptions, baseAssumptions);

        LOGGER.debug("Finishing optimization");

        return optimizedClaim;
    }

    /**
     * Determines all {@link Gadget} that are always true in the given {@code premises}.
     * <p>
     * This is done by taking the intersection of the {@link Proposition#getEvaluationPaths()} (all distinct gadget
     * combinations that can be shown to evaluate to true to prove a statement), which is the set of {@link Gadget}
     * that must (at least) always be true. All inferences that can be drawn from this set will be returned as the
     * base assumption.
     *
     * @param premises {@link Proposition} to derive the base assumptions from
     * @return set of {@link Inference} that can be assumed as proven
     */
    private static Set<Inference> determineBaseAssumptions(Proposition premises) {
        List<List<Gadget>> evaluationPaths = premises.getEvaluationPaths();
        Set<Gadget> commonGadgets = CombinatoricsUtils.computeIntersection(evaluationPaths);

        return ImplicationHelper.drawInferences(new ArrayList<>(commonGadgets));
    }

    /**
     * Dispatches the given {@link Proposition} depending on its type.
     *
     * @param proposition        {@link Proposition} to dispatch
     * @param baseAssumptions    set of {@link Inference} which are true for {@link Gadget}
     * @param contextAssumptions set of {@link Inference} which are true for {@link Conjunction}
     * @return a semantically identical {@link Proposition} to the given {@code proposition} with equal or less cost
     */
    private static Proposition dispatch(Proposition proposition, Set<Inference> baseAssumptions, Set<Inference> contextAssumptions) {
        if (proposition instanceof Conjunction)
            return processConjunction((Conjunction) proposition, contextAssumptions);

        if (proposition instanceof Gadget)
            return runSubstitutionRules(proposition, baseAssumptions);

        throw new InternalCompilerException("Unknown proposition %s found.", proposition.getClass());
    }

    /**
     * Optimizes the given {@link Conjunction} by running all applicable substitution rules.
     *
     * @param conjunction     {@link Conjunction} to optimize
     * @param baseAssumptions set of {@link Inference} that are assumed to be true within this conjunction
     * @return a semantically identical {@link Proposition} to the given {@code conjunction} with equal or less cost
     */
    private static Proposition processConjunction(Conjunction conjunction, Set<Inference> baseAssumptions) {
        Set<Inference> contextAssumptions = determineConjunctionAssumptions(conjunction, baseAssumptions);

        List<Proposition> parts = new ArrayList<>();
        // loop through logically cohesive combinations of propositions
        List<List<Proposition>> cohesivePropositions = conjunction.getCohesivePropositions();
        for (List<Proposition> group : cohesivePropositions) {
            // 1) run substitution rules on children of logical group in isolation
            List<Proposition> processedGroup = group.stream()
                    .map(p -> dispatch(p, baseAssumptions, contextAssumptions))
                    .map(p -> collapse(p, conjunction.getClass()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            // 2) run substitution rules on whole logical group
            parts.addAll(runSubstitutionRules(processedGroup, baseAssumptions));
        }

        // 3) run substitution rules on conjunction itself
        return runSubstitutionRules(Conjunction.createInstanceOf(conjunction.getClass(), parts), baseAssumptions);
    }

    /**
     * Determines all inferences that can be drawn for the children of the provided {@link Conjunction}.
     *
     * @param conjunction {@link Conjunction} to check
     * @param assumptions set of {@link Inference} of inferences that are already assumed for this conjunction
     * @return set of {@link Inference} that can be assumed for the children of the provided {@code conjunction}
     */
    private static Set<Inference> determineConjunctionAssumptions(Conjunction conjunction, Set<Inference> assumptions) {
        if (conjunction instanceof AndConjunction) {
            List<Gadget> targets = conjunction.getParts().stream()
                    .map(proposition -> (proposition instanceof Gadget) ? (Gadget) proposition : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return ImplicationHelper.drawInferences(targets, assumptions);
        }

        return assumptions;
    }

    /**
     * Combines encapsulated conjunctions of the same type into one.
     * <p>
     * This is used to remove to collapse structures such as AND(AND(A, B), AND(C), D) to AND(A, B, C, D).
     * Such structures can for example be formed through the lift up of common children in conjunction optimization.
     *
     * @param proposition     {@link Proposition} to check
     * @param conjunctionType type of the parent conjunction
     * @return list of {@link Proposition} containing the collapsed representation of the given {@code proposition}
     */
    private static List<Proposition> collapse(Proposition proposition, Class<? extends Conjunction> conjunctionType) {
        if (proposition instanceof Conjunction && conjunctionType.isInstance(proposition)) {
            return ((Conjunction) proposition).getParts();
        } else
            return List.of(proposition);
    }

    /**
     * Applies substitutions to the given {@code target} until there is nothing to further replace.
     *
     * @param target  {@link Proposition} to optimize
     * @param context set of {@link Inference} of assumptions as the context of the target
     * @return a semantically identical {@link Proposition} to the given {@code target} with equal or less cost
     */
    private static Proposition runSubstitutionRules(Proposition target, Set<Inference> context) {
        Proposition state = target;
        Optional<Substitute> improvement;
        do {
            improvement = pickSubstitute(List.of(state), context, false);
            if (improvement.isPresent()) {
                Substitute substitute = improvement.get();
                LOGGER.debug("Applying substitution `{}` (cost reduction: {}): Replace {} by {} [context: {}].",
                        substitute.getSource(), substitute.getCostReduction(), substitute.getTargets(),
                        substitute.getReplacement(), substitute.getContext());
                state = substitute.getReplacement();
            }
        } while (improvement.isPresent());

        return state;
    }

    /**
     * Applies substitutions to the given {@code targets} (logical group) until there is nothing to further replace.
     *
     * @param targets list of {@link Proposition} to optimize
     * @param context set of {@link Inference} of assumptions as the context of the targets
     * @return a semantically identical {@link Proposition} to the given {@code target} with equal or less cost
     */
    private static List<Proposition> runSubstitutionRules(List<Proposition> targets, Set<Inference> context) {
        // create local assumptions between targets
        List<Proposition> state = new ArrayList<>(targets);
        Optional<Substitute> improvement;
        do {
            Set<Inference> contextAssumptions = determineConjunctionAssumptions(new AndConjunction(state), context);
            improvement = pickSubstitute(state, contextAssumptions, true);
            if (improvement.isPresent()) {
                Substitute substitute = improvement.get();
                LOGGER.debug("Applying substitution `{}` (cost reduction: {}): Replace {} by {} [context: {}].",
                        substitute.getSource(), substitute.getCostReduction(), substitute.getTargets(),
                        substitute.getReplacement(), substitute.getContext());
                state.removeAll(substitute.getTargets());
                state.add(substitute.getReplacement());
            }
        } while (improvement.isPresent());

        return state;
    }

    /**
     * Invokes all applicable substitution rules for the given {@code targets} and {@code context}. Returns the
     * {@link Substitute} that leads to the biggest cost reduction.
     *
     * @param targets       list of {@link Proposition} to execute substitution rules on
     * @param context       set of {@link Inference} to use in context parameters
     * @param filterContext boolean flag whether to allow duplicate implications from context
     * @return {@link Substitute} object that, when applied, reduces the cost of {@code targets} by >= 0
     */
    private static Optional<Substitute> pickSubstitute(List<Proposition> targets, Set<Inference> context, boolean filterContext) {
        List<Substitute> substitutes = new ArrayList<>();
        for (SubstitutionRule rule : SUBSTITUTION_RULES) {
            for (Substitute.Arguments arguments : getSatisfyingArgs(rule, targets, context, filterContext)) {
                invokeSubstitutionRule(rule.getMethod(), arguments)
                        .ifPresent(proposition -> {
                            Substitute substitute = new Substitute(rule.getName(), arguments, proposition);
                            if (substitute.getCostReduction() >= 0)
                                substitutes.add(substitute);
                        });
            }
        }

        // return the substitute that leads to the biggest cost reduction
        // in case of a tie, take a deterministic choice based on information from the statement
        return substitutes.stream().max(
                Comparator.comparingInt(Substitute::getCostReduction)
                        .thenComparingInt(Substitute::getReferenceHashCode)
                        .thenComparingInt(Substitute::getReplacementHashCode)
                        .thenComparingInt(Substitute::getTargetHashCode)
        );
    }

    /**
     * Returns all combinations of {@code targets} and {@code context} that fulfill the signature of the provided
     * {@code rule}.
     *
     * @param rule          {@link SubstitutionRule} to get satisfying arguments for
     * @param targets       list of {@link Proposition} to choose targets arguments from
     * @param context       list of {@link Inference} to choose context arguments from
     * @param filterContext flag whether to allow the replacement of mutual inferences
     * @return list of {@link Substitute.Arguments} that satisfy the signature of the given {@link SubstitutionRule}
     */
    private static List<Substitute.Arguments> getSatisfyingArgs(SubstitutionRule rule, List<Proposition> targets, Set<Inference> context, boolean filterContext) {
        List<Substitute.Arguments> satisfyingArguments = new ArrayList<>();

        // check whether this substitution rule can be satisfied using the provided targets
        Set<List<Proposition>> targetCombinations = getCombinations(rule.getTargetTypes(), targets);

        List<Class<? extends Proposition>> contextTypes = rule.getContextTypes();

        if (!contextTypes.isEmpty()) {
            for (List<Proposition> targetCombination : targetCombinations) {
                Set<Proposition> targetContext = filterTargetContext(context, targetCombination, filterContext);
                Set<List<Proposition>> contextCombinations = getCombinations(contextTypes, targetContext);
                satisfyingArguments.addAll(contextCombinations.stream()
                        .map(contextCombination -> new Substitute.Arguments(targetCombination, contextCombination))
                        .collect(Collectors.toList()));
            }
        } else {
            satisfyingArguments.addAll(targetCombinations.stream()
                    .map(targetCombination -> new Substitute.Arguments(targetCombination, Collections.emptyList()))
                    .collect(Collectors.toList()));
        }

        return satisfyingArguments;
    }

    /**
     * Maps the given set of {@link Inference} to their conclusion ({@link Proposition}), filtering mutual inferences
     * if {@code doFilter} is set.
     * <p>
     * Mutual inference filtering is required to prevent mutual inferences (e.g. duplicates) to both be replaced due to
     * their conclusion.
     *
     * @param context  list of {@link Inference} to filter
     * @param targets  list of {@link Proposition} that are target of a substitution rule
     * @param doFilter flag whether to filter mutual inferences
     * @return set of conclusions ({@link Proposition}) to use as context
     */
    private static Set<Proposition> filterTargetContext(Set<Inference> context, List<Proposition> targets, boolean doFilter) {
        return context.stream()
                .filter(inference -> !doFilter || inference.getAssumptions().stream().noneMatch(targets::contains))
                .map(Inference::getConclusion)
                .collect(Collectors.toSet());
    }

    /**
     * Invoke the given {@code substitutionRule}, which is expected to return an {@link Optional} of {@link Proposition}.
     *
     * @param substitutionRule {@link Method} to invoke
     * @param arguments        arguments to use for method invocation
     * @return {@link Optional} of {@link Proposition} returned by the invoked method
     */
    private static Optional<Proposition> invokeSubstitutionRule(Method substitutionRule, Substitute.Arguments arguments) {
        if (!ReflectionHelper.checkReturnType(substitutionRule, Optional.class, Proposition.class))
            throw new InternalCompilerException("Invalid implementation of @Substitution annotated method %s in %s. "
                    + "Return type must be Optional<Proposition>.", substitutionRule.getName(), substitutionRule.getDeclaringClass());

        try {
            Object[] args = ArrayUtils.addAll(arguments.getTargets().toArray(), arguments.getContext().toArray());
            @SuppressWarnings("unchecked")
            Optional<Proposition> substitution = (Optional<Proposition>) substitutionRule.invoke(null, args);

            return substitution;
        } catch (ReflectiveOperationException | IllegalArgumentException | ClassCastException e) {
            throw new InternalCompilerException(e, "Invalid implementation of @Substitution annotated method %s in %s: "
                    + "Ensure the method is static and its parameters are matching the annotation.",
                    substitutionRule.getName(), substitutionRule.getDeclaringClass());
        }
    }

    /**
     * Prepares all methods annotated as {@link Substitution}, replacing all wildcards used in
     * {@link Substitution#target()} or {@link Substitution#context()}.
     *
     * @return list of {@link SubstitutionRule}
     */
    private static List<SubstitutionRule> prepareSubstitutionRules() {
        Set<Method> substitutionRules = ReflectionHelper.getMethodsAnnotatedWith(Substitution.class);
        return substitutionRules.stream()
                .map(Optimizer::processSubstitutionRule)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Processes the given {@link Method}, expected to be annotated as {@link Substitution}, by calling the replacement
     * methods for the wildcards {@link Gadget} and {@link Conjunction}.
     *
     * @param method {@link Method} to process
     * @return list of all {@link SubstitutionRule} that could be derived from the given {@code method}
     */
    private static List<SubstitutionRule> processSubstitutionRule(Method method) {
        Substitution annotation = method.getAnnotation(Substitution.class);
        List<Class<? extends Proposition>> targetTypes = Arrays.asList(annotation.target());
        List<Class<? extends Proposition>> contextTypes = Arrays.asList(annotation.context());

        SubstitutionRule rule = new SubstitutionRule(method, targetTypes, contextTypes);
        return replaceGadgetWildcard(rule).stream()
                .map(Optimizer::replaceConjunctionWildcard)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Replaces the wildcard {@link Gadget} in the given {@link SubstitutionRule} by all classes implementing the
     * {@link Gadget}.
     *
     * @param rule {@link SubstitutionRule} to process
     * @return list of {@link SubstitutionRule}, where each object represents a wildcard replacement or the given
     * {@code rule} if the wildcard is not present
     */
    private static List<SubstitutionRule> replaceGadgetWildcard(SubstitutionRule rule) {
        if (rule.getTargetTypes().contains(Gadget.class) || rule.getContextTypes().contains(Gadget.class))
            return GADGET_TYPES.stream()
                    .map(gadget -> createPermutation(rule, Gadget.class, gadget))
                    .collect(Collectors.toList());
        else
            return List.of(rule);
    }

    /**
     * Replaces the wildcard {@link Conjunction} in the given {@link SubstitutionRule} by all classes implementing the
     * {@link Conjunction}.
     *
     * @param rule {@link SubstitutionRule} to process
     * @return list of {@link SubstitutionRule}, where each object represents a wildcard replacement or the given
     * {@code rule} if the wildcard is not present
     */
    private static List<SubstitutionRule> replaceConjunctionWildcard(SubstitutionRule rule) {
        if (rule.getTargetTypes().contains(Conjunction.class) || rule.getContextTypes().contains(Conjunction.class))
            return CONJUNCTION_TYPES.stream()
                    .map(conjunction -> createPermutation(rule, Conjunction.class, conjunction))
                    .collect(Collectors.toList());
        else
            return List.of(rule);
    }

    /**
     * Creates a permutation of the given {@link SubstitutionRule} by replacing all {@code wildcard} by
     * {@code replacement} in {@link SubstitutionRule#getTargetTypes()} and {@link SubstitutionRule#getContextTypes()}.
     *
     * @param rule        {@link SubstitutionRule} to permute
     * @param wildcard    type to match
     * @param replacement type to replace with
     * @return permutation of {@code rule} with all {@code wildcard} replaced by {@code replacement}
     */
    private static SubstitutionRule createPermutation(
            SubstitutionRule rule,
            Class<? extends Proposition> wildcard,
            Class<? extends Proposition> replacement
    ) {
        List<Class<? extends Proposition>> permutationTargetTypes = rule.getTargetTypes().stream()
                .map(type -> replaceWildcard(type, wildcard, replacement))
                .collect(Collectors.toList());
        List<Class<? extends Proposition>> permutationContextTypes = rule.getContextTypes().stream()
                .map(type -> replaceWildcard(type, wildcard, replacement))
                .collect(Collectors.toList());
        return new SubstitutionRule(rule.getMethod(), permutationTargetTypes, permutationContextTypes);
    }

    /**
     * Replaces the given {@code target} by {@code replacement} if it matches the provided {@code wildcard}.
     *
     * @param target      object to replace
     * @param wildcard    object to match against
     * @param replacement object to replace by
     * @return {@code replacement} if the {@code target} matches the {@code wildcard}, {@code target} otherwise
     */
    private static <T> T replaceWildcard(T target, T wildcard, T replacement) {
        return target == wildcard ? replacement : target;
    }

    /**
     * Object to represent the result of a successful substitution rule.
     */
    private static class Substitute {
        private String source;
        private Arguments arguments;
        private Proposition replacement;

        private Substitute(String source, Arguments arguments, Proposition replacement) {
            this.source = source;
            this.arguments = arguments;
            this.replacement = replacement;
        }

        private Integer getCostReduction() {
            return arguments.getTargets().stream().mapToInt(Proposition::getCostEstimate).sum() - replacement.getCostEstimate();
        }

        private String getSource() {
            return source;
        }

        private List<Proposition> getTargets() {
            return arguments.getTargets();
        }

        private List<Proposition> getContext() {
            return arguments.getContext();
        }

        private Proposition getReplacement() {
            return replacement;
        }

        private int getReferenceHashCode() {
            return Stream.concat(
                    replacement.listAllGadgets().stream(),
                    getTargets().stream().map(Proposition::listAllGadgets).flatMap(Collection::stream)
            )
                    .map(Gadget::getVariables)
                    .flatMap(Collection::stream)
                    .map(Variable::getReference)
                    .filter(Objects::nonNull)
                    .mapToInt(Reference::hashCode)
                    .sum();
        }

        private int getReplacementHashCode() {
            return hashCodeOf(replacement.listAllGadgets().stream());
        }

        private int getTargetHashCode() {
            return hashCodeOf(getTargets().stream().map(Proposition::listAllGadgets).flatMap(Collection::stream));
        }

        private int hashCodeOf(Stream<Gadget> stream) {
            return stream
                    .map(Gadget::getVariables)
                    .flatMap(Collection::stream)
                    .map(Variable::getPosition)
                    .filter(Objects::nonNull)
                    .mapToInt(Position.Absolute::hashCode)
                    .sum();
        }

        private static class Arguments {
            private List<Proposition> targets;
            private List<Proposition> context;

            private Arguments(List<Proposition> targets, List<Proposition> context) {
                this.targets = targets;
                this.context = context;
            }

            private List<Proposition> getTargets() {
                return targets;
            }

            private List<Proposition> getContext() {
                return context;
            }
        }
    }

    /**
     * Object to represent a substitution rule defined by methods annotated as {@link Substitution}.
     */
    private static class SubstitutionRule {
        private Method method;
        private List<Class<? extends Proposition>> targetTypes;
        private List<Class<? extends Proposition>> contextTypes;

        private SubstitutionRule(Method method, List<Class<? extends Proposition>> targetTypes, List<Class<? extends Proposition>> contextTypes) {
            this.method = method;
            this.targetTypes = targetTypes;
            this.contextTypes = contextTypes;
        }

        private String getName() {
            return method.getName();
        }

        private Method getMethod() {
            return method;
        }

        private List<Class<? extends Proposition>> getTargetTypes() {
            return targetTypes;
        }

        private List<Class<? extends Proposition>> getContextTypes() {
            return contextTypes;
        }
    }
}
