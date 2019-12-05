package zkstrata.domain.conjunctions;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.Constituent;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.parser.ast.connectives.And;
import zkstrata.utils.CombinatoricsUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AstElement(And.class)
public class AndConjunction implements Conjunction {
    private List<Constituent> parts;

    public AndConjunction(List<Constituent> parts) {
        this.parts = parts;
    }

    @Override
    public List<Constituent> getParts() {
        return parts;
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
     * Which is the cartesian product of the evaluation paths returned by the parts ({@link Constituent#getEvaluationPaths()}).
     */
    @Override
    public List<List<Gadget>> getEvaluationPaths() {
        return CombinatoricsUtils.computeCartesianProduct(
                parts.stream()
                        .map(Constituent::getEvaluationPaths)
                        .collect(Collectors.toList())
        ).stream()
                .map(evaluationPath -> evaluationPath.stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TargetFormat> toTargetFormat() {
        return parts.stream().map(Constituent::toTargetFormat).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
