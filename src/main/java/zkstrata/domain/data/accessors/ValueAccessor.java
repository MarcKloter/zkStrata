package zkstrata.domain.data.accessors;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Value;

public interface ValueAccessor {
    /**
     * Returns the {@link Value} associated with the provided {@code selector}.
     *
     * @param selector {@link Selector} to access a value for
     * @return {@link Value} associated with {@code selector} or {@code null} if nothing is associated with it
     */
    Value getValue(Selector selector);

    /**
     * Returns the information of origin of the values of this accessor as string (e.g. a filename).
     *
     * @return information of origin of the values accessible by this accessor
     */
    String getSource();
}
