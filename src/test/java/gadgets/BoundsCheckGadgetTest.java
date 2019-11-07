package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;
import zkstrata.utils.SemanticsUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BoundsCheckGadgetTest {
    private static final Position.Absolute DUMMY_POS = new Position.Absolute("src", "stmt", "t", 1, 1);

    private static final InstanceVariable INSTANCE_VAR_NEG = new InstanceVariable(new Literal(BigInteger.valueOf(-5)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_17 = new InstanceVariable(new Literal(BigInteger.valueOf(17)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_29 = new InstanceVariable(new Literal(BigInteger.valueOf(29)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_41 = new InstanceVariable(new Literal(BigInteger.valueOf(41)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_53 = new InstanceVariable(new Literal(BigInteger.valueOf(53)), null, DUMMY_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias2", new Selector(List.of("selector2")));
    private static final Reference REF_3 = new Reference(String.class, "alias3", new Selector(List.of("selector3")));

    private static final WitnessVariable WITNESS_VAR_1 = new WitnessVariable(REF_1, REF_1, DUMMY_POS);
    private static final WitnessVariable WITNESS_VAR_2 = new WitnessVariable(REF_2, REF_2, DUMMY_POS);
    private static final WitnessVariable WITNESS_VAR_3 = new WitnessVariable(REF_3, REF_3, DUMMY_POS);

    @Test
    void Invalid_Type_String() {
        assertThrows(CompileTimeException.class, () -> {
            new BoundsCheckGadget(WITNESS_VAR_3, INSTANCE_VAR_NEG, INSTANCE_VAR_17);
        });
    }

    @Test
    void Negative_Lower_Bound() {
        assertThrows(CompileTimeException.class, () -> {
            new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_NEG, INSTANCE_VAR_17);
        });
    }

    @Test
    void Negative_Upper_Bound() {
        assertThrows(CompileTimeException.class, () -> {
            new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_NEG);
        });
    }

    @Test
    void Lower_Bound_Too_Large() {
        InstanceVariable instanceVariable = new InstanceVariable(new Literal(SemanticsUtils.ED25519_PRIME_ORDER), null, DUMMY_POS);
        assertThrows(CompileTimeException.class, () -> {
            new BoundsCheckGadget(WITNESS_VAR_1, instanceVariable, INSTANCE_VAR_17);
        });
    }

    @Test
    void Upper_Bound_Too_Large() {
        InstanceVariable instanceVariable = new InstanceVariable(new Literal(SemanticsUtils.ED25519_PRIME_ORDER), null, DUMMY_POS);
        assertThrows(CompileTimeException.class, () -> {
            new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, instanceVariable);
        });
    }

    @Test
    void Imply_Bounds_Left() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = BoundsCheckGadget.implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertTrue(implication.isEqualTo((BoundsCheckGadget) result.get()));
    }

    @Test
    void Imply_Bounds_Upper() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_2, INSTANCE_VAR_17, INSTANCE_VAR_41);
        BoundsCheckGadget implication = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

        Optional<Gadget> result = BoundsCheckGadget.implyBounds(equalityGadget, boundsCheckGadget);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof BoundsCheckGadget);
        assertTrue(implication.isEqualTo((BoundsCheckGadget) result.get()));
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

        assertThrows(CompileTimeException.class, () -> {
            BoundsCheckGadget.checkContradiction2(boundsCheckGadget);
        });
    }

    @Test
    void Two_Gadget_Contradiction_1() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);

        assertThrows(CompileTimeException.class, () -> {
            BoundsCheckGadget.checkContradiction1(boundsCheckGadget1, boundsCheckGadget2);
        });
    }

    @Test
    void Two_Gadget_Contradiction_2() {
        BoundsCheckGadget boundsCheckGadget1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_29);
        BoundsCheckGadget boundsCheckGadget2 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_41, INSTANCE_VAR_53);

        assertThrows(CompileTimeException.class, () -> {
            BoundsCheckGadget.checkContradiction1(boundsCheckGadget1, boundsCheckGadget2);
        });
    }
}
