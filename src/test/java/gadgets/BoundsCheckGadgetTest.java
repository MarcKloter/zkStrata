package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.InequalityGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static zkstrata.domain.Proposition.trueProposition;
import static zkstrata.domain.gadgets.impl.BoundsCheckGadget.*;
import static zkstrata.utils.GadgetUtils.*;
import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class BoundsCheckGadgetTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_40 = createInstanceVariable(new Literal(BigInteger.valueOf(40)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));
    private static final InstanceVariable INSTANCE_VAR_53 = createInstanceVariable(new Literal(BigInteger.valueOf(53)));
    private static final InstanceVariable INSTANCE_VAR_STRING = createInstanceVariable(new Literal("String"));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);

    @Test
    void Is_Equal_To() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertEquals(boundsCheckGadget1, boundsCheckGadget2);
    }

    @Test
    void Is_Not_Equal_To_1() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertNotEquals(boundsCheckGadget1, boundsCheckGadget2);
    }

    @Test
    void Is_Not_Equal_To_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertNotEquals(boundsCheckGadget1, boundsCheckGadget2);
    }

    @Test
    void Is_Not_Equal_To_3() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        assertNotEquals(boundsCheckGadget1, boundsCheckGadget2);
    }

    @Test
    void Is_Not_Equal_To_4() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertFalse(boundsCheckGadget.equals(null));
    }

    @Test
    void Imply_Bounds_Left() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Bounds_Upper() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Bounds_None_1() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Imply_Bounds_None_2() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(INSTANCE_VAR_29, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Imply_Equality_Upper() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_17);
        EqualityGadget implication = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_17);

        Optional<Gadget> result = implyEquality(boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof EqualityGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Equality_None() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyEquality(boundsCheckGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Imply_Restriction_Upper() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_40);

        Optional<Gadget> result = implyRestrictedBounds(inequalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Restriction_Lower() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_40);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_40, INSTANCE_VAR_53);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);

        Optional<Gadget> result = implyRestrictedBounds(inequalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Restriction_None_1() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyRestrictedBounds(inequalityGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Imply_Restriction_None_2() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_17);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyRestrictedBounds(inequalityGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Imply_Restriction_None_3() {
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_17);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(INSTANCE_VAR_29, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = implyRestrictedBounds(inequalityGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Single_Gadget_Contradiction() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_29);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkSelfBoundsContradiction(boundsCheckGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Single_Gadget_No_Contradiction() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);

        assertDoesNotThrow(() ->
                checkSelfBoundsContradiction(boundsCheckGadget)
        );
    }

    @Test
    void Two_Gadget_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkTwoBoundsChecksContradiction(boundsCheckGadget1, boundsCheckGadget2)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Two_Gadget_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkTwoBoundsChecksContradiction(boundsCheckGadget1, boundsCheckGadget2)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Two_Gadget_No_Contradiction() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);

        assertDoesNotThrow(() -> {
            checkTwoBoundsChecksContradiction(boundsCheckGadget1, boundsCheckGadget2);
            checkTwoBoundsChecksContradiction(boundsCheckGadget2, boundsCheckGadget1);
        });
    }

    @Test
    void Instance_Contradiction() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(INSTANCE_VAR_17, INSTANCE_VAR_41, INSTANCE_VAR_53);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkInstanceContradiction(boundsCheckGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Instance_No_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);

        assertDoesNotThrow(() -> checkInstanceContradiction(boundsCheckGadget));
    }

    @Test
    void Instance_No_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(INSTANCE_VAR_41, INSTANCE_VAR_29, INSTANCE_VAR_53);

        assertDoesNotThrow(() -> checkInstanceContradiction(boundsCheckGadget));
    }

    @Test
    void Equality_Bounds_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Equality_Bounds_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Two_Gadget_No_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);

        assertDoesNotThrow(() -> checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget));
    }

    @Test
    void Two_Gadget_No_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);

        assertDoesNotThrow(() -> checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget));
    }

    @Test
    void Two_Gadget_No_Contradiction_3() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);

        assertDoesNotThrow(() -> checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget));
    }

    @Test
    void Two_Gadget_No_Contradiction_4() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);

        assertDoesNotThrow(() -> checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget));
    }

    @Test
    void Unequal_Bounds_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        BoundsCheckGadget substitution = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, subtractOne(INSTANCE_VAR_41));
        assertEquals(of(substitution), replaceUnequalBounds(boundsCheckGadget, inequalityGadget));
    }

    @Test
    void Unequal_Bounds_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        BoundsCheckGadget substitution = new BoundsCheckGadget(WITNESS_VAR_1, addOne(INSTANCE_VAR_17), INSTANCE_VAR_41);
        assertEquals(of(substitution), replaceUnequalBounds(boundsCheckGadget, inequalityGadget));
    }

    @Test
    void Unequal_Bounds_No_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        assertEquals(empty(), replaceUnequalBounds(boundsCheckGadget, inequalityGadget));
    }

    @Test
    void Unequal_Bounds_No_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);
        assertEquals(empty(), replaceUnequalBounds(boundsCheckGadget, inequalityGadget));
    }

    @Test
    void Unequal_Bounds_No_Substitution_3() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertEquals(empty(), replaceUnequalBounds(boundsCheckGadget, inequalityGadget));
    }

    @Test
    void Unequal_Bounds_No_Substitution_4() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(INSTANCE_VAR_29, INSTANCE_VAR_17, INSTANCE_VAR_41);
        InequalityGadget inequalityGadget = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
        assertEquals(empty(), replaceUnequalBounds(boundsCheckGadget, inequalityGadget));
    }

    @Test
    void Value_Equality_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_41, WITNESS_VAR_1);
        assertEquals(of(trueProposition()), removeValueEquality(boundsCheckGadget, equalityGadget));
    }

    @Test
    void Value_Equality_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_29);
        assertEquals(of(trueProposition()), removeValueEquality(boundsCheckGadget, equalityGadget));
    }

    @Test
    void Value_Equality_No_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_17);
        assertEquals(empty(), removeValueEquality(boundsCheckGadget, equalityGadget));
    }

    @Test
    void Value_Equality_No_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_53);
        assertEquals(empty(), removeValueEquality(boundsCheckGadget, equalityGadget));
    }

    @Test
    void Value_Equality_No_Substitution_3() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(INSTANCE_VAR_29, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_2, INSTANCE_VAR_53);
        assertEquals(empty(), removeValueEquality(boundsCheckGadget, equalityGadget));
    }

    @Test
    void Min_Max_Equality_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_17);
        EqualityGadget substitution = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertEquals(of(substitution), replaceMinEqualsMax(boundsCheckGadget));
    }

    @Test
    void Min_Max_Equality_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);
        EqualityGadget substitution = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        assertEquals(of(substitution), replaceMinEqualsMaxTwoGadgets(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Min_Max_Equality_Substitution_3() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        EqualityGadget substitution = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_29);
        assertEquals(of(substitution), replaceMinEqualsMaxTwoGadgets(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Min_Max_Equality_No_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        assertEquals(empty(), replaceMinEqualsMax(boundsCheckGadget));
    }

    @Test
    void Min_Max_Equality_No_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_29);
        assertEquals(empty(), replaceMinEqualsMaxTwoGadgets(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Instance_Bounds_Substitution() {
        BoundsCheckGadget target = new BoundsCheckGadget(INSTANCE_VAR_29, INSTANCE_VAR_17, INSTANCE_VAR_53);
        assertEquals(of(trueProposition()), removeInstanceComparison(target));
    }

    @Test
    void Instance_Bounds_No_Substitution_1() {
        BoundsCheckGadget target = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_53);
        assertEquals(empty(), removeInstanceComparison(target));
    }

    @Test
    void Instance_Bounds_No_Substitution_2() {
        BoundsCheckGadget target = new BoundsCheckGadget(INSTANCE_VAR_17, INSTANCE_VAR_29, INSTANCE_VAR_53);
        assertEquals(empty(), removeInstanceComparison(target));
    }

    @Test
    void Loose_Bounds_Substitution() {
        BoundsCheckGadget target = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_53);
        BoundsCheckGadget context = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertEquals(of(trueProposition()), removeLooseBounds(target, context));
    }

    @Test
    void Loose_Bounds_No_Substitution_1() {
        BoundsCheckGadget target = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        BoundsCheckGadget context = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        assertEquals(empty(), removeLooseBounds(target, context));
    }

    @Test
    void Loose_Bounds_No_Substitution_2() {
        BoundsCheckGadget target = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget context = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        assertEquals(empty(), removeLooseBounds(target, context));
    }

    @Test
    void Merge_Bounds_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        BoundsCheckGadget substitution = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertEquals(of(substitution), mergeBounds(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Merge_Bounds_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget substitution = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertEquals(of(substitution), mergeBounds(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Merge_Bounds_No_Substitution() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_41);
        assertEquals(empty(), mergeBounds(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Is_Contained_In_Bounds() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        assertTrue(isContainedInBounds(INSTANCE_VAR_41, boundsCheckGadget));
    }

    @Test
    void Is_Contained_In_Bounds_Min() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        assertTrue(isContainedInBounds(INSTANCE_VAR_29, boundsCheckGadget));
    }

    @Test
    void Is_Contained_In_Bounds_Max() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);
        assertTrue(isContainedInBounds(INSTANCE_VAR_53, boundsCheckGadget));
    }

    @Test
    void Is_Not_Contained_In_Bounds_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertFalse(isContainedInBounds(INSTANCE_VAR_17, boundsCheckGadget));
    }

    @Test
    void Is_Not_Contained_In_Bounds_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertFalse(isContainedInBounds(INSTANCE_VAR_53, boundsCheckGadget));
    }

    @Test
    void Is_Not_Contained_In_Bounds_3() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        assertFalse(isContainedInBounds(INSTANCE_VAR_STRING, boundsCheckGadget));
    }
}
