package cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.api.cli.CommandLineInterface;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;

public class CommandLineInterfaceTest {
    private static final String TEST_RESOURCES = "src/test/resources/";
    private static final String NAME = "default";
    private static final String STATEMENT_FILE = TEST_RESOURCES + "statements/" + NAME + ".zkstrata";
    private static final String WITNESS_FILE = TEST_RESOURCES + "data/passport.json";
    private static final String INSTANCE_FILE = TEST_RESOURCES + "data/passport_instance.json";
    private static final String SCHEMA_FILE = TEST_RESOURCES + "schemas/passport_ch.schema.json";

    private static final String INSTANCE_ALIAS = "pass_instance";
    private static final String WITNESS_ALIAS = "pass";
    private static final String SCHEMA = "passport_ch";

    @Test
    void Check_Long_Args() {
        String[] command = new String[]{
                "--statement",
                STATEMENT_FILE,
                "--witness-data",
                String.format("%s=%s", WITNESS_ALIAS, WITNESS_FILE),
                "--schemas",
                String.format("%s=%s", SCHEMA, SCHEMA_FILE),
                "--instance-data",
                String.format("%s=%s", INSTANCE_ALIAS, INSTANCE_FILE)
        };
        CommandLineInterface cli = new CommandLineInterface();
        Arguments arguments = cli.parse(command);
        assertEquals(NAME, arguments.getName());
        assertEquals(STATEMENT_FILE, arguments.getStatement().getSource());
        assertNotNull(arguments.getStatement().getValue());
        assertEquals(JsonAccessor.class, arguments.getInstanceData().get(INSTANCE_ALIAS).getClass());
        assertEquals(JsonSchema.class, arguments.getSchemas().get(SCHEMA).getClass());
        assertEquals(JsonAccessor.class, arguments.getWitnessData().get(WITNESS_ALIAS).getClass());
    }

    @Test
    void Missing_Schema_File() {
        String[] command = new String[]{
                "--statement",
                "not-a-file"
        };
        CommandLineInterface cli = new CommandLineInterface();
        assertThrows(IllegalArgumentException.class, () -> {
            cli.parse(command);
        });
    }

    @Test
    void Malformed_Schema() {
        String[] command = new String[]{
                "--statement",
                STATEMENT_FILE,
                "--schemas",
                String.format("%s", SCHEMA_FILE)
        };
        CommandLineInterface cli = new CommandLineInterface();
        assertThrows(IllegalArgumentException.class, () -> {
            cli.parse(command);
        });
    }

    @Test
    void Malformed_Witness() {
        String[] command = new String[]{
                "--statement",
                STATEMENT_FILE,
                "--witness-data",
                String.format("%s", WITNESS_FILE)
        };
        CommandLineInterface cli = new CommandLineInterface();
        assertThrows(IllegalArgumentException.class, () -> {
            cli.parse(command);
        });
    }

    @Test
    void Malformed_Instance() {
        String[] command = new String[]{
                "--statement",
                STATEMENT_FILE,
                "--instance-data",
                String.format("%s", INSTANCE_FILE)
        };
        CommandLineInterface cli = new CommandLineInterface();
        assertThrows(IllegalArgumentException.class, () -> {
            cli.parse(command);
        });
    }
}
