package zkstrata.exceptions;

public abstract class Traceable {
    private Position position;

    public Traceable(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
