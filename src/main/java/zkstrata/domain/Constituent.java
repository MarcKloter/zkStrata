package zkstrata.domain;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.conjunctions.Conjunction;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.exceptions.InternalCompilerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface Constituent {
    /**
     * Combines the given {@link Constituent} with this using an {@link AndConjunction}.
     *
     * @param constituent {@link Constituent} to combine with
     * @return {@link AndConjunction} of the combination
     */
    default Constituent combine(Constituent constituent) {
        return new AndConjunction(List.of(this, constituent));
    }

    /**
     * Traverses through all objects belonging to this constituent, listing all gadgets visited as flattened map.
     *
     * @return {@link List} of {@link Gadget} accessible through this constituent
     */
    default List<Gadget> listAllGadgets() {
        if (this instanceof Gadget)
            return new ArrayList<>(Arrays.asList((Gadget) this));

        if (this instanceof Conjunction)
            return ((Conjunction) this).getParts().stream()
                    .map(Constituent::listAllGadgets)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

        throw new InternalCompilerException("Unhandled instance %s found.", this.getClass());
    }

    /**
     * Transforms this constituent into the target format.
     */
    List<TargetFormat> toTargetFormat();

    /**
     * Returns a list of gadget-combinations accessible through this constituent.
     * Every element ({@link List} of {@link Gadget}) represents a distinct evaluation path in the statement that, when
     * true, will make the overall statement true.
     * <p>
     * Example: In an OR conjunction, there will be a separate path for each conjunct part, as any of the parts can
     * make the overall statement true independently.
     *
     * @return {@link List} of evaluation paths, which are {@link List} of {@link Gadget}
     */
    List<List<Gadget>> getEvaluationPaths();
}
