package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.MiMCHashGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.Constants;

import java.math.BigInteger;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class MiMCHashGadgetTest {
    private static final String IMAGE_1 = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
    private static final String IMAGE_2 = "0x0cf73df10b141c015cc31bd84798e506529c8c3d2c8a7b0f97b5259656bdcacb";

    private static final InstanceVariable INSTANCE_VAR_1 = createInstanceVariable(new HexLiteral(IMAGE_1));
    private static final InstanceVariable INSTANCE_VAR_2 = createInstanceVariable(new HexLiteral(IMAGE_2));
    private static final InstanceVariable INSTANCE_VAR_NEG = createInstanceVariable(new HexLiteral(BigInteger.valueOf(-5)));
    private static final InstanceVariable INSTANCE_VAR_LARGE = createInstanceVariable(new HexLiteral(Constants.ED25519_PRIME_ORDER));

    private static final WitnessVariable WITNESS_VAR_1 = createWitnessVariable(BigInteger.class);
    private static final WitnessVariable WITNESS_VAR_2 = createWitnessVariable(BigInteger.class);

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
