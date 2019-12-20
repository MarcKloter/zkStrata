package optimizer;

import org.junit.jupiter.api.Test;
import zkstrata.domain.Proposition;
import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.conjunctions.OrConjunction;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.InequalityGadget;
import zkstrata.domain.gadgets.impl.LessThanGadget;
import zkstrata.optimizer.Optimizer;

import java.math.BigInteger;
import java.util.List;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class OptimizerTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);
    private static final WitnessVariable WITNESS_VAR_3 = createWitnessVariable(BigInteger.class, 3);
    private static final WitnessVariable WITNESS_VAR_4 = createWitnessVariable(BigInteger.class, 4);

    private static final EqualityGadget EQUALITY_GADGET_1 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
    private static final EqualityGadget EQUALITY_GADGET_2 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_3);
    private static final EqualityGadget EQUALITY_GADGET_3 = new EqualityGadget(WITNESS_VAR_4, WITNESS_VAR_3);
    private static final LessThanGadget LESS_THAN_GADGET_1 = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
    private static final InequalityGadget INEQUALITY_GADGET_1 = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_29);
    private static final BoundsCheckGadget BOUNDS_CHECK_GADGET_1 = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);
    private static final BoundsCheckGadget BOUNDS_CHECK_GADGET_2 = new BoundsCheckGadget(WITNESS_VAR_4, INSTANCE_VAR_17, INSTANCE_VAR_41);

    /**
     * Check whether the {@link EqualityGadget}, which is contained in all parts of the {@link OrConjunction}, is lifted
     * up.
     */
    @Test
    void Optimization_Test_1() {
        Proposition claim = new AndConjunction(List.of(
                EQUALITY_GADGET_1,
                new OrConjunction(List.of(
                        new AndConjunction(List.of(EQUALITY_GADGET_1, LESS_THAN_GADGET_1)),
                        new AndConjunction(List.of(EQUALITY_GADGET_1, INEQUALITY_GADGET_1))
                ))
        ));
        Proposition result = new Optimizer(Proposition.trueProposition()).process(claim);
        Proposition expected = new AndConjunction(List.of(
                EQUALITY_GADGET_1,
                new OrConjunction(List.of(
                        LESS_THAN_GADGET_1,
                        INEQUALITY_GADGET_1
                ))
        ));
        assertEquals(expected, result);
    }

    /**
     * Check whether the one of the duplicate {@link OrConjunction} get removed and the {@link EqualityGadget}, which is
     * contained in all parts of the remaining {@link OrConjunction}, is lifted up.
     */
    @Test
    void Optimization_Test_2() {
        Proposition claim = new AndConjunction(List.of(
                new OrConjunction(List.of(
                        new AndConjunction(List.of(EQUALITY_GADGET_1, LESS_THAN_GADGET_1)),
                        new AndConjunction(List.of(EQUALITY_GADGET_1, INEQUALITY_GADGET_1))
                )),
                new OrConjunction(List.of(
                        new AndConjunction(List.of(EQUALITY_GADGET_1, LESS_THAN_GADGET_1)),
                        new AndConjunction(List.of(EQUALITY_GADGET_1, INEQUALITY_GADGET_1))
                ))
        ));
        Proposition result = new Optimizer(Proposition.trueProposition()).process(claim);
        Proposition expected = new AndConjunction(List.of(
                EQUALITY_GADGET_1,
                new OrConjunction(List.of(
                        LESS_THAN_GADGET_1,
                        INEQUALITY_GADGET_1
                ))
        ));
        assertEquals(expected, result);
    }

    /**
     * Check whether the one of the two bounds checks get removed, as they can be implicated from each other.
     */
    @Test
    void Optimization_Test_3() {
        Proposition claim = new AndConjunction(List.of(
                BOUNDS_CHECK_GADGET_1,
                EQUALITY_GADGET_1,
                EQUALITY_GADGET_2,
                EQUALITY_GADGET_3,
                BOUNDS_CHECK_GADGET_2
        ));
        Proposition result = new Optimizer(Proposition.trueProposition()).process(claim);
        Proposition expected = new AndConjunction(List.of(
                EQUALITY_GADGET_1,
                EQUALITY_GADGET_2,
                EQUALITY_GADGET_3,
                BOUNDS_CHECK_GADGET_1
        ));
        assertEquals(expected, result);
    }

    /**
     * This {@link OrConjunction} should be removed as its first part makes it a tautology.
     */
    @Test
    void Optimization_Test_4() {
        Proposition claim = new AndConjunction(List.of(
                new OrConjunction(List.of(
                        EQUALITY_GADGET_1,
                        new AndConjunction(List.of(LESS_THAN_GADGET_1, INEQUALITY_GADGET_1))
                )),
                EQUALITY_GADGET_1
        ));
        Proposition result = new Optimizer(Proposition.trueProposition()).process(claim);
        assertEquals(EQUALITY_GADGET_1, result);
    }

    /**
     * Check whether the {@link InequalityGadget} and {@link BoundsCheckGadget}, which are contained in all parts of the
     * {@link OrConjunction}, are lifted up, causing the {@link OrConjunction} to become a tautology and be removed.
     */
    @Test
    void Optimization_Test_5() {
        Proposition claim = new OrConjunction(List.of(
                new AndConjunction(List.of(INEQUALITY_GADGET_1, BOUNDS_CHECK_GADGET_1, LESS_THAN_GADGET_1)),
                new AndConjunction(List.of(EQUALITY_GADGET_1, BOUNDS_CHECK_GADGET_1, INEQUALITY_GADGET_1)),
                new AndConjunction(List.of(BOUNDS_CHECK_GADGET_1, INEQUALITY_GADGET_1))
        ));
        Proposition result = new Optimizer(Proposition.trueProposition()).process(claim);
        Proposition expected = new AndConjunction(List.of(BOUNDS_CHECK_GADGET_1, INEQUALITY_GADGET_1));
        assertEquals(expected, result);
    }

    /**
     * Check whether implications that can be drawn together with premises are being replaced in the given claim.
     */
    @Test
    void Optimization_Test_6() {
        Proposition premise = new AndConjunction(List.of(BOUNDS_CHECK_GADGET_1, EQUALITY_GADGET_2, EQUALITY_GADGET_3));
        Proposition claim = new AndConjunction(List.of(EQUALITY_GADGET_1, BOUNDS_CHECK_GADGET_2));
        Proposition result = new Optimizer(premise).process(claim);
        assertEquals(EQUALITY_GADGET_1, result);
    }

    /**
     * Check whether implications that can be drawn together with premises are being replaced in the given claim.
     */
    @Test
    void Optimization_Test_7() {
        Proposition premise = new AndConjunction(List.of(EQUALITY_GADGET_1, EQUALITY_GADGET_2, EQUALITY_GADGET_3));
        Proposition claim = new AndConjunction(List.of(BOUNDS_CHECK_GADGET_1, BOUNDS_CHECK_GADGET_2));
        Proposition result = new Optimizer(premise).process(claim);
        assertEquals(BOUNDS_CHECK_GADGET_1, result);
    }

    /**
     * Check whether implications that can be drawn from premises are being replaced in the given claim.
     */
    @Test
    void Optimization_Test_8() {
        Proposition premise = new AndConjunction(List.of(EQUALITY_GADGET_1, EQUALITY_GADGET_2, EQUALITY_GADGET_3));
        Proposition claim = new AndConjunction(List.of(EQUALITY_GADGET_1, EQUALITY_GADGET_2));
        Proposition result = new Optimizer(premise).process(claim);
        assertEquals(Proposition.trueProposition(), result);
    }
}
