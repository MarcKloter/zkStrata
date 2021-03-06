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
            new Compiler(args).compile();
        });
    }

    @Test
    void Inequality_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("inequality")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void BoundsCheck_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("boundscheck")
                    .withInstance("pass", "passport.metadata")
                    .withInstance("pass2", "passport2.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void MiMCHash_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("mimchash")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void MerkleTree_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("merkletree")
                    .withInstance("pass", "passport.metadata")
                    .withSchema("hex", "hex")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void LessThan_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(GadgetBaseTest.class)
                    .withStatement("lessthan")
                    .withInstance("pass", "passport.metadata")
                    .withInstance("pass2", "passport2")
                    .withInstance("pass3", "passport2")
                    .build();
            new Compiler(args).compile();
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
            new Compiler(args).compile();
        });
    }
}
