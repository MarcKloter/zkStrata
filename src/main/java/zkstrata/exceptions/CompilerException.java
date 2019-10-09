package zkstrata.exceptions;

import zkstrata.utils.ErrorUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: consider error codes
public class CompilerException extends RuntimeException {
    private List<Position> errors;

    public CompilerException(String statement, CompileTimeException e) {
        this(e.getMessage(), statement, e.getPositions());
    }

    public CompilerException(String message, String statement, Position position) {
        this(message, statement, List.of(position));
    }

    public CompilerException(String message, String statement, List<Position> positions) {
        super(String.format("%s: %s%n%s", createMessage(positions), message, ErrorUtils.underline(statement, positions)));
        this.errors = positions;
    }

    private static String createMessage(List<Position> positions) {
        Set<Integer> lineNumbers = positions.stream().map(Position::getLine).collect(Collectors.toSet());
        if (lineNumbers.size() == 1)
            return String.format("Error at line %s", lineNumbers.iterator().next());
        else
            return String.format("Error at lines %s", lineNumbers.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    public List<Position> getErrors() {
        return errors;
    }
}
