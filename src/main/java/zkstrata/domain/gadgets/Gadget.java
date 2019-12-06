package zkstrata.domain.gadgets;

import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.wrapper.Variable;

import java.util.List;
import java.util.Map;

public interface Gadget<T extends Gadget> extends Proposition {
    /**
     * Initializes fields of this gadget annotated with {@link Type} according to the given source.
     *
     * @param source {@link Map} containing the field name {@link String} and value {@link Object} pairs
     */
    void initFrom(Map<String, Object> source);

    List<Variable> getVariables();

    /**
     * Indicates whether this gadget instance is equal to the provided one.
     */
    boolean isEqualTo(T other);

    /**
     * Hook method that will be called after @Type annotated fields were wired.
     */
    void performChecks();

    String toDebugString();
}
