package zkstrata.parser.ast.types;

import zkstrata.parser.ast.Position;

public class IntegerLiteral extends Literal<Integer> {
    private Integer value;

    public IntegerLiteral(String value, Position position) {
        super(position);
        this.value = Integer.parseInt(value);
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
