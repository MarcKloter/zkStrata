package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.Optional;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class EqualityGadgetTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));
    private static final InstanceVariable INSTANCE_VAR_STRING = createInstanceVariable(new Literal("String"));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);

    @Test
    void Instance_Equals_Instance_Contradiction() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, INSTANCE_VAR_17);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                EqualityGadget.checkContradiction(equalityGadget)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Instance_Equals_Instance_No_Contradiction_1() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, INSTANCE_VAR_41);
        assertDoesNotThrow(() -> EqualityGadget.checkContradiction(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Contradiction_2() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        assertDoesNotThrow(() -> EqualityGadget.checkContradiction(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Contradiction_3() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, WITNESS_VAR_1);
        assertDoesNotThrow(() -> EqualityGadget.checkContradiction(equalityGadget));
    }

    @Test
    void Witness_Equals_Self_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_1);
        assertEquals(Optional.of(Proposition.trueProposition()), EqualityGadget.removeWitnessEqualsSelf(equalityGadget));
    }

    @Test
    void Witness_Equals_Self_No_Substitution_1() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(Optional.empty(), EqualityGadget.removeWitnessEqualsSelf(equalityGadget));
    }

    @Test
    void Witness_Equals_Self_No_Substitution_2() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        assertEquals(Optional.empty(), EqualityGadget.removeWitnessEqualsSelf(equalityGadget));
    }

    @Test
    void Witness_Equals_Self_No_Substitution_3() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, WITNESS_VAR_1);
        assertEquals(Optional.empty(), EqualityGadget.removeWitnessEqualsSelf(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_Substitution() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, INSTANCE_VAR_17);
        assertEquals(Optional.of(Proposition.trueProposition()), EqualityGadget.removeInstanceEqualsInstance(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_1() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, INSTANCE_VAR_41);
        assertEquals(Optional.empty(), EqualityGadget.removeInstanceEqualsInstance(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_2() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        assertEquals(Optional.empty(), EqualityGadget.removeInstanceEqualsInstance(equalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_3() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);
        assertEquals(Optional.empty(), EqualityGadget.removeInstanceEqualsInstance(equalityGadget));
    }

    @Test
    void Is_Equal_To_1() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        assertEquals(equalityGadget1, equalityGadget2);
    }

    @Test
    void Is_Equal_To_2() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(equalityGadget1, equalityGadget2);
    }

    @Test
    void Is_Not_Equal_To_1() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_2);
        assertNotEquals(equalityGadget1, equalityGadget2);
    }

    @Test
    void Is_Not_Equal_To_2() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertNotEquals(null, equalityGadget);
    }

    @Test
    void Parity_Implication_1() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Parity_Implication_2() {
        EqualityGadget equalityGadget1 = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_1);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Parity_Implication_3() {
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Parity_Implication_4() {
        EqualityGadget equalityGadget1 = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_1);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget implication = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);

        Optional<Gadget> result = EqualityGadget.implyEquality(equalityGadget1, equalityGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertEquals(implication, result.get());
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
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_STRING)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }
}
