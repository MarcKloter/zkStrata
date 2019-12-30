package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zkstrata.codegen.TargetFormat;
import zkstrata.compiler.Arguments;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.domain.visitor.ASTVisitor;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Subject;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.predicates.LessThan;
import zkstrata.parser.ast.predicates.MiMCHash;
import zkstrata.parser.ast.types.Identifier;
import zkstrata.parser.ast.types.IntegerLiteral;
import zkstrata.parser.ast.types.StringLiteral;
import zkstrata.utils.ArgumentsBuilder;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

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
        this.visitor = new ASTVisitor(args.getSubjectData(), "test");
    }

    @Test
    void Unexpected_Type_Should_Throw_1() {
        BoundsCheck boundsCheckGadget = new BoundsCheck(IDENTIFIER_1, INT_LIT_13, STRING_LIT);
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), boundsCheckGadget);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("unexpected type"));
    }

    @Test
    void Unexpected_Type_Should_Throw_2() {
        MiMCHash miMCHash = new MiMCHash(IDENTIFIER_1, STRING_LIT);
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), miMCHash);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("unexpected type"));
    }

    @Test
    void Invalid_Confidentiality_Level_Should_Throw() {
        LessThan lessThan = new LessThan(IDENTIFIER_2, INT_LIT_13);
        AbstractSyntaxTree ast = new AbstractSyntaxTree(SOURCE, STATEMENT, List.of(SUBJECT), lessThan);

        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> visitor.visit(ast));

        assertTrue(exception.getMessage().toLowerCase().contains("not allowed here"));
    }

    @Test
    void Unable_To_Access_Should_Throw() {
        InvalidGadget invalidGadget = new InvalidGadget();
        InstanceVariable instanceVariable = createInstanceVariable(new Literal(BigInteger.valueOf(17)));

        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                invalidGadget.initFrom(Map.of("unableToAccess", instanceVariable))
        );

        assertTrue(exception.getMessage().toLowerCase().contains("unable to access"));
    }

    @Test
    void Missing_Type_Annotation_Should_Throw() {
        InvalidGadget invalidGadget = new InvalidGadget();
        InstanceVariable instanceVariable = createInstanceVariable(new Literal(BigInteger.valueOf(17)));

        InternalCompilerException exception = assertThrows(InternalCompilerException.class, () ->
                invalidGadget.initFrom(Map.of("missingTypeAnnotation", instanceVariable))
        );

        assertTrue(exception.getMessage().toLowerCase().contains("is missing @type"));
    }

    public static class InvalidGadget extends AbstractGadget {
        @Type({BigInteger.class})
        private static final Variable unableToAccess = null;

        private Variable missingTypeAnnotation;

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public List<TargetFormat> toTargetFormat() {
            return null;
        }

        @Override
        public int getCostEstimate() {
            return 0;
        }
    }
}
