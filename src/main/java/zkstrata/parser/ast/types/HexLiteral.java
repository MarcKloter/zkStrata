package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;
import zkstrata.parser.TokenType;
import zkstrata.zkStrataLexer;

public class HexLiteral extends Literal<String> {
    private String value;

    @TokenType(type = zkStrataLexer.HEX_LITERAL)
    public HexLiteral(String value, Position position) {
        super(position);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}