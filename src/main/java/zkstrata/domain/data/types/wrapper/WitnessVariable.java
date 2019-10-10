package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;

/**
 * Class representing a witness variable (nonpublic value).
 * Witness data must be referenced using a selector in statements (to avoid leaking information) and thus, such
 * variables can only be compared using the selector (although the prover would be able to access its value directly).
 */
public class WitnessVariable extends AbstractVariable {
    private Selector selector;

    public WitnessVariable(Value value, Selector selector, Position position) {
        super(value, position);
        this.selector = selector;
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return selector.equals(((WitnessVariable) obj).getSelector());
    }
}
