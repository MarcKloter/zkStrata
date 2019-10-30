package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;

/**
 * Class representing a witness variable (nonpublic value).
 * Witness data must be referenced in statements (to avoid leaking information) and thus, such variables can only be
 * compared using the reference (although the prover would be able to access its value directly).
 */
public class WitnessVariable extends AbstractVariable {
    public WitnessVariable(Value value, Reference reference, Position.Absolute position) {
        super(value, reference, position);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return getReference().equals(((WitnessVariable) obj).getReference());
    }

    @Override
    public int hashCode() {
        return getReference().hashCode();
    }

    @Override
    public String toString() {
        return getReference().toString();
    }
}
