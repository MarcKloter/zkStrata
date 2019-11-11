package zkstrata.domain.data.types;

public interface Value {
    Class<?> getType();

    String toHex();
}
