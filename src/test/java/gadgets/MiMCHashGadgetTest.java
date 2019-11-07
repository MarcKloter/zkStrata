package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.MiMCHashGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;
import zkstrata.utils.SemanticsUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MiMCHashGadgetTest {
    private static final Position.Absolute DUMMY_POS = new Position.Absolute("src", "stmt", "t", 1, 1);

    private static final String IMAGE_1 = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
    private static final String IMAGE_2 = "0x0cf73df10b141c015cc31bd84798e506529c8c3d2c8a7b0f97b5259656bdcacb";

    private static final InstanceVariable INSTANCE_VAR_IMAGE_1 = new InstanceVariable(new HexLiteral(IMAGE_1), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_IMAGE_2 = new InstanceVariable(new HexLiteral(IMAGE_2), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_IMAGE_NEG = new InstanceVariable(new HexLiteral(BigInteger.valueOf(-5)), null, DUMMY_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(BigInteger.class, "alias2", new Selector(List.of("selector2")));

    private static final WitnessVariable WITNESS_VAR_1 = new WitnessVariable(REF_1, REF_1, DUMMY_POS);
    private static final WitnessVariable WITNESS_VAR_2 = new WitnessVariable(REF_2, REF_2, DUMMY_POS);

    @Test
    void Image_Too_Large() {
        InstanceVariable instanceVariable = new InstanceVariable(new HexLiteral(SemanticsUtils.ED25519_PRIME_ORDER), null, DUMMY_POS);
        assertThrows(CompileTimeException.class, () -> {
            new MiMCHashGadget(WITNESS_VAR_1, instanceVariable);
        });
    }

    @Test
    void Image_Negative() {
        assertThrows(CompileTimeException.class, () -> {
            new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_IMAGE_NEG);
        });
    }

    @Test
    void Same_Preimage_Different_Image_Contradiction() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_IMAGE_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_IMAGE_2);
        assertThrows(CompileTimeException.class, () -> {
            MiMCHashGadget.checkContradiction(miMCHashGadget1, miMCHashGadget2);
        });
    }

    @Test
    void Same_Preimage_Different_Image_Confidentiality_No_Contradiction() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_IMAGE_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_1, WITNESS_VAR_2);
        assertDoesNotThrow(() -> {
            MiMCHashGadget.checkContradiction(miMCHashGadget1, miMCHashGadget2);
            MiMCHashGadget.checkContradiction(miMCHashGadget2, miMCHashGadget1);
        });
    }

    @Test
    void Different_Preimage_No_Contradiction() {
        MiMCHashGadget miMCHashGadget1 = new MiMCHashGadget(WITNESS_VAR_1, INSTANCE_VAR_IMAGE_1);
        MiMCHashGadget miMCHashGadget2 = new MiMCHashGadget(WITNESS_VAR_2, INSTANCE_VAR_IMAGE_1);
        assertDoesNotThrow(() -> {
            MiMCHashGadget.checkContradiction(miMCHashGadget1, miMCHashGadget2);
            MiMCHashGadget.checkContradiction(miMCHashGadget2, miMCHashGadget1);
        });
    }
}
