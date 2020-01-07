package zkstrata.domain.conjunctions;

import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.optimizer.Substitution;
import zkstrata.optimizer.TrueProposition;
import zkstrata.parser.ast.connectives.Or;
import zkstrata.utils.CombinatoricsUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AstElement(Or.class)
public class OrConjunction extends AbstractConjunction {
    public OrConjunction(List<Proposition> parts) {
        super(parts);
    }

    /**
     * Checks whether the given {@link OrConjunction} has a part that is a tautology. If this is the case, replace the
     * conjunction by a true proposition (neutral element).
     *
     * @param orConjunction {@link OrConjunction} to check
     * @return {@link TrueProposition} if the check succeeds, an empty {@link Optional} otherwise
     */
    @Substitution(target = {OrConjunction.class})
    public static Optional<Proposition> removeTautology(OrConjunction orConjunction) {
        for (Proposition part : orConjunction.getParts()) {
            if (part.getClass() == TrueProposition.class)
                return Optional.of(Proposition.trueProposition());
        }

        return Optional.empty();
    }

    /**
     * Checks whether the given {@link OrConjunction} has children that occur in each part. If this is the case, lift
     * them up as {@link AndConjunction}.
     * <p>
     * Example: The proposition A occurs in each part of the OR conjunction.
     * Input: (A AND B) OR A OR (A AND C)
     * Output: A AND (B OR C)
     * <p>
     * This implementation relies on future conjunctions that logically allow the lift up of common propositions to
     * implement an equivalent of this substitution and thus is explicitly only implemented for {@link AndConjunction}.
     *
     * @param orConjunction {@link OrConjunction} to check
     * @return an {@link AndConjunction} if the check succeeds, an empty {@link Optional} otherwise
     */
    @Substitution(target = {OrConjunction.class})
    public static Optional<Proposition> liftUpCommonPropositions(OrConjunction orConjunction) {
        List<List<Proposition>> parts = new ArrayList<>();
        for (Proposition part : orConjunction.getParts()) {
            if (part instanceof AndConjunction)
                parts.add(((AndConjunction) part).getParts());
            else if (part instanceof Gadget)
                parts.add(List.of(part));
            else
                return Optional.empty();
        }
        Set<Proposition> commonPropositions = CombinatoricsUtils.computeIntersection(parts);

        if (!commonPropositions.isEmpty()) {
            List<Proposition> filteredParts = parts.stream()
                    .map(part -> {
                        // remove common elements from children
                        List<Proposition> filtered = part.stream()
                                .filter(Predicate.not(commonPropositions::contains))
                                .collect(Collectors.toList());
                        if (filtered.size() > 1)
                            return new AndConjunction(filtered);
                        else if (filtered.size() == 1)
                            return filtered.get(0);
                        else
                            return Proposition.trueProposition();
                    }).collect(Collectors.toList());

            List<Proposition> liftedPropositions = new ArrayList<>(commonPropositions);
            // drop the or conjunction if it became a tautology
            if (filteredParts.stream().noneMatch(Proposition.trueProposition()::equals))
                liftedPropositions.add(new OrConjunction(filteredParts));
            return Optional.of(new AndConjunction(liftedPropositions));
        }

        return Optional.empty();
    }

    @Override
    public int getCostEstimate() {
        return getParts().stream().mapToInt(Proposition::getCostEstimate).reduce(1, (a, b) -> a * b);
    }

    /**
     * Returns the flattened combination of the logical evaluation paths of its parts.
     * <p>
     * Example Statement: (A OR B) OR C OR (D AND E)
     * <pre>
     *          OR <-- this conjunction
     *      /   |   \
     *    OR    C    AND
     *   /  \       /  \
     *  A    B     D    E</pre>
     * <p>
     * This statement can be proven by showing that at least one of the following gadget-combinations evaluates to true:
     * [[A], [B], [C], [D, E]]
     * <p>
     * Which is the flattened combination of the evaluation paths returned by the parts ({@link Proposition#getEvaluationPaths()}).
     */
    @Override
    public List<List<Gadget>> getEvaluationPaths() {
        return getParts().stream()
                .map(Proposition::getEvaluationPaths)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        List<BulletproofsGadgetsCodeLine> result = new ArrayList<>();
        result.add(new BulletproofsGadgetsCodeLine("OR", new LinkedHashMap<>()));
        result.add(new BulletproofsGadgetsCodeLine("[", new LinkedHashMap<>()));
        getParts().forEach(proposition -> {
            result.add(new BulletproofsGadgetsCodeLine("{", new LinkedHashMap<>()));
            result.addAll(proposition.toBulletproofsGadgets());
            result.add(new BulletproofsGadgetsCodeLine("}", new LinkedHashMap<>()));
        });
        result.add(new BulletproofsGadgetsCodeLine("]", new LinkedHashMap<>()));
        return result;
    }

    @Override
    public List<List<Proposition>> getCohesivePropositions() {
        return getParts().stream().map(List::of).collect(Collectors.toList());
    }
}
