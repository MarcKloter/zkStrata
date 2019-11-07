package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;

public class Null implements Variable {
    @Override
    public Class<?> getType() {
        return Null.class;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public Reference getReference() {
        return null;
    }

    @Override
    public Position.Absolute getPosition() {
        return null;
    }
}
