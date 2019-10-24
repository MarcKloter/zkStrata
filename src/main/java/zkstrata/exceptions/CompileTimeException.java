package zkstrata.exceptions;

import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.utils.ErrorUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: consider error codes
public class CompileTimeException extends RuntimeException {
    private final List<Position.Absolute> errors;

    public CompileTimeException(String source, String statement, ParserException e) {
        this(e.getMessage(), e.getPositions().stream().map(pos -> new Position.Absolute(source, statement, pos)).collect(Collectors.toList()));
    }

    public CompileTimeException(String message, Variable variable) {
        this(message, List.of(variable.getPosition()));
    }

    public CompileTimeException(String message, Set<Variable> variables) {
        this(message, variables.stream().map(Variable::getPosition).collect(Collectors.toList()));
    }

    public CompileTimeException(String message, Position.Absolute position) {
        this(message, List.of(position));
    }

    public CompileTimeException(String message, List<Position.Absolute> positions) {
        super(String.format("%s: %s%n%s", createMessage(positions), message, ErrorUtils.underline(positions)));
        this.errors = positions;
    }

    private static String createMessage(List<Position.Absolute> positions) {
        Set<Integer> lineNumbers = positions.stream().map(Position::getLine).collect(Collectors.toSet());
        if (lineNumbers.size() == 1)
            return String.format("Error at line %s", lineNumbers.iterator().next());
        else
            return String.format("Error at lines %s", lineNumbers.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    public List<Position.Absolute> getErrors() {
        return errors;
    }
}
