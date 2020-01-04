package analysis;

import org.junit.jupiter.api.Test;
import zkstrata.analysis.Inference;
import zkstrata.analysis.SemanticAnalyzer;
import zkstrata.domain.Proposition;
import zkstrata.domain.Statement;
import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class SemanticAnalyzerTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class, 2);
    private static final WitnessVariable WITNESS_VAR_3 = createWitnessVariable(BigInteger.class, 3);
    private static final WitnessVariable WITNESS_VAR_4 = createWitnessVariable(BigInteger.class, 4);

    @Test
    void Complex_Contradiction_Should_Throw_1() {
        EqualityGadget eq1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget eq2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget eq3 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_3);
        EqualityGadget eq4 = new EqualityGadget(WITNESS_VAR_3, WITNESS_VAR_4);
        BoundsCheckGadget bc = new BoundsCheckGadget(WITNESS_VAR_4, INSTANCE_VAR_29, INSTANCE_VAR_41);

        Proposition claim = new AndConjunction(List.of(eq1, eq2, eq3, eq4, bc));

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                SemanticAnalyzer.process(new Statement(claim, Proposition.trueProposition(), Proposition.trueProposition()))
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Complex_Contradiction_Should_Throw_2() {
        EqualityGadget eq1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget eq2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget eq3 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_3);
        EqualityGadget eq4 = new EqualityGadget(WITNESS_VAR_3, WITNESS_VAR_4);
        BoundsCheckGadget bc = new BoundsCheckGadget(WITNESS_VAR_4, INSTANCE_VAR_29, INSTANCE_VAR_41);

        Proposition claim = new AndConjunction(List.of(eq1, bc));
        Proposition premises = new AndConjunction(List.of(new AndConjunction(List.of(eq2, eq4)), eq3));

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                SemanticAnalyzer.process(new Statement(claim, premises, Proposition.trueProposition()))
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }
    @Test
    void Inference_Equals_1() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        Inference inference = new Inference(Set.of(equalityGadget), equalityGadget, null);
        assertFalse(inference.equals(null));
    }

    @Test
    void Inference_Equals_2() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        Inference inference = new Inference(Set.of(equalityGadget), equalityGadget, null);
        assertFalse(inference.equals(new Object()));
    }

    @Test
    void Inference_Equals_3() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        Inference inference1 = new Inference(null, equalityGadget, null);
        Inference inference2 = new Inference(null, equalityGadget, null);
        assertNotEquals(inference1, inference2);
    }

    @Test
    void Inference_Equals_4() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        Inference inference1 = new Inference(Set.of(equalityGadget), null, null);
        Inference inference2 = new Inference(Set.of(equalityGadget), null, null);
        assertNotEquals(inference1, inference2);
    }

    @Test
    void Inference_Equals_5() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        Inference inference1 = new Inference(Set.of(equalityGadget), equalityGadget, null);
        BoundsCheckGadget boundsCheckGadget = new BoundsCheckGadget(WITNESS_VAR_4, INSTANCE_VAR_29, INSTANCE_VAR_41);
        Inference inference2 = new Inference(Set.of(boundsCheckGadget), boundsCheckGadget, null);
        assertNotEquals(inference1, inference2);
    }

    @Test
    void Inference_Equals_6() {
        EqualityGadget equalityGadget = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        Inference inference1 = new Inference(Set.of(equalityGadget), equalityGadget, null);
        Inference inference2 = new Inference(Set.of(equalityGadget), equalityGadget, null);
        assertEquals(inference1, inference2);
    }
}
