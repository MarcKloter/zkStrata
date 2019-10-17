package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;

public class StringLiteral extends Literal<String> {
    private String value;

    public StringLiteral(String value, Position position) {
        super(position);
        this.value = value.substring(1, value.length() - 1);
    }

    @Override
    public String getValue() {
        return value;
    }
}
