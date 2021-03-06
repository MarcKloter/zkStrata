package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.SetMembershipGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static zkstrata.domain.Proposition.trueProposition;
import static zkstrata.domain.gadgets.impl.SetMembershipGadget.*;
import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class SetMembershipGadgetTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));
    private static final InstanceVariable INSTANCE_VAR_53 = createInstanceVariable(new Literal(BigInteger.valueOf(53)));
    private static final InstanceVariable INSTANCE_VAR_STRING = createInstanceVariable(new Literal("String"));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);

    private static final Set<Variable> SET_1A = Set.of(INSTANCE_VAR_17, WITNESS_VAR_2, INSTANCE_VAR_41);
    private static final Set<Variable> SET_1B = Set.of(WITNESS_VAR_2, INSTANCE_VAR_41, INSTANCE_VAR_17);
    private static final Set<Variable> SET_2 = Set.of(INSTANCE_VAR_41, WITNESS_VAR_1, INSTANCE_VAR_29, WITNESS_VAR_2);
    private static final Set<Variable> SET_3 = Set.of(INSTANCE_VAR_41, INSTANCE_VAR_17, INSTANCE_VAR_29);
    private static final Set<Variable> SET_4 = Set.of(INSTANCE_VAR_41, INSTANCE_VAR_STRING, INSTANCE_VAR_29);

    @Test
    void Is_Equal_To_1() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(WITNESS_VAR_1, SET_1A);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(WITNESS_VAR_1, SET_1A);
        assertEquals(setMembershipGadget1, setMembershipGadget2);
    }

    @Test
    void Is_Equal_To_2() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(WITNESS_VAR_1, SET_1A);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(WITNESS_VAR_1, SET_1B);
        assertEquals(setMembershipGadget1, setMembershipGadget2);
    }

    @Test
    void Is_Not_Equal_To_1() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(WITNESS_VAR_1, SET_2);
        assertNotEquals(setMembershipGadget1, setMembershipGadget2);
    }

    @Test
    void Is_Not_Equal_To_2() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(INSTANCE_VAR_17, SET_3);
        assertNotEquals(setMembershipGadget1, setMembershipGadget2);
    }

    @Test
    void Is_Not_Equal_To_3() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        assertFalse(setMembershipGadget.equals(null));
    }

    @Test
    void Self_Contained_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(INSTANCE_VAR_29, SET_2);
        assertEquals(of(trueProposition()), removeSelfContained(setMembershipGadget));
    }

    @Test
    void Self_Contained_No_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        assertEquals(empty(), removeSelfContained(setMembershipGadget));
    }

    @Test
    void Equality_Contained_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_29);
        assertEquals(of(trueProposition()), removeEqualityContained(setMembershipGadget, equalityGadget));
    }

    @Test
    void Equality_Contained_No_Substitution_1() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        assertEquals(empty(), removeEqualityContained(setMembershipGadget, equalityGadget));
    }

    @Test
    void Equality_Contained_No_Substitution_2() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_53);
        assertEquals(empty(), removeEqualityContained(setMembershipGadget, equalityGadget));
    }

    @Test
    void Instance_Equality_Contradiction() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Instance_Equality_No_Contradiction_1() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_53);
        assertDoesNotThrow(() ->
                checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }

    @Test
    void Instance_Equality_No_Contradiction_2() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_2);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        assertDoesNotThrow(() ->
                checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }

    @Test
    void Instance_Equality_No_Contradiction_3() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertDoesNotThrow(() ->
                checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }

    @Test
    void Instance_Equality_No_Contradiction_4() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertDoesNotThrow(() ->
                checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }

    @Test
    void Heterogeneous_Set_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                new SetMembershipGadget(WITNESS_VAR_1, SET_4)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }
}
