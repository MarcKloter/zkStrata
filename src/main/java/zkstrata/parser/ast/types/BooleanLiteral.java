package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;
import zkstrata.parser.TokenType;
import zkstrata.zkStrataLexer;

public class BooleanLiteral extends Literal<Boolean> {
    private Boolean value;

    @TokenType(type = zkStrataLexer.BOOLEAN_LITERAL)
    public BooleanLiteral(String value, Position position) {
        super(position);
        this.value = value.equals("TRUE");
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s", value.toString().toUpperCase());
    }
}
