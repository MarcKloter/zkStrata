package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;
import zkstrata.parser.TokenType;
import zkstrata.zkStrataLexer;

public class Constant extends Value<String> {
    private String value;

    @TokenType(type = zkStrataLexer.CONSTANT)
    public Constant(String value, Position position) {
        super(position);
        this.value = value.substring(1);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("_%s", value);
    }
}
