package zkstrata.parser.ast.connectives;

import zkstrata.exceptions.AbstractTraceable;
import zkstrata.exceptions.Position;
import zkstrata.parser.ast.Clause;

public abstract class Connective extends AbstractTraceable implements Clause {
    private Clause left;
    private Clause right;

    public Connective(Clause left, Clause right, Position position) {
        super(position);
        this.left = left;
        this.right = right;
    }

    public Clause getLeft() {
        return left;
    }

    public Clause getRight() {
        return right;
    }
}
