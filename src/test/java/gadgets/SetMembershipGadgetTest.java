package gadgets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.SetMembershipGadget;
import zkstrata.exceptions.Position;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SetMembershipGadgetTest {
    private static final Position.Absolute MOCK_POS = Mockito.mock(Position.Absolute.class);

    private static final InstanceVariable INSTANCE_VAR_17 = new InstanceVariable(new Literal(BigInteger.valueOf(17)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_29 = new InstanceVariable(new Literal(BigInteger.valueOf(29)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_41 = new InstanceVariable(new Literal(BigInteger.valueOf(41)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_53 = new InstanceVariable(new Literal(BigInteger.valueOf(53)), null, MOCK_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias2", new Selector(List.of("selector2")));

    private static final WitnessVariable WITNESS_VAR_1 = new WitnessVariable(REF_1, REF_1, MOCK_POS);
    private static final WitnessVariable WITNESS_VAR_2 = new WitnessVariable(REF_2, REF_2, MOCK_POS);

    private static final Set<Variable> SET_1A = Set.of(INSTANCE_VAR_17, WITNESS_VAR_2, INSTANCE_VAR_41);
    private static final Set<Variable> SET_1B = Set.of(WITNESS_VAR_2, INSTANCE_VAR_41, INSTANCE_VAR_17);
    private static final Set<Variable> SET_2 = Set.of(INSTANCE_VAR_41, WITNESS_VAR_1, INSTANCE_VAR_29, WITNESS_VAR_2);
    private static final Set<Variable> SET_3 = Set.of(INSTANCE_VAR_41, INSTANCE_VAR_17, INSTANCE_VAR_29);

    @BeforeAll
    static void init() {
        Mockito.when(MOCK_POS.getLine()).thenReturn(1);
        Mockito.when(MOCK_POS.getPosition()).thenReturn(0);
        Mockito.when(MOCK_POS.getSource()).thenReturn(EqualityGadgetTest.class.getSimpleName());
        Mockito.when(MOCK_POS.getStatement()).thenReturn("");
        Mockito.when(MOCK_POS.getTarget()).thenReturn("");
    }

    @Test
    void Is_Equal_To_1() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(WITNESS_VAR_1, SET_1A);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(WITNESS_VAR_1, SET_1A);
        assertTrue(setMembershipGadget1.isEqualTo(setMembershipGadget2));
    }

    @Test
    void Is_Equal_To_2() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(WITNESS_VAR_1, SET_1A);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(WITNESS_VAR_1, SET_1B);
        assertTrue(setMembershipGadget1.isEqualTo(setMembershipGadget2));
    }

    @Test
    void Is_Not_Equal_To_1() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(WITNESS_VAR_1, SET_2);
        assertFalse(setMembershipGadget1.isEqualTo(setMembershipGadget2));
    }

    @Test
    void Is_Not_Equal_To_2() {
        SetMembershipGadget setMembershipGadget1 = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        SetMembershipGadget setMembershipGadget2 = new SetMembershipGadget(INSTANCE_VAR_17, SET_3);
        assertFalse(setMembershipGadget1.isEqualTo(setMembershipGadget2));
    }

    @Test
    void Self_Contained_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(INSTANCE_VAR_29, SET_2);
        assertEquals(Collections.emptySet(), SetMembershipGadget.removeSelfContained(setMembershipGadget));
    }

    @Test
    void Self_Contained_No_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(INSTANCE_VAR_17, SET_2);
        assertEquals(Set.of(setMembershipGadget), SetMembershipGadget.removeSelfContained(setMembershipGadget));
    }

    @Test
    void Equality_Contained_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_29);
        assertEquals(Collections.emptySet(), SetMembershipGadget.removeEqualityContained(setMembershipGadget, equalityGadget));
    }

    @Test
    void Equality_Contained_No_Substitution() {
        SetMembershipGadget setMembershipGadget = new SetMembershipGadget(WITNESS_VAR_1, SET_3);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        assertEquals(Set.of(setMembershipGadget), SetMembershipGadget.removeEqualityContained(setMembershipGadget, equalityGadget));
    }
}
