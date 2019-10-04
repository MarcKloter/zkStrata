package zkstrata.utils;

import org.antlr.v4.runtime.Token;
import zkstrata.parser.ast.Position;

public class ParserUtils {
    public static Position getPosition(Token token) {
        return new Position(token.getLine(), token.getCharPositionInLine());
    }
}
