package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.Traceable;

public interface Variable extends Traceable {
    Class<?> getType();

    Value getValue();

    Reference getReference();

    @Override
    Position.Absolute getPosition();
}
