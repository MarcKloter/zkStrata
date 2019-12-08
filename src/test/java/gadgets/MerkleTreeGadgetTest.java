package gadgets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.MerkleTreeGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.Constants;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MerkleTreeGadgetTest {
    private static final Position.Absolute MOCK_POS = Mockito.mock(Position.Absolute.class);

    private static final String IMAGE_1 = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
    private static final String IMAGE_2 = "0x0cf73df10b141c015cc31bd84798e506529c8c3d2c8a7b0f97b5259656bdcacb";

    private static final InstanceVariable INSTANCE_VAR_ROOT_LARGE = new InstanceVariable(new HexLiteral(Constants.ED25519_PRIME_ORDER), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_ROOT_NEG = new InstanceVariable(new HexLiteral(BigInteger.valueOf(-5)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_ROOT_1 = new InstanceVariable(new HexLiteral(IMAGE_1), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_ROOT_2 = new InstanceVariable(new HexLiteral(IMAGE_2), null, MOCK_POS);

    private static final InstanceVariable INSTANCE_VAR_INT = new InstanceVariable(new Literal(BigInteger.valueOf(41)), null, MOCK_POS);
    private static final InstanceVariable INSTANCE_VAR_STRING = new InstanceVariable(new Literal("String"), null, MOCK_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(String.class, "alias2", new Selector(List.of("selector2")));

    private static final WitnessVariable WITNESS_VAR_INT = new WitnessVariable(REF_1, REF_1, MOCK_POS);
    private static final WitnessVariable WITNESS_VAR_STRING = new WitnessVariable(REF_2, REF_2, MOCK_POS);

    private static final BinaryTree.Node<Variable> LEAF_1 = new BinaryTree.Node<>(INSTANCE_VAR_INT);
    private static final BinaryTree.Node<Variable> LEAF_2 = new BinaryTree.Node<>(INSTANCE_VAR_STRING);
    private static final BinaryTree.Node<Variable> LEAF_3 = new BinaryTree.Node<>(WITNESS_VAR_INT);
    private static final BinaryTree.Node<Variable> LEAF_4 = new BinaryTree.Node<>(WITNESS_VAR_STRING);

    private static final BinaryTree<Variable> TREE_1 = new BinaryTree<>(new BinaryTree.Node<>(new BinaryTree.Node<>(LEAF_1, LEAF_4), new BinaryTree.Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<Variable> TREE_2 = new BinaryTree<>(new BinaryTree.Node<>(LEAF_1, new BinaryTree.Node<>(LEAF_3, LEAF_2)));

    @BeforeAll
    static void init() {
        Mockito.when(MOCK_POS.getLine()).thenReturn(1);
        Mockito.when(MOCK_POS.getPosition()).thenReturn(0);
        Mockito.when(MOCK_POS.getSource()).thenReturn(EqualityGadgetTest.class.getSimpleName());
        Mockito.when(MOCK_POS.getStatement()).thenReturn("");
        Mockito.when(MOCK_POS.getTarget()).thenReturn("");
    }

    @Test
    void Root_Too_Large() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
            new MerkleTreeGadget(INSTANCE_VAR_ROOT_LARGE, TREE_1)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("invalid root hash image"));
    }

    @Test
    void Root_Negative() {
        CompileTimeException exception = assertThrows(CompileTimeException.class, () ->
            new MerkleTreeGadget(INSTANCE_VAR_ROOT_NEG, TREE_1)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("invalid root hash image"));
    }

    @Test
    void Is_Equal_To() {
        MerkleTreeGadget merkleTreeGadget1 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_1, TREE_1);
        MerkleTreeGadget merkleTreeGadget2 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_1, TREE_1);
        assertEquals(merkleTreeGadget1, merkleTreeGadget2);
    }

    @Test
    void Is_Not_Equal_To_1() {
        MerkleTreeGadget merkleTreeGadget1 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_1, TREE_1);
        MerkleTreeGadget merkleTreeGadget2 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_2, TREE_1);
        assertNotEquals(merkleTreeGadget1, merkleTreeGadget2);
    }

    @Test
    void Is_Not_Equal_To_2() {
        MerkleTreeGadget merkleTreeGadget1 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_2, TREE_1);
        MerkleTreeGadget merkleTreeGadget2 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_2, TREE_2);
        assertNotEquals(merkleTreeGadget1, merkleTreeGadget2);
    }
}
