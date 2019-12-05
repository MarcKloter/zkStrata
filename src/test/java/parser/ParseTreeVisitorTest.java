package parser;

import static org.junit.jupiter.api.Assertions.*;
import static zkstrata.utils.StatementBuilder.*;
import static zkstrata.utils.StatementBuilder.integerLiteral;

import org.junit.jupiter.api.Test;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Clause;
import zkstrata.parser.ast.predicates.*;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.StatementBuilder;

import java.math.BigInteger;
import java.util.Set;

public class ParseTreeVisitorTest {
    private static final String SOURCE = "test";
    private static final String SCHEMA = "schema";
    private static final String PARENT_SCHEMA = null;
    private static final String ALIAS = "alias";
    private static final String UNEXPECTED_SYMBOL = "%";
    private static final String UNEXPECTED_TOKEN = "FOR";
    private static final String MISSING_TOKEN = "";
    private static final String IDENTIFIER_1 = "alias.identifier1";
    private static final String IDENTIFIER_2 = "alias.identifier2";
    private static final String STRING_LITERAL = "StringLiteral";
    private static final String HEX_LITERAL = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
    private static final BigInteger INT_LITERAL_1 = BigInteger.valueOf(5);
    private static final BigInteger INT_LITERAL_2 = BigInteger.valueOf(20);

    @Test
    void Equality_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .equality(IDENTIFIER_1, stringLiteral(STRING_LITERAL))
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(Equality.class, predicateClause.getClass());

        Equality equality = (Equality) predicateClause;
        assertEquals(IDENTIFIER_1, equality.getLeft().getValue());
        assertEquals(STRING_LITERAL, equality.getRight().getValue());
    }

    @Test
    void Equality_Is_Parsed_Correctly_2() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .equality(integerLiteral(INT_LITERAL_1), IDENTIFIER_1)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(Equality.class, predicateClause.getClass());

        Equality equality = (Equality) predicateClause;
        assertEquals(INT_LITERAL_1, equality.getLeft().getValue());
        assertEquals(IDENTIFIER_1, equality.getRight().getValue());
    }

    @Test
    void Inequality_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .inequality(IDENTIFIER_1, stringLiteral(STRING_LITERAL))
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(Inequality.class, predicateClause.getClass());

        Inequality inequality = (Inequality) predicateClause;
        assertEquals(IDENTIFIER_1, inequality.getLeft().getValue());
        assertEquals(STRING_LITERAL, inequality.getRight().getValue());
    }

    @Test
    void Inequality_Is_Parsed_Correctly_2() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .inequality(integerLiteral(INT_LITERAL_1), IDENTIFIER_1)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(Inequality.class, predicateClause.getClass());

        Inequality inequality = (Inequality) predicateClause;
        assertEquals(INT_LITERAL_1, inequality.getLeft().getValue());
        assertEquals(IDENTIFIER_1, inequality.getRight().getValue());
    }

    @Test
    void BoundsCheck_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .boundsCheck(IDENTIFIER_1, integerLiteral(INT_LITERAL_1), integerLiteral(INT_LITERAL_2))
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(BoundsCheck.class, predicateClause.getClass());

        BoundsCheck boundsCheck = (BoundsCheck) predicateClause;
        assertEquals(IDENTIFIER_1, boundsCheck.getValue().getValue());
        assertEquals(INT_LITERAL_1, boundsCheck.getMin().getValue());
        assertEquals(INT_LITERAL_2, boundsCheck.getMax().getValue());
    }

    @Test
    void BoundsCheck_Is_Parsed_Correctly_2() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .boundsCheck(IDENTIFIER_1, integerLiteral(INT_LITERAL_1), null)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(BoundsCheck.class, predicateClause.getClass());

        BoundsCheck boundsCheck = (BoundsCheck) predicateClause;
        assertEquals(IDENTIFIER_1, boundsCheck.getValue().getValue());
        assertEquals(INT_LITERAL_1, boundsCheck.getMin().getValue());
    }

    @Test
    void BoundsCheck_Is_Parsed_Correctly_3() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .boundsCheck(IDENTIFIER_1, null, integerLiteral(INT_LITERAL_2))
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(BoundsCheck.class, predicateClause.getClass());

        BoundsCheck boundsCheck = (BoundsCheck) predicateClause;
        assertEquals(IDENTIFIER_1, boundsCheck.getValue().getValue());
        assertEquals(INT_LITERAL_2, boundsCheck.getMax().getValue());
    }

    @Test
    void MiMCHash_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .mimcHash(IDENTIFIER_1, HEX_LITERAL)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(MiMCHash.class, predicateClause.getClass());

        MiMCHash mimcHash = (MiMCHash) predicateClause;
        assertEquals(IDENTIFIER_1, mimcHash.getPreimage().getValue());
        assertEquals(HEX_LITERAL, mimcHash.getImage().getValue());
    }

    @Test
    void MerkleTree_Is_Parsed_Correctly_1() {
        BinaryTree<String> tree = new BinaryTree<>(new BinaryTree.Node<>(
                new BinaryTree.Node<>(new BinaryTree.Node<>(stringLiteral(STRING_LITERAL)), new BinaryTree.Node<>(IDENTIFIER_1)),
                new BinaryTree.Node<>(new BinaryTree.Node<>(HEX_LITERAL), new BinaryTree.Node<>(integerLiteral(INT_LITERAL_1)))
        ));
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .merkleTree(HEX_LITERAL, tree)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(MerkleTree.class, predicateClause.getClass());

        MerkleTree merkleTree = (MerkleTree) predicateClause;
        assertEquals(HEX_LITERAL, merkleTree.getRoot().getValue());
        assertEquals(STRING_LITERAL, merkleTree.getTree().getRoot().getLeft().getLeft().getValue().getValue());
        assertEquals(IDENTIFIER_1, merkleTree.getTree().getRoot().getLeft().getRight().getValue().getValue());
        assertEquals(HEX_LITERAL, merkleTree.getTree().getRoot().getRight().getLeft().getValue().getValue());
        assertEquals(INT_LITERAL_1, merkleTree.getTree().getRoot().getRight().getRight().getValue().getValue());
    }

    @Test
    void MerkleTree_Is_Parsed_Correctly_2() {
        BinaryTree<String> tree = new BinaryTree<>(new BinaryTree.Node<>(
                new BinaryTree.Node<>(new BinaryTree.Node<>(stringLiteral(STRING_LITERAL)), new BinaryTree.Node<>(IDENTIFIER_1)),
                new BinaryTree.Node<>(integerLiteral(INT_LITERAL_1))
        ));
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .merkleTree(HEX_LITERAL, tree)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(MerkleTree.class, predicateClause.getClass());

        MerkleTree merkleTree = (MerkleTree) predicateClause;
        assertEquals(HEX_LITERAL, merkleTree.getRoot().getValue());
        assertEquals(STRING_LITERAL, merkleTree.getTree().getRoot().getLeft().getLeft().getValue().getValue());
        assertEquals(IDENTIFIER_1, merkleTree.getTree().getRoot().getLeft().getRight().getValue().getValue());
        assertEquals(INT_LITERAL_1, merkleTree.getTree().getRoot().getRight().getValue().getValue());
    }

    @Test
    void MerkleTree_Is_Parsed_Correctly_3() {
        BinaryTree<String> tree = new BinaryTree<>(new BinaryTree.Node<>(
                new BinaryTree.Node<>(IDENTIFIER_1),
                new BinaryTree.Node<>(new BinaryTree.Node<>(HEX_LITERAL), new BinaryTree.Node<>(integerLiteral(INT_LITERAL_1)))
        ));
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .merkleTree(HEX_LITERAL, tree)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(MerkleTree.class, predicateClause.getClass());

        MerkleTree merkleTree = (MerkleTree) predicateClause;
        assertEquals(HEX_LITERAL, merkleTree.getRoot().getValue());
        assertEquals(IDENTIFIER_1, merkleTree.getTree().getRoot().getLeft().getValue().getValue());
        assertEquals(HEX_LITERAL, merkleTree.getTree().getRoot().getRight().getLeft().getValue().getValue());
        assertEquals(INT_LITERAL_1, merkleTree.getTree().getRoot().getRight().getRight().getValue().getValue());
    }

    @Test
    void LessThan_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .lessThan(IDENTIFIER_1, IDENTIFIER_2)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(LessThan.class, predicateClause.getClass());

        LessThan lessThan = (LessThan) predicateClause;
        assertEquals(IDENTIFIER_1, lessThan.getLeft().getValue());
        assertEquals(IDENTIFIER_2, lessThan.getRight().getValue());
    }

    @Test
    void LessThan_Is_Parsed_Correctly_2() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .greaterThan(IDENTIFIER_1, IDENTIFIER_2)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(LessThan.class, predicateClause.getClass());

        LessThan lessThan = (LessThan) predicateClause;
        assertEquals(IDENTIFIER_1, lessThan.getRight().getValue());
        assertEquals(IDENTIFIER_2, lessThan.getLeft().getValue());
    }

    @Test
    void SetMembership_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .setMembership(IDENTIFIER_1, Set.of(integerLiteral(INT_LITERAL_1), HEX_LITERAL, IDENTIFIER_1))
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);

        Clause predicateClause = ast.getClause();
        assertEquals(SetMembership.class, predicateClause.getClass());

        SetMembership setMembership = (SetMembership) predicateClause;
        assertEquals(IDENTIFIER_1, setMembership.getMember().getValue());
        Set<Object> set = Set.of(INT_LITERAL_1, HEX_LITERAL, IDENTIFIER_1);
        for (Value value : setMembership.getSet())
            assertTrue(set.contains(value.getValue()));
    }

    @Test
    void Unexpected_Symbol_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            new ParseTreeVisitor().parse(SOURCE, UNEXPECTED_SYMBOL, PARENT_SCHEMA);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("unexpected symbol"));
    }

    @Test
    void Unexpected_Token_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            new ParseTreeVisitor().parse(SOURCE, UNEXPECTED_TOKEN, PARENT_SCHEMA);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("unexpected token"));
    }

    @Test
    void Unexpected_Input_Should_Throw() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () -> {
            String statement = new StatementBuilder()
                    .subject(SCHEMA, ALIAS, true)
                    .mimcHash(MISSING_TOKEN, HEX_LITERAL)
                    .build();
            new ParseTreeVisitor().parse(SOURCE, statement, PARENT_SCHEMA);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("unexpected input"));
    }
}
