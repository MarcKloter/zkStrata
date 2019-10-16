package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.Traceable;

public interface Variable extends Traceable {
    Class<?> getType();
    Value getValue();

    @Override
    Position.Absolute getPosition();
}
