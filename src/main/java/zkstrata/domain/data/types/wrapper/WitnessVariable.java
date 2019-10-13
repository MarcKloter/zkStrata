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
    private Reference reference;

    public WitnessVariable(Value value, Reference reference, Position.Absolute position) {
        super(value, position);
        this.reference = reference;
    }

    public Reference getReference() {
        return reference;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return reference.equals(((WitnessVariable) obj).getReference());
    }
}
