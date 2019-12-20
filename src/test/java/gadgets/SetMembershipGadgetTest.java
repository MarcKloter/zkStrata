package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.SetMembershipGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class SetMembershipGadgetTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));
    private static final InstanceVariable INSTANCE_VAR_53 = createInstanceVariable(new Literal(BigInteger.valueOf(53)));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);

    private static final Set<Variable> SET_1A = Set.of(INSTANCE_VAR_17, WITNESS_VAR_2, INSTANCE_VAR_41);
    private static final Set<Variable> SET_1B = Set.of(WITNESS_VAR_2, INSTANCE_VAR_41, INSTANCE_VAR_17);
    private static final Set<Variable> SET_2 = Set.of(INSTANCE_VAR_41, WITNESS_VAR_1, INSTANCE_VAR_29, WITNESS_VAR_2);
    private static final Set<Variable> SET_3 = Set.of(INSTANCE_VAR_41, INSTANCE_VAR_17, INSTANCE_VAR_29);

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
    void Self_Contained_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(INSTANCE_VAR_29, SET_2);
        assertEquals(Optional.of(Proposition.trueProposition()), SetMembershipGadget.removeSelfContained(setMembershipGadget));
    }

    @Test
    void Self_Contained_No_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        assertEquals(Optional.empty(), SetMembershipGadget.removeSelfContained(setMembershipGadget));
    }

    @Test
    void Equality_Contained_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_29);
        assertEquals(Optional.of(Proposition.trueProposition()), SetMembershipGadget.removeEqualityContained(setMembershipGadget, equalityGadget));
    }

    @Test
    void Equality_Contained_No_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        assertEquals(Optional.empty(), SetMembershipGadget.removeEqualityContained(setMembershipGadget, equalityGadget));
    }

    @Test
    void Instance_Equality_Contradiction() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                SetMembershipGadget.checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Instance_Equality_No_Contradiction_1() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_53);
        assertDoesNotThrow(() ->
                SetMembershipGadget.checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }

    @Test
    void Instance_Equality_No_Contradiction_2() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_2);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        assertDoesNotThrow(() ->
                SetMembershipGadget.checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }

    @Test
    void Instance_Equality_No_Contradiction_3() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertDoesNotThrow(() ->
                SetMembershipGadget.checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }

    @Test
    void Instance_Equality_No_Contradiction_4() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertDoesNotThrow(() ->
                SetMembershipGadget.checkInstanceEqualityContradiction(setMembershipGadget, equalityGadget)
        );
    }
}
