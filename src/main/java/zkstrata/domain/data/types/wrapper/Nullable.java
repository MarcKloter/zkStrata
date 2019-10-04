package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Value;

public class Nullable implements Variable {
    @Override
    public Class<?> getType() {
        return Nullable.class;
    }

    @Override
    public Value getValue() {
        return null;
    }
}
