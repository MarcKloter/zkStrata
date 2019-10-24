package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EqualityGadgetTest {
    private static final Position.Absolute DUMMY_POS = new Position.Absolute("src", "stmt", "t", 1, 1);

    private static final InstanceVariable INSTANCE_VAR_17 = new InstanceVariable(new Literal(BigInteger.valueOf(17)), DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_41 = new InstanceVariable(new Literal(BigInteger.valueOf(41)), DUMMY_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias", new Selector(List.of("selector")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias", new Selector(List.of("selector")));
    private static final WitnessVariable WITNESS_VAR_1 = new WitnessVariable(REF_1, REF_1, DUMMY_POS);
    private static final WitnessVariable WITNESS_VAR_2 = new WitnessVariable(REF_2, REF_2, DUMMY_POS);

    @Test
    void Instance_Equals_Instance_Contradiction() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, INSTANCE_VAR_17);
        assertThrows(CompileTimeException.class, () -> {
            EqualityGadget.checkContradiction(equalityGadget);
        });
    }

    @Test
    void Witness_Equals_Self_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_1);
        assertEquals(Collections.emptyList(), EqualityGadget.removeWitnessEqualsSelf(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, INSTANCE_VAR_17);
        assertEquals(Collections.emptyList(), EqualityGadget.removeInstanceEqualsInstance(equalityGadget));
    }
}
