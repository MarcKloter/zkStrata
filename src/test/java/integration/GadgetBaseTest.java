package integration;

import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.utils.ArgumentsBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class GadgetBaseTest {
    @Test
    void Equality_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("equality")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void Inequality_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("inequality")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void BoundsCheck_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("boundscheck")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void MiMCHash_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("mimchash")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void MerkleTree_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("merkletree")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void LessThan_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("lessthan")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void SetMembership_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("setmembership")
                    .withInstance("pass1", "passport.metadata")
                    .withInstance("pass2", "passport.metadata")
                    .build();
            Compiler.run(args);
        });
    }
}
