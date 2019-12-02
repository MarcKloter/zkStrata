package gadgets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.LessThanGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LessThanGadgetTest {
    private static final Position.Absolute MOCK_POS = Mockito.mock(Position.Absolute.class);

    private static final InstanceVariable INSTANCE_VAR_17 = new InstanceVariable(new Literal(BigInteger.valueOf(17)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_29 = new InstanceVariable(new Literal(BigInteger.valueOf(29)), null, MOCK_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias2", new Selector(List.of("selector2")));
    private static final Reference REF_3 = new Reference(BigInteger.class, "alias3", new Selector(List.of("selector3")));
    private static final Reference REF_4 = new Reference(BigInteger.class, "alias4", new Selector(List.of("selector4")));

    private static final WitnessVariable WITNESS_VAR_1 = new WitnessVariable(REF_1, REF_1, MOCK_POS);
    private static final WitnessVariable WITNESS_VAR_2 = new WitnessVariable(REF_2, REF_2, MOCK_POS);
    private static final WitnessVariable WITNESS_VAR_3 = new WitnessVariable(REF_3, REF_3, MOCK_POS);
    private static final WitnessVariable WITNESS_VAR_4 = new WitnessVariable(REF_4, REF_4, MOCK_POS);

    @BeforeAll
    static void init() {
        Mockito.when(MOCK_POS.getLine()).thenReturn(1);
        Mockito.when(MOCK_POS.getPosition()).thenReturn(0);
        Mockito.when(MOCK_POS.getSource()).thenReturn(EqualityGadgetTest.class.getSimpleName());
        Mockito.when(MOCK_POS.getStatement()).thenReturn("");
        Mockito.when(MOCK_POS.getTarget()).thenReturn("");
    }

    @Test
    void Is_Equal_To() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertTrue(lessThanGadget1.isEqualTo(lessThanGadget2));
    }

    @Test
    void Is_Not_Equal_To() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_3);
        assertFalse(lessThanGadget1.isEqualTo(lessThanGadget2));
    }

    @Test
    void Self_Contradiction() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_1);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                LessThanGadget.checkSelfContradiction(lessThanGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Self_No_Contradiction() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertDoesNotThrow(() -> LessThanGadget.checkSelfContradiction(lessThanGadget));
    }

    @Test
    void Equality_Contradiction() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                LessThanGadget.checkEqualityContradiction(equalityGadget, lessThanGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Equality_No_Contradiction_1() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertDoesNotThrow(() -> LessThanGadget.checkEqualityContradiction(equalityGadget, lessThanGadget));
    }

    @Test
    void Equality_No_Contradiction_2() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_29, WITNESS_VAR_2);
        assertDoesNotThrow(() -> LessThanGadget.checkEqualityContradiction(equalityGadget, lessThanGadget));
    }

    @Test
    void Exposure_Substitution() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_29);
        assertEquals(Collections.emptySet(), LessThanGadget.removeExposedComparison(lessThanGadget, equalityGadget1, equalityGadget2));
    }

    @Test
    void Exposure_No_Substitution() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(Set.of(lessThanGadget), LessThanGadget.removeExposedComparison(lessThanGadget, equalityGadget1, equalityGadget2));
    }

    @Test
    void Imply_Transitivity_1() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_2, WITNESS_VAR_3);
        LessThanGadget implication = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_3);

        Optional<Gadget> result = LessThanGadget.implyTransitivity(lessThanGadget1, lessThanGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LessThanGadget);
        assertTrue(implication.isEqualTo((LessThanGadget) result.get()));
    }

    @Test
    void Imply_Transitivity_2() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_2);
        LessThanGadget implication = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_1);

        Optional<Gadget> result = LessThanGadget.implyTransitivity(lessThanGadget1, lessThanGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LessThanGadget);
        assertTrue(implication.isEqualTo((LessThanGadget) result.get()));
    }

    @Test
    void Imply_No_Transitivity() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_2);

        Optional<Gadget> result = LessThanGadget.implyTransitivity(lessThanGadget1, lessThanGadget2);
        assertTrue(result.isEmpty());
    }

    @Test
    void Imply_Equality_1() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_3, WITNESS_VAR_2);
        LessThanGadget implication = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_1);

        Optional<Gadget> result = LessThanGadget.implyEquality(lessThanGadget, equalityGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LessThanGadget);
        assertTrue(implication.isEqualTo((LessThanGadget) result.get()));
    }

    @Test
    void Imply_Equality_2() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_3, WITNESS_VAR_2);
        LessThanGadget implication = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_3);

        Optional<Gadget> result = LessThanGadget.implyEquality(lessThanGadget, equalityGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LessThanGadget);
        assertTrue(implication.isEqualTo((LessThanGadget) result.get()));
    }

    @Test
    void Imply_Equality_None() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_3, WITNESS_VAR_4);

        Optional<Gadget> result = LessThanGadget.implyEquality(lessThanGadget, equalityGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Imply_Bounds_1() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_29);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_1, null, INSTANCE_VAR_29);

        Optional<Gadget> result = LessThanGadget.implyBounds(lessThanGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertTrue(implication.isEqualTo((BoundsCheckGadget) result.get()));
    }

    @Test
    void Imply_Bounds_2() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, null);

        Optional<Gadget> result = LessThanGadget.implyBounds(lessThanGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertTrue(implication.isEqualTo((BoundsCheckGadget) result.get()));
    }

    @Test
    void Imply_Bounds_None() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);

        Optional<Gadget> result = LessThanGadget.implyBounds(lessThanGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }
}
