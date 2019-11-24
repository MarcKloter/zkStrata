package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;

public class HexLiteral extends Literal<String> {
    private String value;

    public HexLiteral(String value, Position position) {
        super(position);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}