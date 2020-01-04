package zkstrata.domain.conjunctions;

import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.Proposition;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.optimizer.Substitution;
import zkstrata.optimizer.TrueProposition;
import zkstrata.parser.ast.connectives.And;
import zkstrata.utils.CombinatoricsUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AstElement(And.class)
public class AndConjunction extends AbstractConjunction {
    public AndConjunction(List<Proposition> parts) {
        super(parts);
    }

    /**
     * Checks whether the given {@link AndConjunction} only consists of {@link TrueProposition} objects. If this is the
     * case, replace the conjunction by a {@link TrueProposition}.
     *
     * @param andConjunction {@link AndConjunction} to check
     * @return {@link TrueProposition} if the check succeeds, an empty {@link Optional} otherwise
     */
    @Substitution(target = {AndConjunction.class})
    public static Optional<Proposition> removeTautology(AndConjunction andConjunction) {
        if (andConjunction.getParts().isEmpty())
            return Optional.of(Proposition.trueProposition());

        return Optional.empty();
    }

    /**
     * Checks whether the given {@link AndConjunction} has only one part. If this is the case, replace the conjunction
     * by that part.
     *
     * @param andConjunction {@link AndConjunction} to check
     * @return {@link TrueProposition} if the check succeeds, an empty {@link Optional} otherwise
     */
    @Substitution(target = {AndConjunction.class})
    public static Optional<Proposition> liftUpSinglePart(AndConjunction andConjunction) {
        if (andConjunction.getParts().size() == 1)
            return Optional.of(andConjunction.getParts().get(0));

        return Optional.empty();
    }

    @Override
    public List<Proposition> getParts() {
        return super.getParts().stream()
                .filter(Predicate.not(Proposition::isTrueProposition))
                .collect(Collectors.toList());
    }

    @Override
    public int getCostEstimate() {
        return getParts().stream().mapToInt(Proposition::getCostEstimate).sum();
    }

    /**
     * Returns the cartesian product of the logical evaluation paths of its parts.
     * <p>
     * Example Statement: (A OR B) AND C AND (D OR E OR F)
     * <pre>
     *          AND <-- this conjunction
     *      /    |   \
     *    OR     C    OR
     *   /  \        / | \
     *  A    B      D  E  F</pre>
     * <p>
     * This statement can be proven by showing that at least one of the following gadget-combinations evaluates to true:
     * [[A, C, D], [A, C, E], [A, C, F], [B, C, D], [B, C, E], [B, C, F]]
     * <p>
     * Which is the cartesian product of the evaluation paths returned by the parts ({@link Proposition#getEvaluationPaths()}).
     */
    @Override
    public List<List<Gadget>> getEvaluationPaths() {
        return CombinatoricsUtils.computeCartesianProduct(
                getParts().stream()
                        .map(Proposition::getEvaluationPaths)
                        .collect(Collectors.toList())
        ).stream()
                .map(evaluationPath -> evaluationPath.stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        return getParts().stream()
                .map(Proposition::toBulletproofsGadgets)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<List<Proposition>> getCohesivePropositions() {
        return List.of(getParts());
    }
}
