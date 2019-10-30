package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;
import zkstrata.exceptions.CompileTimeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class IntegrationTest {
    private static final String STATEMENTS = "statements/";
    private static final String SCHEMAS = "src/test/resources/schemas/";
    private static final String ZKSTRATA_EXT = ".zkstrata";
    private static final String SCHEMA_EXT = ".schema.json";
    private static final String SOURCE = "test";

    private String getStatement(String name) throws IOException {
        InputStream inputStream = IntegrationTest.class.getClassLoader().getResourceAsStream(STATEMENTS + name + ZKSTRATA_EXT);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    @Test
    void Verifier_Equality_Statement_Should_Succeed() {
        String name = "equality";

        assertDoesNotThrow(() -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement,
                    Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
            Compiler.run(args);
        });
    }

    @Test
    void Verifier_BoundsCheck_Statement_Should_Succeed() {
        String name = "boundscheck";

        assertDoesNotThrow(() -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement,
                    Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
            Compiler.run(args);
        });
    }

    @Test
    void Duplicate_Alias_Should_Throw() {
        String name = "duplicate_alias";

        assertThrows(CompileTimeException.class, () -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement,
                    Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
            Compiler.run(args);
        });
    }

    @Test
    void Alias_Self_Should_Throw() {
        String name = "alias_self";

        assertThrows(CompileTimeException.class, () -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement,
                    Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
            Compiler.run(args);
        });
    }

    @Test
    void Schema_Statement_Default() {
        String name = "default";
        String schema_name = "passport_ch";
        String schema_file = SCHEMAS + "default_schema_statement" + SCHEMA_EXT;

        assertDoesNotThrow(() -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement, Collections.emptyMap(), Collections.emptyMap(),
                    Map.of(schema_name, new JsonSchema(schema_file, schema_name)));
            Compiler.run(args);
        });
    }

    @Test
    void Schema_Statement_Default_Contradiction() {
        String name = "default";
        String schema_name = "passport_ch";
        String schema_file = SCHEMAS + "statement_default_contradiction" + SCHEMA_EXT;

        assertThrows(CompileTimeException.class, () -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement, Collections.emptyMap(), Collections.emptyMap(),
                    Map.of(schema_name, new JsonSchema(schema_file, schema_name)));
            Compiler.run(args);
        });
    }
}
