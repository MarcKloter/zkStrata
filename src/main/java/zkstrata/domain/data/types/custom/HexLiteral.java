package zkstrata.domain.data.types.custom;

import zkstrata.domain.data.types.Literal;
import zkstrata.utils.HexEncoder;

import java.math.BigInteger;

public class HexLiteral extends Literal {
    public HexLiteral(String value) {
        super(new BigInteger(trim(value), 16));
    }

    public HexLiteral(BigInteger value) {
        super(value);
    }

    private static String trim(String string) {
        return string.replaceFirst("^0x", "");
    }

    @Override
    public String toHex() {
        return HexEncoder.encode((BigInteger) getValue());
    }

    @Override
    public Class<?> getType() {
        return HexLiteral.class;
    }
}
