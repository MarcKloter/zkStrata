package gadgets;

import org.junit.jupiter.api.Test;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.impl.MerkleTreeGadget;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.Constants;

import java.math.BigInteger;

import static zkstrata.utils.BinaryTree.Node;
import static zkstrata.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class MerkleTreeGadgetTest {
    private static final String IMAGE_1 = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
    private static final String IMAGE_2 = "0x0cf73df10b141c015cc31bd84798e506529c8c3d2c8a7b0f97b5259656bdcacb";

    private static final InstanceVariable INSTANCE_VAR_ROOT_LARGE = createInstanceVariable(new HexLiteral(Constants.ED25519_PRIME_ORDER));
    private static final InstanceVariable INSTANCE_VAR_ROOT_NEG = createInstanceVariable(new HexLiteral(BigInteger.valueOf(-5)));
    private static final InstanceVariable INSTANCE_VAR_ROOT_1 = createInstanceVariable(new HexLiteral(IMAGE_1));
    private static final InstanceVariable INSTANCE_VAR_ROOT_2 = createInstanceVariable(new HexLiteral(IMAGE_2));

    private static final InstanceVariable INSTANCE_VAR_INT = createInstanceVariable(new Literal(BigInteger.valueOf(41)));
    private static final InstanceVariable INSTANCE_VAR_STRING = createInstanceVariable(new Literal("String"));

    private static final WitnessVariable WITNESS_VAR_INT = createWitnessVariable(BigInteger.class, 1);
    private static final WitnessVariable WITNESS_VAR_STRING = createWitnessVariable(String.class, 2);

    private static final Node<Variable> LEAF_1 = new Node<>(INSTANCE_VAR_INT);
    private static final Node<Variable> LEAF_2 = new Node<>(INSTANCE_VAR_STRING);
    private static final Node<Variable> LEAF_3 = new Node<>(WITNESS_VAR_INT);
    private static final Node<Variable> LEAF_4 = new Node<>(WITNESS_VAR_STRING);

    private static final BinaryTree<Variable> TREE_1 = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<Variable> TREE_2 = new BinaryTree<>(new Node<>(LEAF_1, new Node<>(LEAF_3, LEAF_2)));

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

    @Test
    void Is_Not_Equal_To_3() {
        MerkleTreeGadget merkleTreeGadget = new MerkleTreeGadget(INSTANCE_VAR_ROOT_2, TREE_1);
        assertNotEquals(null, merkleTreeGadget);
    }
}
