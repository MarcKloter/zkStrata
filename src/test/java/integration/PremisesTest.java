package integration;

import org.junit.jupiter.api.Test;
import zkstrata.codegen.representations.BulletproofsGadgetsStructure;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.ArgumentsBuilder;

import static org.junit.jupiter.api.Assertions.*;

public class PremisesTest {
    @Test
    void Mixed_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(PremisesTest.class)
                    .withStatement("default")
                    .withPremise("equality")
                    .withPremise("boundscheck")
                    .withPremise("mimchash")
                    .withPremise("merkletree")
                    .withWitness("pass", "passport")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void Witness_Exposure_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(PremisesTest.class)
                    .withStatement("default_instance")
                    .withPremise("default")
                    .withWitness("pass", "passport")
                    .withInstance("pass", "passport.metadata")
                    .withInstance("pass_i", "passport")
                    .build();
            new Compiler(args).compile();
        });

        assertTrue(exception.getMessage().toLowerCase().contains("simultaneously"));
    }

    @Test
    void Contradiction_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(PremisesTest.class)
                    .withStatement("default")
                    .withPremise("default_contradiction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Validation_Rule_Is_Repressed_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(PremisesTest.class)
                    .withStatement("default")
                    .withPremise("default")
                    .withWitness("pass", "passport")
                    .withInstance("pass", "passport.metadata")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(0, statement.getGadgets().size());
        });
    }

    @Test
    void Validation_Rule_Is_Preserved_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(PremisesTest.class)
                    .withStatement("default_extended")
                    .withPremise("default")
                    .withWitness("pass", "passport")
                    .withInstance("pass", "passport.metadata")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(2, statement.getGadgets().size());
        });
    }
}
