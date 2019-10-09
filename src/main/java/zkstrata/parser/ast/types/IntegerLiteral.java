package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;

import java.math.BigInteger;

public class IntegerLiteral extends Literal<BigInteger> {
    private BigInteger value;

    public IntegerLiteral(String value, Position position) {
        super(position);
        this.value = new BigInteger(value, 10);
    }

    @Override
    public BigInteger getValue() {
        return value;
    }
}
