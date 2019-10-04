package zkstrata.parser.ast;

public abstract class Leaf {
    private Position position;

    public Leaf(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
