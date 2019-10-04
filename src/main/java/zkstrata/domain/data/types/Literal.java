package zkstrata.domain.data.types;

import zkstrata.utils.HexEncoder;

public class Literal implements Value {
    private Object value;

    public Literal(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Class<?> getType() {
        return value.getClass();
    }

    @Override
    public String toHex() {
        return HexEncoder.encode(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return value.equals(((Literal) obj).getValue());
    }
}
