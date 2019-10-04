package zkstrata.domain.data.types.custom;

import zkstrata.domain.data.types.Literal;

public class HexLiteral extends Literal {
    public HexLiteral(String value) {
        super(trim(value));
    }

    private static String trim(String string) {
        return string.replaceFirst("^0x", "");
    }

    @Override
    public String toHex() {
        return (String) getValue();
    }
}
