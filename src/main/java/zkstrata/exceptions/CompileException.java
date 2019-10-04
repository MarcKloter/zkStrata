package zkstrata.exceptions;

import zkstrata.parser.ast.Position;
import zkstrata.utils.ErrorUtils;

public class CompileException extends RuntimeException {
    private int line, getCharPositionInLine;

    public CompileException(String message, String input, int offendingInputLength, Position position) {
        super(String.format("Error at line %s: %s%n%s", position.getLine(), message, context(input, offendingInputLength, position)));
        this.line = position.getLine();
        this.getCharPositionInLine = position.getCharPositionInLine();
    }

    private static String context(String input, int offendingInputLength, Position position) {
        int start = position.getCharPositionInLine();
        return ErrorUtils.underlineError(input, start, start + offendingInputLength - 1, position.getLine(), start);
    }

    public int getLine() {
        return line;
    }

    public int getGetCharPositionInLine() {
        return getCharPositionInLine;
    }
}
