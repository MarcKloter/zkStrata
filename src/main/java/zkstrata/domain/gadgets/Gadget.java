package zkstrata.domain.gadgets;

import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.wrapper.Variable;

import java.util.Map;

public interface Gadget extends Proposition {
    /**
     * Initializes fields of this gadget annotated with {@link Type} according to the given source.
     *
     * @param source {@link Map} containing the field name {@link String} and value {@link Object} pairs
     */
    void initFrom(Map<String, Object> source);

    /**
     * Returns all values of fields annotated with {@link Type} as map of field names to values
     *
     * @return map of field names to {@link Variable} used by this gadget
     */
    Map<String, Variable> getVariables();

    /**
     * Hook method that will be called after @Type annotated fields were wired.
     */
    void performChecks();
}
