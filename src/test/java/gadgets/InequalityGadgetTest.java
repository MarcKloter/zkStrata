package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.InequalityGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static zkstrata.domain.Proposition.trueProposition;
import static zkstrata.domain.gadgets.impl.InequalityGadget.*;
import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class InequalityGadgetTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));
    private static final InstanceVariable INSTANCE_VAR_STRING = createInstanceVariable(new Literal("String"));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);

    @Test
    void Instance_Unequals_Instance_Contradiction() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, INSTANCE_VAR_41);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkSelfContradiction(inequalityGadget)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Instance_Unequals_Instance_No_Contradiction_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertDoesNotThrow(() -> checkSelfContradiction(inequalityGadget));
    }

    @Test
    void Instance_Unequals_Instance_No_Contradiction_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, WITNESS_VAR_1);
        assertDoesNotThrow(() -> checkSelfContradiction(inequalityGadget));
    }

    @Test
    void Instance_Unequals_Instance_No_Contradiction_3() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, INSTANCE_VAR_17);
        assertDoesNotThrow(() -> checkSelfContradiction(inequalityGadget));
    }

    @Test
    void Equality_Contradiction_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkEqualityContradiction(equalityGadget, inequalityGadget)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Equality_Contradiction_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, WITNESS_VAR_2);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkEqualityContradiction(equalityGadget, inequalityGadget)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Equality_No_Contradiction_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertDoesNotThrow(() -> checkEqualityContradiction(equalityGadget, inequalityGadget));
    }

    @Test
    void Equality_No_Contradiction_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, WITNESS_VAR_2);
        assertDoesNotThrow(() -> checkEqualityContradiction(equalityGadget, inequalityGadget));
    }

    @Test
    void Instance_Unequals_Instance_Substitution() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, INSTANCE_VAR_17);
        assertEquals(of(trueProposition()), removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, INSTANCE_VAR_41);
        assertEquals(empty(), removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        assertEquals(empty(), removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_3() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, WITNESS_VAR_1);
        assertEquals(empty(), removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Outside_of_Bounds_Substitution_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertEquals(of(trueProposition()), removeInequalityOutsideOfBounds(inequalityGadget, boundsCheckGadget));
    }

    @Test
    void Outside_of_Bounds_Substitution_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, WITNESS_VAR_1);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        assertEquals(of(trueProposition()), removeInequalityOutsideOfBounds(inequalityGadget, boundsCheckGadget));
    }

    @Test
    void Outside_of_Bounds_No_Substitution_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_29);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        assertEquals(empty(), removeInequalityOutsideOfBounds(inequalityGadget, boundsCheckGadget));
    }

    @Test
    void Outside_of_Bounds_No_Substitution_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_29);
        assertEquals(empty(), removeInequalityOutsideOfBounds(inequalityGadget, boundsCheckGadget));
    }

    @Test
    void Is_Equal_To_1() {
        InequalityGadget inequalityGadget1 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        InequalityGadget inequalityGadget2 = new InequalityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        assertEquals(inequalityGadget1, inequalityGadget2);
    }

    @Test
    void Is_Equal_To_2() {
        InequalityGadget inequalityGadget1 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        InequalityGadget inequalityGadget2 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(inequalityGadget1, inequalityGadget2);
    }

    @Test
    void Is_Not_Equal_To_1() {
        InequalityGadget inequalityGadget1 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        InequalityGadget inequalityGadget2 = new InequalityGadget(WITNESS_VAR_2, WITNESS_VAR_2);
        assertNotEquals(inequalityGadget1, inequalityGadget2);
    }

    @Test
    void Is_Not_Equal_To_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertFalse(inequalityGadget.equals(null));
    }

    @Test
    void Is_Not_Equal_To_3() {
        InequalityGadget inequalityGadget1 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        InequalityGadget inequalityGadget2 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_1);
        assertNotEquals(inequalityGadget1, inequalityGadget2);
    }

    @Test
    void Type_Mismatch() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_STRING)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }
}
