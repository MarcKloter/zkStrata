package zkstrata.domain.conjunctions;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.Proposition;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.parser.ast.connectives.And;
import zkstrata.utils.CombinatoricsUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AstElement(And.class)
public class AndConjunction extends AbstractConjunction {
    public AndConjunction(List<Proposition> parts) {
        super(parts);
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
     * [[A, C, E], [A, C, F], [A, C, G], [B, C, E], [B, C, F], [B, C, G]]
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
    public List<TargetFormat> toTargetFormat() {
        return getParts().stream()
                .map(Proposition::toTargetFormat)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
