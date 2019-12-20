package integration;

import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.ArgumentsBuilder;

import static org.junit.jupiter.api.Assertions.*;

public class ConjunctionTest {
    @Test
    void Or_Conjunction_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(ConjunctionTest.class)
                    .withStatement("or_conjunction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).run();
        });
    }

    @Test
    void Or_Conjunction_Contradiction_Should_Succeed() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(ConjunctionTest.class)
                    .withStatement("or_conjunction_contradiction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).run();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }

    @Test
    void And_Conjunction_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = new ArgumentsBuilder(ConjunctionTest.class)
                    .withStatement("and_conjunction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).run();
        });
    }

    @Test
    void And_Conjunction_Contradiction_Should_Succeed() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            Arguments args = new ArgumentsBuilder(ConjunctionTest.class)
                    .withStatement("and_conjunction_contradiction")
                    .withInstance("pass", "passport.metadata")
                    .build();
            new Compiler(args).run();
        });
        assertTrue(exception.getMessage().toLowerCase().contains("contradiction"));
    }
}
