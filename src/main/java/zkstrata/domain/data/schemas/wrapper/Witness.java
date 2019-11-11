package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.Position;

/**
 * Wrapper class to mark a {@link Schema} as witness (only known to the prover, confidential information).
 */
public class Witness extends AbstractStructuredData<Variable> {
    public Witness(String alias, Schema schema, ValueAccessor accessor, ValueAccessor metaData) {
        super(alias, schema, accessor, metaData);
    }

    @Override
    public boolean isWitness() {
        return true;
    }

    @Override
    public Variable getVariable(Selector selector, Position.Absolute position) {
        try {
            Value value = resolve(getSchema(), selector);
            Reference reference = new Reference(value.getType(), getAlias(), selector);
            return (indexOfMetadata(getSchema(), selector) > -1)
                    ? new InstanceVariable((Literal) value, reference, position)
                    : new WitnessVariable(value, reference, position);
        } catch (ClassCastException e) {
            e.printStackTrace();
            String msg = String.format("Witness data %s does not match the structure of schema %s.", getAlias(), getSchema().getClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }
    }
}
