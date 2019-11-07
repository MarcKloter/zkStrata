package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;
import zkstrata.exceptions.CompileTimeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class IntegrationTest {
    private static final String STATEMENTS = "statements/";
    private static final String INSTANCE_DATA = "src/test/resources/instance-data/";
    private static final String WITNESS_DATA = "src/test/resources/witness-data/";
    private static final String SCHEMAS = "src/test/resources/schemas/";
    private static final String ZKSTRATA_EXT = ".zkstrata";
    private static final String JSON_EXT = ".json";
    private static final String SCHEMA_EXT = ".schema.json";
    private static final String SOURCE = "test";

    private static final ValueAccessor WITNESS_PASSPORT_JSON = new JsonAccessor(WITNESS_DATA + "passport" + JSON_EXT);
    private static final ValueAccessor INSTANCE_PASSPORT_JSON = new JsonAccessor(INSTANCE_DATA + "passport" + JSON_EXT);

    private String getStatement(String name) throws IOException {
        InputStream inputStream = IntegrationTest.class.getClassLoader().getResourceAsStream(STATEMENTS + name + ZKSTRATA_EXT);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    private String getWitnessData(String name) throws IOException {
        InputStream inputStream = IntegrationTest.class.getClassLoader().getResourceAsStream(INSTANCE_DATA + name + JSON_EXT);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    private String getInstanceData(String name) throws IOException {
        InputStream inputStream = IntegrationTest.class.getClassLoader().getResourceAsStream(WITNESS_DATA + name + JSON_EXT);
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
    void Verifier_MiMCHash_Statement_Should_Succeed() {
        String name = "mimchash";

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

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement,
                    Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Alias_Self_Should_Throw() {
        String name = "alias_self";

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement,
                    Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Schema_Statement_Default_Should_Succeed() {
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
    void Schema_Statement_Contradiction_Should_Throw() {
        String name = "default";
        String schema_name = "passport_ch";
        String schema_file = SCHEMAS + "statement_default_contradiction" + SCHEMA_EXT;

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement, Collections.emptyMap(), Collections.emptyMap(),
                    Map.of(schema_name, new JsonSchema(schema_file, schema_name)));
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Witness_Exposure_Should_Throw() {
        String name = "witness_exposure";

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement, Map.of("pass_w", WITNESS_PASSPORT_JSON),
                    Map.of("pass_i", WITNESS_PASSPORT_JSON), Collections.emptyMap());
            Compiler.run(args);
        });

        System.out.println(exception.getMessage());
    }

    @Test
    void Instance_Data_Referenced_Should_Succeed() {
        String name = "instance_data_referenced";

        assertDoesNotThrow(() -> {
            String statement = getStatement(name);
            Arguments args = new Arguments(name, SOURCE, statement, Map.of("pass_w", WITNESS_PASSPORT_JSON),
                    Map.of("pass_i", INSTANCE_PASSPORT_JSON), Collections.emptyMap());
            Compiler.run(args);
        });
    }
}
