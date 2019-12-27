package utils;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.utils.GadgetUtils;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static zkstrata.utils.TestHelper.createInstanceVariable;

public class GadgetUtilsTest {
    private static final InstanceVariable INSTANCE_VAR_BIG_INTEGER = createInstanceVariable(new Literal(BigInteger.valueOf(17)));
    private static final InstanceVariable INSTANCE_VAR_STRING = createInstanceVariable(new Literal("String"));

    @Test
    void Assert_Big_Integer_Is_Big_Integer_Should_Succeed() {
        assertDoesNotThrow(() ->
                GadgetUtils.assertIsBigInteger(INSTANCE_VAR_BIG_INTEGER)
        );
    }

    @Test
    void Assert_String_Is_Big_Integer_Should_Throw() {
        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                GadgetUtils.assertIsBigInteger(INSTANCE_VAR_STRING)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }
}
