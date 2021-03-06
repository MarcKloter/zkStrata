package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Test;
import zkstrata.codegen.representations.BulletproofsGadgetsStructure;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.ArgumentsBuilder;

public class IntegrationTest {
    @Test
    void Default_Quiet_Should_Succeed() {
        Configurator.setRootLevel(Level.DEBUG);
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        Configurator.setRootLevel(Level.OFF);
    }

    @Test
    void And_Conjunction_Verbose_Should_Succeed() {
        Configurator.setRootLevel(Level.DEBUG);
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("and_conjunction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        Configurator.setRootLevel(Level.OFF);
    }

    @Test
    void Or_Conjunction_Verbose_Should_Succeed() {
        Configurator.setRootLevel(Level.DEBUG);
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("or_conjunction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        Configurator.setRootLevel(Level.OFF);
    }

    @Test
    void Duplicate_Alias_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("duplicate_alias")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("is already defined"));
    }

    @Test
    void Alias_Private_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("alias_private")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("reserved keyword"));
    }

    @Test
    void Alias_Public_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("alias_public")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("reserved keyword"));
    }

    @Test
    void Validation_Rule_Default_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withInstance("pass", "passport.metadata")
                    .withSchema("passport_ch", "default_validation_rule")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void Or_Conjunction_Validation_Rule_Default_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "or_validation_rule")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void Validation_Rule_Missing_Instance_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("equality")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("missing instance data"));
    }

    @Test
    void Validation_Rule_Contradiction_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "statement_default_contradiction")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
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
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("simultaneously"));
    }

    @Test
    void Unused_Subject_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("unused_subject")
                    .withInstance("pass1", "passport.metadata")
                    .withInstance("pass2", "passport.metadata")
                    .withInstance("pass3", "passport_instance")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(3, statement.getGadgets().size());
        });
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
            new Compiler(args).compile();
        });
    }

    @Test
    void Verbose_Subject_Definition_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("verbose_default")
                    .withWitness("pass_1", "passport")
                    .withWitness("pass_2", "passport")
                    .withInstance("pass_1", "passport.metadata")
                    .withInstance("pass_2", "passport_instance")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void Validation_Rules_Test_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "validation_rules")
                    .withWitness("pass", "passport")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void Undefined_Schema_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("undefined_schema")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("undefined schema"));
    }

    @Test
    void Missing_Witness_Data_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("missing_witness_data")
                    .withWitness("pass1", "passport")
                    .withInstance("pass1", "passport.metadata")
                    .withInstance("pass2", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("missing witness data"));
    }

    @Test
    void Duplicate_Set_Entries_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("duplicate_set_entries")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("duplicate element"));
    }

    @Test
    void Undeclared_Alias_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("undeclared_alias")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("undeclared alias"));
    }

    @Test
    void Missing_Entry_Should_Throw() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withWitness("pass", "passport_missing_entry")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("missing entry"));
    }

    @Test
    void Witness_Type_Mismatch_Should_Throw() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withWitness("pass", "passport_invalid_entry")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }

    @Test
    void Unexpected_Type_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("unexpected_type")
                    .withWitness("pass", "passport_missing_entry")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("unexpected type"));
    }

    @Test
    void Instance_Subject_Repress_Validation_Rule_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default_instance")
                    .withInstance("pass_i", "passport")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(0, statement.getGadgets().size());
        });
    }

    @Test
    void Tautology_Validation_Rule_Removed_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("default")
                    .withSchema("passport_ch", "tautology_validation_rule")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(1, statement.getGadgets().size());
        });
    }

    @Test
    void Multiple_Witnesses_Preserve_Validation_Rules_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("multiple_witnesses")
                    .withWitness("pass1", "passport")
                    .withWitness("pass2", "passport2")
                    .withInstance("pass1", "passport.metadata")
                    .withInstance("pass2", "passport2.metadata")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(4, statement.getGadgets().size());
        });
    }

    @Test
    void Multiple_Instances_Remove_Validation_Rules_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("multiple_instances")
                    .withInstance("pass1", "passport")
                    .withInstance("pass2", "passport2")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(0, statement.getGadgets().size());
        });
    }

    @Test
    void Boolean_Base_Test_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("boolean")
                    .withSchema("boolean", "boolean")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void Boolean_Type_Mismatch_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("boolean_type_mismatch")
                    .withSchema("boolean", "boolean")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }

    @Test
    void Date_Constants_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("date_constants")
                    .withSchema("date", "date")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void Date_Validation_Rule_Contradiction_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("date_constants")
                    .withSchema("date", "date_validation_rule")
                    .build();
            new Compiler(args).compile();
        });

        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Complex_Optimization_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("complex_optimization")
                    .withSchema("schema", "basic")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(1, statement.getGadgets().size());
        });
    }

    @Test
    void Complex_Statement_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("complex_statement")
                    .withInstance("pass", "passport.metadata")
                    .withInstance("date", "date")
                    .build();
            BulletproofsGadgetsStructure statement = (BulletproofsGadgetsStructure) new Compiler(args).compile();
            assertEquals(13, statement.getGadgets().size());
        });
    }

    @Test
    void Complex_Contradiction_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("complex_contradiction")
                    .withSchema("schema", "basic")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void Heterogeneous_Set_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(IntegrationTest.class)
                    .withStatement("heterogeneous_set")
                    .withInstance("pass1", "passport.metadata")
                    .withInstance("pass2", "passport.metadata")
                    .build();
            new Compiler(args).compile();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("type mismatch"));
    }
}
