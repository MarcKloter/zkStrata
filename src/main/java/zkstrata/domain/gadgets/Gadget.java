package zkstrata.domain.gadgets;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.exceptions.CompileTimeException;

import java.util.Map;

public interface Gadget<T extends Gadget> {
    /**
     * Initializes fields of this gadget annotated with {@link Type} according to the given source.
     *
     * @param source {@link Map} containing the field name and {@link Variable} pairs
     */
    void initFrom(Map<String, Variable> source) throws CompileTimeException;

    /**
     * Indicates whether this gadget instance is equal to the provided one.
     */
    boolean isEqualTo(T other);

    /**
     * Transforms this gadget into the target format.
     */
    TargetFormat toTargetFormat();

    /**
     * Hook method that will be called after @Type annotated fields were wired.
     */
    void performChecks() throws CompileTimeException;
}
