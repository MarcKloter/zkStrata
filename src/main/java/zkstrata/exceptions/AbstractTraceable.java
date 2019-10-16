package zkstrata.exceptions;

public abstract class AbstractTraceable implements Traceable {
    private Position position;

    public AbstractTraceable(Position position) {
        this.position = position;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
