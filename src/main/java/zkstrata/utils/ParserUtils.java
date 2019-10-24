package zkstrata.utils;

import org.antlr.v4.runtime.Token;
import zkstrata.exceptions.Position;

public class ParserUtils {
    private ParserUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Position getPosition(Token token) {
        return new Position.Relative(token.getText(), token.getLine(), token.getCharPositionInLine());
    }
}
