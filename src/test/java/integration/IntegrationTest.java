package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.ArgumentsBuilder;

public class IntegrationTest {
    @Test
    void Duplicate_Alias_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("duplicate_alias")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Alias_Self_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("alias_self")
                    .withInstance("pass", "passport.metadata")
                    .build();
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Validation_Rule_Default_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "default_validation_rule")
                    .build();
            Compiler.run(args);
        });
    }

    @Test
    void Validation_Rule_Missing_Instance_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("equality")
                    .build();
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Validation_Rule_Contradiction_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "statement_default_contradiction")
                    .build();
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Witness_Exposure_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("witness_exposure")
                    .withWitness("pass_w", "passport")
                    .withInstance("pass_w", "passport.metadata")
                    .withInstance("pass_i", "passport")
                    .build();
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Instance_Data_Referenced_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("instance_data_referenced")
                    .withWitness("pass_w", "passport")
                    .withInstance("pass_w", "passport.metadata")
                    .withInstance("pass_i", "passport_instance")
                    .build();
            Compiler.run(args);
        });
    }
}
