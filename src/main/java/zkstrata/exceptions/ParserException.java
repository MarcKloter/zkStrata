package zkstrata.exceptions;

import java.util.List;

public class ParserException extends RuntimeException {
    private final String message;
    private final List<Position.Relative> positions;

    public ParserException(String message, Position.Relative position) {
        super(message);
        this.message = message;
        this.positions = List.of(position);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public List<Position.Relative> getPositions() {
        return positions;
    }
}
