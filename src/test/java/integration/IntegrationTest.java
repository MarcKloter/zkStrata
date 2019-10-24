package integration;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class IntegrationTest {
    private static final String STATEMENTS = "statements/";
    private static final String FILE_EXTENSION = ".zkstrata";
    private static final String SOURCE = "test";

    private String getStatement(String name) throws IOException {
        InputStream inputStream = IntegrationTest.class.getClassLoader().getResourceAsStream(STATEMENTS + name + FILE_EXTENSION);
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
}
