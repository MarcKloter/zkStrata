package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.domain.visitor.ASTVisitor;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.predicates.LessThan;
import zkstrata.parser.ast.predicates.MiMCHash;
import zkstrata.parser.ast.types.Identifier;
import zkstrata.parser.ast.types.IntegerLiteral;
import zkstrata.parser.ast.types.StringLiteral;
import zkstrata.utils.ArgumentsBuilder;

import java.util.List;

import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractGadgetTest {
    private static final String SOURCE = "test";
    private static final String STATEMENT = "statement";
    private static final Identifier IDENTIFIER_1 = createIdentifier("", "String");
    private static final Identifier IDENTIFIER_2 = createIdentifier("", "Number");

    private static final Subject SUBJECT = createSubject(true, "");
    private static final IntegerLiteral INT_LIT_13 = createIntegerLiteral(13);
    private static final StringLiteral STRING_LIT = createStringLiteral("string");

    private ASTVisitor visitor;

    @BeforeEach
    void setup() {
        Arguments args = new ArgumentsBuilder(ASTVisitorTest.class)
                .withSchema("schema", "schema")
                .build();
        this.visitor = new ASTVisitor(args, "test");
    }

    @Test
    void Unexpected_Type_Should_Throw_1() {
        BoundsCheck boundsCheckGadget = new BoundsCheck(IDENTIFIER_1, INT_LIT_13, STRING_LIT, getAbsPosition());
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), boundsCheckGadget);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("unexpected type"));
    }

    @Test
    void Unexpected_Type_Should_Throw_2() {
        MiMCHash miMCHash = new MiMCHash(IDENTIFIER_1, STRING_LIT, getAbsPosition());
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), miMCHash);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("unexpected type"));
    }

    @Test
    void Invalid_Confidentiality_Level_Should_Throw() {
        LessThan lessThan = new LessThan(IDENTIFIER_2, INT_LIT_13, getAbsPosition());
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), lessThan);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("not allowed here"));
    }
}
