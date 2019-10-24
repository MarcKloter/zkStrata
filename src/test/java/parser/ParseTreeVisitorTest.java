package parser;

import static org.junit.jupiter.api.Assertions.*;
import static zkstrata.utils.StatementBuilder.*;

import org.junit.jupiter.api.Test;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.predicates.Equality;
import zkstrata.parser.ast.predicates.Predicate;
import zkstrata.utils.StatementBuilder;

import java.math.BigInteger;

public class ParseTreeVisitorTest {
    private static final String SOURCE = "test";
    private static final String SCHEMA = "schema";
    private static final String ALIAS = "alias";
    private static final String IDENTIFIER = "alias.identifier";
    private static final String STRING_LITERAL = "StringLiteral";
    private static final BigInteger INT_LITERAL_1 = BigInteger.valueOf(5);
    private static final BigInteger INT_LITERAL_2 = BigInteger.valueOf(20);

    @Test
    void Equality_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .equality(IDENTIFIER, stringLiteral(STRING_LITERAL))
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(Equality.class, predicate.getClass());

        Equality equality = (Equality) predicate;
        assertEquals(IDENTIFIER, equality.getLeftHand().getValue());
        assertEquals(STRING_LITERAL, equality.getRightHand().getValue());
    }

    @Test
    void Equality_Is_Parsed_Correctly_2() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .equality(integerLiteral(INT_LITERAL_1), IDENTIFIER)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(Equality.class, predicate.getClass());

        Equality equality = (Equality) predicate;
        assertEquals(INT_LITERAL_1, equality.getLeftHand().getValue());
        assertEquals(IDENTIFIER, equality.getRightHand().getValue());
    }

    @Test
    void BoundsCheck_Is_Parsed_Correctly_1() {
        String statement = String.format("PROOF FOR schema AS %s THAT %s IS LESS THAN %s AND GREATER THAN %s",
                ALIAS, IDENTIFIER, INT_LITERAL_1, INT_LITERAL_2);
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse("test", statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(BoundsCheck.class, predicate.getClass());

        BoundsCheck boundsCheck = (BoundsCheck) predicate;
        assertEquals(IDENTIFIER, boundsCheck.getValue().getValue());
        assertEquals(INT_LITERAL_1, boundsCheck.getMax().getValue());
        assertEquals(INT_LITERAL_2, boundsCheck.getMin().getValue());
    }

    @Test
    void Invalid_Symbol_Should_Fail() {
        String invalidSymbol = "%";
        assertThrows(CompileTimeException.class, () -> {
            new ParseTreeVisitor().parse("test", invalidSymbol);
        });
    }
}
