package gadgets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.MiMCHashGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;
import zkstrata.utils.Constants;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MiMCHashGadgetTest {
    private static final Position.Absolute MOCK_POS = Mockito.mock(Position.Absolute.class);

    private static final String IMAGE_1 = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
    private static final String IMAGE_2 = "0x0cf73df10b141c015cc31bd84798e506529c8c3d2c8a7b0f97b5259656bdcacb";

    private static final InstanceVariable INSTANCE_VAR_1 = new InstanceVariable(new HexLiteral(IMAGE_1), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_2 = new InstanceVariable(new HexLiteral(IMAGE_2), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_NEG = new InstanceVariable(new HexLiteral(BigInteger.valueOf(-5)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_LARGE = new InstanceVariable(new HexLiteral(Constants.ED25519_PRIME_ORDER), null, MOCK_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias2", new Selector(List.of("selector2")));

    private static final WitnessVariable WITNESS_VAR_1 = new WitnessVariable(REF_1, REF_1, null);
    private static final WitnessVariable WITNESS_VAR_2 = new WitnessVariable(REF_2, REF_2, null);

    @BeforeAll
    static void init() {
        Mockito.when(MOCK_POS.getLine()).thenReturn(1);
        Mockito.when(MOCK_POS.getPosition()).thenReturn(0);
        Mockito.when(MOCK_POS.getSource()).thenReturn(EqualityGadgetTest.class.getSimpleName());
        Mockito.when(MOCK_POS.getStatement()).thenReturn("");
        Mockito.when(MOCK_POS.getTarget()).thenReturn("");
    }

    @Test
    void Image_Too_Large() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
            new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_LARGE)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("invalid mimc-hash image"));
    }

    @Test
    void Image_Negative() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
            new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_NEG)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("invalid mimc-hash image"));
    }

    @Test
    void Is_Equal_To() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        assertEquals(miMCHashGadget1, miMCHashGadget2);
    }

    @Test
    void Is_Not_Equal_To_1() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_2, INSTANCE_VAR_1);
        assertNotEquals(miMCHashGadget1, miMCHashGadget2);
    }

    @Test
    void Is_Not_Equal_To_2() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_2);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        assertNotEquals(miMCHashGadget1, miMCHashGadget2);
    }

    @Test
    void Same_Preimage_Different_Image_Contradiction() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_2);
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
            MiMCHashGadget.checkContradiction(miMCHashGadget1, miMCHashGadget2)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Same_Preimage_Different_Image_Confidentiality_No_Contradiction() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertDoesNotThrow(() -> {
            MiMCHashGadget.checkContradiction(miMCHashGadget1, miMCHashGadget2);
            MiMCHashGadget.checkContradiction(miMCHashGadget2, miMCHashGadget1);
        });
    }

    @Test
    void Equal_No_Contradiction() {
        MiMCHashGadget miMCHashGadget = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        assertDoesNotThrow(() ->
            MiMCHashGadget.checkContradiction(miMCHashGadget, miMCHashGadget)
        );
    }

    @Test
    void Different_Preimage_No_Contradiction() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_2, INSTANCE_VAR_1);
        assertDoesNotThrow(() -> {
            MiMCHashGadget.checkContradiction(miMCHashGadget1, miMCHashGadget2);
            MiMCHashGadget.checkContradiction(miMCHashGadget2, miMCHashGadget1);
        });
    }
}
