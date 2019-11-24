package gadgets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.InequalityGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InequalityGadgetTest {
    private static final Position.Absolute MOCK_POS = Mockito.mock(Position.Absolute.class);

    private static final InstanceVariable INSTANCE_VAR_17 = new InstanceVariable(new Literal(BigInteger.valueOf(17)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_41 = new InstanceVariable(new Literal(BigInteger.valueOf(41)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_STRING = new InstanceVariable(new Literal("String"), null, MOCK_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias2", new Selector(List.of("selector2")));
    private static final WitnessVariable WITNESS_VAR_1 = new WitnessVariable(REF_1, REF_1, MOCK_POS);
    private static final WitnessVariable WITNESS_VAR_2 = new WitnessVariable(REF_2, REF_2, MOCK_POS);

    @BeforeAll
    static void init() {
        Mockito.when(MOCK_POS.getLine()).thenReturn(1);
        Mockito.when(MOCK_POS.getPosition()).thenReturn(0);
        Mockito.when(MOCK_POS.getSource()).thenReturn(EqualityGadgetTest.class.getSimpleName());
        Mockito.when(MOCK_POS.getStatement()).thenReturn("");
        Mockito.when(MOCK_POS.getTarget()).thenReturn("");
    }

    @Test
    void Instance_Unequals_Instance_Contradiction() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, INSTANCE_VAR_41);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                InequalityGadget.checkSelfContradiction(inequalityGadget)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Instance_Unequals_Instance_No_Contradiction() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, INSTANCE_VAR_17);
        assertDoesNotThrow(() -> InequalityGadget.checkSelfContradiction(inequalityGadget));
    }

    @Test
    void Equality_Contradiction_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                InequalityGadget.checkEqualityContradiction(equalityGadget, inequalityGadget)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Equality_Contradiction_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, WITNESS_VAR_2);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                InequalityGadget.checkEqualityContradiction(equalityGadget, inequalityGadget)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Equality_No_Contradiction() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertDoesNotThrow(() -> InequalityGadget.checkEqualityContradiction(equalityGadget, inequalityGadget));
    }

    @Test
    void Instance_Unequals_Instance_Substitution() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, INSTANCE_VAR_17);
        assertEquals(Collections.emptySet(), InequalityGadget.removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(Set.of(inequalityGadget), InequalityGadget.removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        assertEquals(Set.of(inequalityGadget), InequalityGadget.removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Instance_Equals_Instance_No_Substitution_3() {
        InequalityGadget inequalityGadget = new InequalityGadget(INSTANCE_VAR_41, WITNESS_VAR_1);
        assertEquals(Set.of(inequalityGadget), InequalityGadget.removeInstanceUnequalsInstance(inequalityGadget));
    }

    @Test
    void Is_Equal_To_1() {
        InequalityGadget inequalityGadget1 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        InequalityGadget inequalityGadget2 = new InequalityGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        assertTrue(inequalityGadget1.isEqualTo(inequalityGadget2));
    }

    @Test
    void Is_Equal_To_2() {
        InequalityGadget inequalityGadget1 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        InequalityGadget inequalityGadget2 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertTrue(inequalityGadget1.isEqualTo(inequalityGadget2));
    }

    @Test
    void Is_Not_Equal_To() {
        InequalityGadget inequalityGadget1 = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        InequalityGadget inequalityGadget2 = new InequalityGadget(WITNESS_VAR_2, WITNESS_VAR_2);
        assertFalse(inequalityGadget1.isEqualTo(inequalityGadget2));
    }

    @Test
    void Type_Mismatch() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_STRING)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }
}
