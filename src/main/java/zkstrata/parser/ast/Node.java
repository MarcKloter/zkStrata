package zkstrata.parser.ast;

import org.apache.commons.text.TextStringBuilder;
import zkstrata.parser.ast.connectives.Connective;
import zkstrata.utils.StatementBuilder;

public interface Node {
    void addTo(StatementBuilder statementBuilder);

    /**
     * Returns the string representation of this node (subtree or leaf) as tree structure of connectives and predicates.
     */
    default String toDebugString() {
        TextStringBuilder builder = new TextStringBuilder();
        append(builder, "", true);
        return builder.build().trim();
    }

    /**
     * Appends the string format of this object as tree branch/leaf to the given string builder.
     * <p>
     * Based on: https://stackoverflow.com/a/27153988/4382892
     *
     * @param builder {@link TextStringBuilder} to append to
     * @param prefix  string to prefix to the string representation of this
     * @param isTail  indicator whether this node is part of the left part (tail) of a subtree
     */
    private void append(TextStringBuilder builder, String prefix, boolean isTail) {
        if (this instanceof Connective && ((Connective) this).getRight() != null) {
            ((Connective) this).getRight().append(builder, prefix + (isTail ? "│   " : "    "), false);
        }

        builder.append(prefix).append(isTail ? "└── " : "┌── ").append(this.toString()).appendNewLine();

        if (this instanceof Connective && ((Connective) this).getLeft() != null) {
            ((Connective) this).getLeft().append(builder, prefix + (isTail ? "    " : "│   "), true);
        }
    }
}
