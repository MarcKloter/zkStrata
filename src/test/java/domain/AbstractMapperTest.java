package domain;

import org.junit.jupiter.api.Test;
import zkstrata.domain.gadgets.mapper.LessThanMapper;
import zkstrata.exceptions.InternalCompilerException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractMapperTest {
    @Test
    void Assert_Equals_Throws() {
        assertThrows(InternalCompilerException.class, () -> new LessThanMapper().equals(null));
    }

    @Test
    void Assert_HashCode_Throws() {
        assertThrows(InternalCompilerException.class, () -> new LessThanMapper().hashCode());
    }

    @Test
    void Assert_ToTargetFormat_Throws() {
        assertThrows(InternalCompilerException.class, () -> new LessThanMapper().toBulletproofsGadgets());
    }

    @Test
    void Assert_GetCostEstimate_Throws() {
        assertThrows(InternalCompilerException.class, () -> new LessThanMapper().getCostEstimate());
    }
}
