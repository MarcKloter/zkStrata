package zkstrata.domain.data.accessors;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Value;

public interface ValueAccessor {
    String getSubject();
    Value getValue(Selector selector);
}
