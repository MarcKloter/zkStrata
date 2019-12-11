package analysis;

import org.junit.jupiter.api.Test;
import zkstrata.analysis.SemanticAnalyzer;
import zkstrata.domain.Proposition;
import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.EqualityGadget;
import zkstrata.exceptions.CompileTimeException;

import java.math.BigInteger;
import java.util.List;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class SemanticAnalyzerTest {
    private static final InstanceVariable INSTANCE_VAR_17 = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_29 = createInstanceVariable(new Literal(BigInteger.valueOf(29)));
    private static final InstanceVariable INSTANCE_VAR_41 = createInstanceVariable(new Literal(BigInteger.valueOf(41)));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class);
    private static final WitnessVariable WITNESS_VAR_3 = createWitnessVariable(BigInteger.class);
    private static final WitnessVariable WITNESS_VAR_4 = createWitnessVariable(BigInteger.class);

    @Test
    void Complex_Contradiction_Should_Throw_1() {
        EqualityGadget eq1 = new EqualityGadget(WITNESS_VAR_1, INSTANCE_VAR_17);
        EqualityGadget eq2 = new EqualityGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        EqualityGadget eq3 = new EqualityGadget(WITNESS_VAR_2, WITNESS_VAR_3);
        EqualityGadget eq4 = new EqualityGadget(WITNESS_VAR_3, WITNESS_VAR_4);
        BoundsCheckGadget bc = new BoundsCheckGadget(WITNESS_VAR_4, INSTANCE_VAR_29, INSTANCE_VAR_41);

        Proposition claim = new AndConjunction(List.of(eq1, eq2, eq3, eq4, bc));

        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
                SemanticAnalyzer.process(claim, Proposition.trueProposition())
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
                SemanticAnalyzer.process(claim, premises)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }
}
