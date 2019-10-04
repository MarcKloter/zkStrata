package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Value;

public interface Variable {
    Class<?> getType();
    Value getValue();
}
