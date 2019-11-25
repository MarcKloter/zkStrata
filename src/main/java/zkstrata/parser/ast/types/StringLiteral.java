package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;
import zkstrata.parser.TokenType;
import zkstrata.zkStrataLexer;

public class StringLiteral extends Literal<String> {
    private String value;

    @TokenType(type = zkStrataLexer.STRING_LITERAL)
    public StringLiteral(String value, Position position) {
        super(position);
        this.value = value.substring(1, value.length() - 1);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("'%s'", value);
    }
}
