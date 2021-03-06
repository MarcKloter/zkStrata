package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.Position;

/**
 * Wrapper class to mark a {@link Schema} as witness (only known to the prover, confidential information).
 */
public class Witness extends AbstractStructuredData<Variable> {
    public Witness(String alias, Schema schema, ValueAccessor accessor) {
        super(alias, schema, accessor);
    }

    @Override
    public boolean isWitness() {
        return true;
    }

    @Override
    public Variable getVariable(Selector selector, Position.Absolute position) {
        Value value = resolve(getSchema(), selector);
        Reference reference = new Reference(value.getType(), getAlias(), selector);
        return new WitnessVariable(value, reference, position);
    }
}
