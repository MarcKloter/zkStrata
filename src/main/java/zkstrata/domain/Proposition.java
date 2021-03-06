package zkstrata.domain;

import org.apache.commons.text.TextStringBuilder;
import zkstrata.codegen.representations.BulletproofsGadgets;
import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.conjunctions.Conjunction;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.optimizer.TrueProposition;

import java.util.*;

public interface Proposition extends BulletproofsGadgets {
    /**
     * Returns a {@link TrueProposition}, which can be used as a neutral element (no effect on a statement).
     *
     * @return true proposition
     */
    static Proposition trueProposition() {
        return new TrueProposition();
    }

    /**
     * Combines the given {@link Proposition} with this using an {@link AndConjunction}.
     *
     * @param proposition {@link Proposition} to combine with
     * @return {@link AndConjunction} of the combination, just a proposition in case one of the two is always true
     */
    default Proposition combine(Proposition proposition) {
        if (this.isTrueProposition())
            return proposition;
        else if (proposition.isTrueProposition())
            return this;
        else
            return new AndConjunction(List.of(this, proposition));
    }

    default boolean isTrueProposition() {
        return this instanceof TrueProposition;
    }

    /**
     * Traverses through all objects belonging to this proposition, listing all gadgets visited as flattened map.
     *
     * @return {@link List} of {@link Gadget} accessible through this proposition
     */
    List<Gadget> listAllGadgets();

    /**
     * Returns a list of gadget-combinations accessible through this proposition.
     * Every element ({@link List} of {@link Gadget}) represents a distinct evaluation path in the statement that, when
     * true, will make the overall statement true.
     * <p>
     * Example: In an OR conjunction, there will be a separate path for each conjunct part, as any of the parts can
     * make the overall statement true independently.
     *
     * @return {@link List} of evaluation paths, which are {@link List} of {@link Gadget}
     */
    List<List<Gadget>> getEvaluationPaths();

    /**
     * Returns an upper bound for the cost (number of constraints) to prove/verify this proposition.
     */
    int getCostEstimate();

    /**
     * Returns the string representation of this proposition as tree structure of conjunctions and gadgets.
     */
    default String toDebugString() {
        TextStringBuilder builder = new TextStringBuilder();
        append(builder, "", "");
        return builder.build();
    }

    /**
     * Appends the string format of this object as tree branch/leaf to the given string builder.
     * <p>
     * Based on: https://stackoverflow.com/a/8948691/4382892
     *
     * @param builder        {@link TextStringBuilder} to append to
     * @param prefix         string to prefix to the string representation of this
     * @param childrenPrefix prefix to apply to children of this (used to indent multiple levels)
     */
    private void append(TextStringBuilder builder, String prefix, String childrenPrefix) {
        builder.append(prefix).append(this.toString());

        if (this instanceof Conjunction) {
            for (Iterator<Proposition> it = ((Conjunction) this).getParts().iterator(); it.hasNext(); ) {
                Proposition next = it.next();
                if (it.hasNext()) {
                    next.append(builder.appendNewLine(), childrenPrefix + "|-- ", childrenPrefix + "|   ");
                } else {
                    next.append(builder.appendNewLine(), childrenPrefix + "\\-- ", childrenPrefix + "    ");
                }
            }
        }
    }
}
