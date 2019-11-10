package parser;

import static org.junit.jupiter.api.Assertions.*;
import static zkstrata.utils.StatementBuilder.*;
import static zkstrata.utils.StatementBuilder.integerLiteral;

import org.junit.jupiter.api.Test;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.predicates.*;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.StatementBuilder;

import java.math.BigInteger;

public class ParseTreeVisitorTest {
    private static final String SOURCE = "test";
    private static final String SCHEMA = "schema";
    private static final String ALIAS = "alias";
    private static final String INVALID_SYMBOL = "%";
    private static final String IDENTIFIER = "alias.identifier";
    private static final String STRING_LITERAL = "StringLiteral";
    private static final String HEX_LITERAL = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
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
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .boundsCheck(IDENTIFIER, INT_LITERAL_1, INT_LITERAL_2)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(BoundsCheck.class, predicate.getClass());

        BoundsCheck boundsCheck = (BoundsCheck) predicate;
        assertEquals(IDENTIFIER, boundsCheck.getValue().getValue());
        assertEquals(INT_LITERAL_1, boundsCheck.getMin().getValue());
        assertEquals(INT_LITERAL_2, boundsCheck.getMax().getValue());
    }

    @Test
    void BoundsCheck_Is_Parsed_Correctly_2() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .boundsCheck(IDENTIFIER, INT_LITERAL_1, null)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(BoundsCheck.class, predicate.getClass());

        BoundsCheck boundsCheck = (BoundsCheck) predicate;
        assertEquals(IDENTIFIER, boundsCheck.getValue().getValue());
        assertEquals(INT_LITERAL_1, boundsCheck.getMin().getValue());
    }

    @Test
    void BoundsCheck_Is_Parsed_Correctly_3() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .boundsCheck(IDENTIFIER, null, INT_LITERAL_2)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(BoundsCheck.class, predicate.getClass());

        BoundsCheck boundsCheck = (BoundsCheck) predicate;
        assertEquals(IDENTIFIER, boundsCheck.getValue().getValue());
        assertEquals(INT_LITERAL_2, boundsCheck.getMax().getValue());
    }

    @Test
    void MiMCHash_Is_Parsed_Correctly_1() {
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .mimcHash(IDENTIFIER, HEX_LITERAL)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(MiMCHash.class, predicate.getClass());

        MiMCHash mimcHash = (MiMCHash) predicate;
        assertEquals(IDENTIFIER, mimcHash.getPreimage().getValue());
        assertEquals(HEX_LITERAL, mimcHash.getImage().getValue());
    }

    @Test
    void MerkleTree_Is_Parsed_Correctly_1() {
        BinaryTree<String> tree = new BinaryTree<>(new BinaryTree.Node<>(
                new BinaryTree.Node<>(new BinaryTree.Node<>(stringLiteral(STRING_LITERAL)), new BinaryTree.Node<>(IDENTIFIER)),
                new BinaryTree.Node<>(new BinaryTree.Node<>(HEX_LITERAL), new BinaryTree.Node<>(integerLiteral(INT_LITERAL_1)))
        ));
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .merkleTree(HEX_LITERAL, tree)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(MerkleTree.class, predicate.getClass());

        MerkleTree merkleTree = (MerkleTree) predicate;
        assertEquals(HEX_LITERAL, merkleTree.getRoot().getValue());
        assertEquals(STRING_LITERAL, merkleTree.getTree().getRoot().getLeft().getLeft().getValue().getValue());
        assertEquals(IDENTIFIER, merkleTree.getTree().getRoot().getLeft().getRight().getValue().getValue());
        assertEquals(HEX_LITERAL, merkleTree.getTree().getRoot().getRight().getLeft().getValue().getValue());
        assertEquals(INT_LITERAL_1, merkleTree.getTree().getRoot().getRight().getRight().getValue().getValue());
    }

    @Test
    void MerkleTree_Is_Parsed_Correctly_2() {
        BinaryTree<String> tree = new BinaryTree<>(new BinaryTree.Node<>(
                new BinaryTree.Node<>(new BinaryTree.Node<>(stringLiteral(STRING_LITERAL)), new BinaryTree.Node<>(IDENTIFIER)),
                new BinaryTree.Node<>(integerLiteral(INT_LITERAL_1))
        ));
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .merkleTree(HEX_LITERAL, tree)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(MerkleTree.class, predicate.getClass());

        MerkleTree merkleTree = (MerkleTree) predicate;
        assertEquals(HEX_LITERAL, merkleTree.getRoot().getValue());
        assertEquals(STRING_LITERAL, merkleTree.getTree().getRoot().getLeft().getLeft().getValue().getValue());
        assertEquals(IDENTIFIER, merkleTree.getTree().getRoot().getLeft().getRight().getValue().getValue());
        assertEquals(INT_LITERAL_1, merkleTree.getTree().getRoot().getRight().getValue().getValue());
    }

    @Test
    void MerkleTree_Is_Parsed_Correctly_3() {
        BinaryTree<String> tree = new BinaryTree<>(new BinaryTree.Node<>(
                new BinaryTree.Node<>(IDENTIFIER),
                new BinaryTree.Node<>(new BinaryTree.Node<>(HEX_LITERAL), new BinaryTree.Node<>(integerLiteral(INT_LITERAL_1)))
        ));
        String statement = new StatementBuilder()
                .subject(SCHEMA, ALIAS, true)
                .merkleTree(HEX_LITERAL, tree)
                .build();
        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(SOURCE, statement);
        assertEquals(1, ast.getPredicates().size());

        Predicate predicate = ast.getPredicates().get(0);
        assertEquals(MerkleTree.class, predicate.getClass());

        MerkleTree merkleTree = (MerkleTree) predicate;
        assertEquals(HEX_LITERAL, merkleTree.getRoot().getValue());
        assertEquals(IDENTIFIER, merkleTree.getTree().getRoot().getLeft().getValue().getValue());
        assertEquals(HEX_LITERAL, merkleTree.getTree().getRoot().getRight().getLeft().getValue().getValue());
        assertEquals(INT_LITERAL_1, merkleTree.getTree().getRoot().getRight().getRight().getValue().getValue());
    }

    @Test
    void Invalid_Symbol_Should_Fail() {
        assertThrows(CompileTimeException.class, () -> {
            new ParseTreeVisitor().parse(SOURCE, INVALID_SYMBOL);
        });
    }
}
