package zkstrata.exceptions;

import zkstrata.exceptions.Position;

import java.util.List;

public class CompileTimeException extends Exception {
    private String message;
    private List<Position> positions;

    public CompileTimeException(String message, Position position) {
        super(message);
        this.message = message;
        this.positions = List.of(position);
    }

    public CompileTimeException(String message, List<Position> positions) {
        super(message);
        this.message = message;
        this.positions = positions;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public List<Position> getPositions() {
        return positions;
    }
}
