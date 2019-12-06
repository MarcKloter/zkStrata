package zkstrata.domain.conjunctions;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.Proposition;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.parser.ast.connectives.Or;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AstElement(Or.class)
public class OrConjunction extends AbstractConjunction {
    public OrConjunction(List<Proposition> parts) {
        super(parts);
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
    public List<TargetFormat> toTargetFormat() {
        List<TargetFormat> result = new ArrayList<>();
        result.add(new TargetFormat("OR", Collections.emptyMap()));
        result.add(new TargetFormat("[", Collections.emptyMap()));
        getParts().forEach(proposition -> {
            result.add(new TargetFormat("{", Collections.emptyMap()));
            result.addAll(proposition.toTargetFormat());
            result.add(new TargetFormat("}", Collections.emptyMap()));
        });
        result.add(new TargetFormat("]", Collections.emptyMap()));
        return result;
    }
}
