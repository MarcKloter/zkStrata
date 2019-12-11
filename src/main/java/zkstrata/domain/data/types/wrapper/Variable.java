package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.Traceable;

public interface Variable extends Traceable {
    /**
     * Returns the type declared for this variable.
     *
     * @return type of this variable
     */
    Class<?> getType();

    /**
     * Returns the value assigned to this variable.
     *
     * @return value of this variable
     */
    Value getValue();

    /**
     * Returns the {@link Reference} this variable was defined by.
     * <p>
     * Can be {@code null} e.g. if the variable is a literal instance variable.
     *
     * @return {@link Reference} or {@code null} if this variable was not defined using a reference
     */
    Reference getReference();

    /**
     * Return the {@link Position.Absolute} holding the positional information of the initialization of this variable.
     * <p>
     * Can be {@code null} e.g. if the variable was defined as fallback value within a gadget implementation.
     *
     * @return {@link Position.Absolute} or {@code null} if this variable was not defined within a statement
     */
    @Override
    Position.Absolute getPosition();
}
