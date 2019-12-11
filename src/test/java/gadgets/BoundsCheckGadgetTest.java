package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.Optional;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class BoundsCheckGadgetTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));
    private static final InstanceVariable INSTANCE_VAR_53 = createInstanceVariable(new Literal(BigInteger.valueOf(53)));

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
    void Imply_Bounds_Left() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = BoundsCheckGadget.implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Bounds_Upper() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = BoundsCheckGadget.implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertEquals(implication, result.get());
    }

    @Test
    void Imply_Bounds_None() {
        EqualityGadget equalityGadget = new EqualityGadget(INSTANCE_VAR_17, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = BoundsCheckGadget.implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isEmpty());
    }

    @Test
    void Single_Gadget_Contradiction() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_29);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                BoundsCheckGadget.checkSelfBoundsContradiction(boundsCheckGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Single_Gadget_No_Contradiction() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);

        assertDoesNotThrow(() ->
                BoundsCheckGadget.checkSelfBoundsContradiction(boundsCheckGadget)
        );
    }

    @Test
    void Two_Gadget_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                BoundsCheckGadget.checkTwoBoundsChecksContradiction(boundsCheckGadget1, boundsCheckGadget2)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Two_Gadget_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                BoundsCheckGadget.checkTwoBoundsChecksContradiction(boundsCheckGadget1, boundsCheckGadget2)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Two_Gadget_No_Contradiction() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_53);

        assertDoesNotThrow(() -> {
            BoundsCheckGadget.checkTwoBoundsChecksContradiction(boundsCheckGadget1, boundsCheckGadget2);
            BoundsCheckGadget.checkTwoBoundsChecksContradiction(boundsCheckGadget2, boundsCheckGadget1);
        });
    }

    @Test
    void Equality_Bounds_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                BoundsCheckGadget.checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Equality_Bounds_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_53);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                BoundsCheckGadget.checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Two_Gadget_No_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);

        assertDoesNotThrow(() ->
                BoundsCheckGadget.checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget)
        );
    }

    @Test
    void Two_Gadget_No_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);

        assertDoesNotThrow(() ->
                BoundsCheckGadget.checkEqualityBoundsContradiction(equalityGadget, boundsCheckGadget)
        );
    }

    @Test
    void Equality_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_17);
        EqualityGadget substitution = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        assertEquals(Optional.of(substitution), BoundsCheckGadget.replaceEquality1(boundsCheckGadget));
    }

    @Test
    void Equality_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);
        EqualityGadget substitution = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_41);
        assertEquals(Optional.of(substitution), BoundsCheckGadget.replaceEquality2(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Equality_Substitution_3() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        EqualityGadget substitution = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_29);
        assertEquals(Optional.of(substitution), BoundsCheckGadget.replaceEquality2(boundsCheckGadget1, boundsCheckGadget2));
    }

    @Test
    void Equality_No_Substitution_1() {
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        assertEquals(Optional.empty(), BoundsCheckGadget.replaceEquality1(boundsCheckGadget));
    }

    @Test
    void Equality_No_Substitution_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_29, INSTANCE_VAR_41);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_29);
        assertEquals(Optional.empty(), BoundsCheckGadget.replaceEquality2(boundsCheckGadget1, boundsCheckGadget2));
    }
}
