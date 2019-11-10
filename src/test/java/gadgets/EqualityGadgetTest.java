package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EqualityGadgetTest {
    private static final Position.Absolute DUMMY_POS = new Position.Absolute("src", "stmt", "t", 1, 1);

    private static final InstanceVariable INSTANCE_VAR_17 = new InstanceVariable(new Literal(BigInteger.valueOf(17)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_41 = new InstanceVariable(new Literal(BigInteger.valueOf(41)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_STRING = new InstanceVariable(new Literal("String"), null, DUMMY_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias2", new Selector(List.of("selector2")));
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
    void Instance_Equals_Instance_No_Contradiction() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, INSTANCE_VAR_41);
        assertDoesNotThrow(() -> {
            EqualityGadget.checkContradiction(equalityGadget);
        });
    }

    @Test
    void Witness_Equals_Self_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_1);
        assertEquals(Collections.emptySet(), EqualityGadget.removeWitnessEqualsSelf(equalityGadget));
    }

    @Test
    void Witness_Equals_Self_No_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(Set.of(equalityGadget), EqualityGadget.removeWitnessEqualsSelf(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, INSTANCE_VAR_17);
        assertEquals(Collections.emptySet(), EqualityGadget.removeInstanceEqualsInstance(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, INSTANCE_VAR_41);
        assertEquals(Set.of(equalityGadget), EqualityGadget.removeInstanceEqualsInstance(equalityGadget));
    }

    @Test
    void Is_Equal_To() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        assertTrue(equalityGadget1.isEqualTo(equalityGadget2));
    }

    @Test
    void Is_Not_Equal_To() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_2);
        assertFalse(equalityGadget1.isEqualTo(equalityGadget2));
    }

    @Test
    void Parity_Implication_1() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertTrue(implication.isEqualTo((EqualityGadget) result.get()));
    }

    @Test
    void Parity_Implication_2() {
        EqualityGadget equalityGadget1 = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_1);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertTrue(implication.isEqualTo((EqualityGadget) result.get()));
    }

    @Test
    void Parity_Implication_3() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertTrue(implication.isEqualTo((EqualityGadget) result.get()));
    }

    @Test
    void Parity_Implication_4() {
        EqualityGadget equalityGadget1 = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_1);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertTrue(implication.isEqualTo((EqualityGadget) result.get()));
    }

    @Test
    void Parity_None_Implication() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(INSTANCE_VAR_41, WITNESS_VAR_2);

        Optional<Gadget> result1 = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result1.isEmpty());

        Optional<Gadget> result2 = EqualityGadget.implyEquality(equalityGadget2, equalityGadget1);
        assertTrue(result2.isEmpty());
    }

    @Test
    void Type_Mismatch() {
        assertThrows(CompileTimeException.class, () -> {
            new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_STRING);
        });
    }
}
