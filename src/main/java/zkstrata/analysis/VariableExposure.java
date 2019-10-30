package zkstrata.analysis;

import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.CompileTimeException;

import java.util.Set;

public class VariableExposure {
    private WitnessVariable witness = null;
    private InstanceVariable instance = null;

    public void markWitness(WitnessVariable witnessVariable) {
        this.witness = witnessVariable;
        this.checkExposure();
    }

    public void markInstance(InstanceVariable instanceVariable) {
        this.instance = instanceVariable;
        this.checkExposure();
    }

    private void checkExposure() {
        if (witness != null && instance != null)
            throw new CompileTimeException("Value is being used as witness and instance data simultaneously.",
                    Set.of(witness, instance));
    }
}
