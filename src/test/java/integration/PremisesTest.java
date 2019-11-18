package integration;

import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.ArgumentsBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            Compiler.run(args);
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
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Contradiction_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(PremisesTest.class)
                    .withStatement("default")
                    .withPremise("default_contradiction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }
}
