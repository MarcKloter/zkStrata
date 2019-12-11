package conjunctions;

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
import zkstrata.optimizer.TrueProposition;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static zkstrata.utils.TestHelper.*;

public class OrConjunctionTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);

    private static final TrueProposition TRUE_PROPOSITION = new TrueProposition();
    private static final EqualityGadget EQUALITY_GADGET = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
    private static final LessThanGadget LESS_THAN_GADGET = new LessThanGadget(WITNESS_VAR_1, WITNESS_VAR_2);
    private static final InequalityGadget INEQUALITY_GADGET = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);
    private static final BoundsCheckGadget BOUNDS_CHECK_GADGET = new BoundsCheckGadget(WITNESS_VAR_1, INSTANCE_VAR_17, INSTANCE_VAR_41);

    @Test
    void Remove_Tautology_Substitution() {
        OrConjunction orConjunction = new OrConjunction(List.of(
                EQUALITY_GADGET,
                INEQUALITY_GADGET,
                TRUE_PROPOSITION,
                LESS_THAN_GADGET
        ));
        assertEquals(Optional.of(Proposition.trueProposition()), OrConjunction.removeTautology(orConjunction));
    }

    @Test
    void Remove_Tautology_No_Substitution() {
        OrConjunction orConjunction = new OrConjunction(List.of(
                EQUALITY_GADGET,
                INEQUALITY_GADGET,
                LESS_THAN_GADGET
        ));
        assertEquals(Optional.empty(), OrConjunction.removeTautology(orConjunction));
    }

    @Test
    void Lift_Up_Substitution_1() {
        OrConjunction orConjunction = new OrConjunction(List.of(
                new AndConjunction(List.of(EQUALITY_GADGET, INEQUALITY_GADGET)),
                new AndConjunction(List.of(LESS_THAN_GADGET, EQUALITY_GADGET))
        ));
        AndConjunction expected = new AndConjunction(List.of(
                EQUALITY_GADGET,
                new OrConjunction(List.of(INEQUALITY_GADGET, LESS_THAN_GADGET))
        ));
        assertEquals(Optional.of(expected), OrConjunction.liftUpCommonPropositions(orConjunction));
    }

    @Test
    void Lift_Up_Substitution_2() {
        OrConjunction orConjunction = new OrConjunction(List.of(
                new AndConjunction(List.of(EQUALITY_GADGET, BOUNDS_CHECK_GADGET, INEQUALITY_GADGET, LESS_THAN_GADGET)),
                new AndConjunction(List.of(LESS_THAN_GADGET, BOUNDS_CHECK_GADGET, EQUALITY_GADGET)),
                new AndConjunction(List.of(BOUNDS_CHECK_GADGET, EQUALITY_GADGET))
        ));
        AndConjunction expected = new AndConjunction(List.of(
                EQUALITY_GADGET,
                BOUNDS_CHECK_GADGET
        ));
        assertEquals(Optional.of(expected), OrConjunction.liftUpCommonPropositions(orConjunction));
    }

    @Test
    void Lift_Up_No_Substitution() {
        OrConjunction orConjunction = new OrConjunction(List.of(
                new AndConjunction(List.of(EQUALITY_GADGET, INEQUALITY_GADGET)),
                new AndConjunction(List.of(BOUNDS_CHECK_GADGET, LESS_THAN_GADGET))
        ));
        assertEquals(Optional.empty(), OrConjunction.liftUpCommonPropositions(orConjunction));
    }
}
