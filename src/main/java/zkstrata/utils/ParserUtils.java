package zkstrata.utils;

import org.antlr.v4.runtime.Token;
import zkstrata.exceptions.Position;

public class ParserUtils {
    public static Position getPosition(Token token) {
        return new Position(token.getText(), token.getLine(), token.getCharPositionInLine());
    }
}
