package conjunctions;

import org.junit.jupiter.api.Test;
import zkstrata.domain.Proposition;
import zkstrata.domain.conjunctions.AbstractConjunction;
import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.domain.gadgets.impl.InequalityGadget;
import zkstrata.optimizer.TrueProposition;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static zkstrata.utils.TestHelper.*;

public class AndConjunctionTest {
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);

    private static final TrueProposition TRUE_PROPOSITION = new TrueProposition();
    private static final EqualityGadget EQUALITY_GADGET = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
    private static final InequalityGadget INEQUALITY_GADGET = new InequalityGadget(WITNESS_VAR_2, INSTANCE_VAR_41);

    @Test
    void Is_Equal_To() {
        AndConjunction andConjunction1 = new AndConjunction(List.of(EQUALITY_GADGET, INEQUALITY_GADGET));
        AndConjunction andConjunction2 = new AndConjunction(List.of(INEQUALITY_GADGET, EQUALITY_GADGET));
        assertEquals(andConjunction1, andConjunction2);
    }

    @Test
    void Is_Not_Equal_To_1() {
        AndConjunction andConjunction = new AndConjunction(List.of(EQUALITY_GADGET, INEQUALITY_GADGET));
        assertFalse(andConjunction.equals(null));
    }

    @Test
    void Is_Not_Equal_To_2() {
        AndConjunction andConjunction1 = new AndConjunction(List.of(EQUALITY_GADGET, INEQUALITY_GADGET));
        AndConjunction andConjunction2 = new AndConjunction(List.of(EQUALITY_GADGET));
        assertNotEquals(andConjunction1, andConjunction2);
        assertNotEquals(andConjunction2, andConjunction1);
    }

    @Test
    void Remove_Tautology_Substitution_1() {
        AndConjunction andConjunction = new AndConjunction(List.of(TRUE_PROPOSITION, TRUE_PROPOSITION, TRUE_PROPOSITION));
        assertEquals(Optional.of(Proposition.trueProposition()), AndConjunction.removeTautology(andConjunction));
    }

    @Test
    void Remove_Tautology_Substitution_2() {
        AndConjunction andConjunction = new AndConjunction(List.of(TRUE_PROPOSITION));
        assertEquals(Optional.of(Proposition.trueProposition()), AndConjunction.removeTautology(andConjunction));
    }

    @Test
    void Remove_Tautology_No_Substitution() {
        AndConjunction andConjunction = new AndConjunction(List.of(EQUALITY_GADGET, INEQUALITY_GADGET));
        assertEquals(Optional.empty(), AndConjunction.removeTautology(andConjunction));
    }

    @Test
    void Lift_Up_Substitution() {
        AndConjunction andConjunction = new AndConjunction(List.of(INEQUALITY_GADGET));
        assertEquals(Optional.of(INEQUALITY_GADGET), AndConjunction.liftUpSinglePart(andConjunction));
    }

    @Test
    void Lift_Up_No_Substitution() {
        AndConjunction andConjunction = new AndConjunction(List.of(
                EQUALITY_GADGET,
                INEQUALITY_GADGET
        ));
        assertEquals(Optional.empty(), AndConjunction.liftUpSinglePart(andConjunction));
    }

    @Test
    void Remove_Duplicate_Contradiction() {
        AndConjunction andConjunction1 = new AndConjunction(List.of(EQUALITY_GADGET, INEQUALITY_GADGET));
        AndConjunction andConjunction2 = new AndConjunction(List.of(INEQUALITY_GADGET, EQUALITY_GADGET));
        Optional<Proposition> actual = AbstractConjunction.removeDuplicateConjunction(andConjunction1, andConjunction2);
        assertEquals(Optional.of(andConjunction1), actual);
    }
}
