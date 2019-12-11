package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.LessThanGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.*;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class LessThanGadgetTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);
    private static final WitnessVariable WITNESS_VAR_3 = createWitnessVariable(BigInteger.class, 3);
    private static final WitnessVariable WITNESS_VAR_4 = createWitnessVariable(BigInteger.class, 4);

    @Test
    void Is_Equal_To() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(lessThanGadget1, lessThanGadget2);
    }

    @Test
    void Is_Not_Equal_To() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_3);
        assertNotEquals(lessThanGadget1, lessThanGadget2);
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
        assertEquals(Optional.of(Proposition.trueProposition()),
                LessThanGadget.removeExposedComparison(lessThanGadget, equalityGadget1, equalityGadget2));
    }

    @Test
    void Exposure_No_Substitution() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget equalityGadget2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(Optional.empty(),
                LessThanGadget.removeExposedComparison(lessThanGadget, equalityGadget1, equalityGadget2));
    }

    @Test
    void Imply_Transitivity_1() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_2, WITNESS_VAR_3);
        LessThanGadget implication = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_3);

        Optional<Gadget> result = LessThanGadget.implyTransitivity(lessThanGadget1, lessThanGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LessThanGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Transitivity_2() {
        LessThanGadget lessThanGadget1 = new LessThanGadget(WITNESS_VAR_2, WITNESS_VAR_1);
        LessThanGadget lessThanGadget2 = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_2);
        LessThanGadget implication = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_1);

        Optional<Gadget> result = LessThanGadget.implyTransitivity(lessThanGadget1, lessThanGadget2);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LessThanGadget);
        assertEquals(implication, result.get());
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
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Equality_2() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_3, WITNESS_VAR_2);
        LessThanGadget implication = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_3);

        Optional<Gadget> result = LessThanGadget.implyEquality(lessThanGadget, equalityGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof LessThanGadget);
        assertEquals(implication, result.get());
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
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Bounds_2() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, null);

        Optional<Gadget> result = LessThanGadget.implyBounds(lessThanGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Bounds_None() {
        LessThanGadget lessThanGadget = new LessThanGadget(WITNESS_VAR_3, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);

        Optional<Gadget> result = LessThanGadget.implyBounds(lessThanGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }
}
