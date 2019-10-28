package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.Position;

/**
 * Wrapper class to mark a {@link Schema} as witness (only known to the prover, confidential information).
 */
public class Witness extends AbstractStructuredData<WitnessVariable> {
    public Witness(String alias, Schema schema, ValueAccessor accessor) {
        super(alias, schema, accessor);
    }

    @Override
    public boolean isWitness() {
        return true;
    }

    @Override
    public WitnessVariable getVariable(Selector selector, Position.Absolute position) {
        try {
            Value value = resolve(getSchema(), selector, getAccessor());
            return new WitnessVariable(value, new Reference(value.getType(), getAlias(), selector), position);
        } catch (ClassCastException e) {
            String msg = String.format("Witness data %s does not match the structure of schema %s.", getAlias(), getSchema().getClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }
    }
}
