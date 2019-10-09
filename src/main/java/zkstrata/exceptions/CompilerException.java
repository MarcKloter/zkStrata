package zkstrata.exceptions;

import zkstrata.utils.ErrorUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TODO: consider error codes
public class CompilerException extends RuntimeException {
    private List<Position> errors;

    public CompilerException(String statement, CompileTimeException e) {
        this(e.getMessage(), statement, e.getPositions());
    }

    public CompilerException(String message, String statement, Position position) {
        super(String.format("Error at line %s: %s%n%s", position.getLine(), message, ErrorUtils.underline(statement, position)));
        this.errors = List.of(position);
    }

    public CompilerException(String message, String statement, List<Position> positions) {
        super(String.format("Error: %s%n%s", message, ErrorUtils.underline(statement, positions)));
        this.errors = positions;
    }

    public List<Position> getErrors() {
        return errors;
    }
}
