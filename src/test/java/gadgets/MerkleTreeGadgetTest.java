package gadgets;

import org.junit.jupiter.api.Test;
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
import zkstrata.utils.SemanticsUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MerkleTreeGadgetTest {
    private static final Position.Absolute DUMMY_POS = new Position.Absolute("src", "stmt", "t", 1, 1);

    private static final String IMAGE_1 = "0x01bd94c871b2d21926cf4f1c9e2fcbca8ece3353a0aac7cea8d507a9ad30afe2";
    private static final String IMAGE_2 = "0x0cf73df10b141c015cc31bd84798e506529c8c3d2c8a7b0f97b5259656bdcacb";

    private static final InstanceVariable INSTANCE_VAR_ROOT_NEG = new InstanceVariable(new HexLiteral(BigInteger.valueOf(-5)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_ROOT_1 = new InstanceVariable(new HexLiteral(IMAGE_1), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_ROOT_2 = new InstanceVariable(new HexLiteral(IMAGE_2), null, DUMMY_POS);

    private static final InstanceVariable INSTANCE_VAR_INT = new InstanceVariable(new Literal(BigInteger.valueOf(41)), null, DUMMY_POS);
    private static final InstanceVariable INSTANCE_VAR_STRING = new InstanceVariable(new Literal("String"), null, DUMMY_POS);

    private static final Reference REF_1 = new Reference(BigInteger.class, "alias1", new Selector(List.of("selector1")));
    private static final Reference REF_2 = new Reference(String.class, "alias2", new Selector(List.of("selector2")));

    private static final WitnessVariable WITNESS_VAR_INT = new WitnessVariable(REF_1, REF_1, DUMMY_POS);
    private static final WitnessVariable WITNESS_VAR_STRING = new WitnessVariable(REF_2, REF_2, DUMMY_POS);

    private static final BinaryTree.Node<Variable> LEAF_1 = new BinaryTree.Node<>(INSTANCE_VAR_INT);
    private static final BinaryTree.Node<Variable> LEAF_2 = new BinaryTree.Node<>(INSTANCE_VAR_STRING);
    private static final BinaryTree.Node<Variable> LEAF_3 = new BinaryTree.Node<>(WITNESS_VAR_INT);
    private static final BinaryTree.Node<Variable> LEAF_4 = new BinaryTree.Node<>(WITNESS_VAR_STRING);

    private static final BinaryTree<Variable> TREE_1 = new BinaryTree<>(new BinaryTree.Node<>(new BinaryTree.Node<>(LEAF_1, LEAF_4), new BinaryTree.Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<Variable> TREE_2 = new BinaryTree<>(new BinaryTree.Node<>(LEAF_1, new BinaryTree.Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<Variable> TREE_3 = new BinaryTree<>(new BinaryTree.Node<>(new BinaryTree.Node<>(LEAF_1, LEAF_4), LEAF_2));
    private static final BinaryTree<Variable> TREE_4 = new BinaryTree<>(new BinaryTree.Node<>(LEAF_1, LEAF_4));

    @Test
    void Root_Too_Large() {
        InstanceVariable instanceVariable = new InstanceVariable(new HexLiteral(SemanticsUtils.ED25519_PRIME_ORDER), null, DUMMY_POS);
        assertThrows(CompileTimeException.class, () -> {
            new MerkleTreeGadget(instanceVariable, TREE_1);
        });
    }

    @Test
    void Root_Negative() {
        assertThrows(CompileTimeException.class, () -> {
            new MerkleTreeGadget(INSTANCE_VAR_ROOT_NEG, TREE_1);
        });
    }

    @Test
    void Is_Equal_To() {
        MerkleTreeGadget merkleTreeGadget1 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_1, TREE_1);
        MerkleTreeGadget merkleTreeGadget2 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_1, TREE_1);
        assertTrue(merkleTreeGadget1.isEqualTo(merkleTreeGadget2));
    }

    @Test
    void Is_Not_Equal_To_1() {
        MerkleTreeGadget merkleTreeGadget1 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_1, TREE_1);
        MerkleTreeGadget merkleTreeGadget2 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_2, TREE_1);
        assertFalse(merkleTreeGadget1.isEqualTo(merkleTreeGadget2));
    }

    @Test
    void Is_Not_Equal_To_2() {
        MerkleTreeGadget merkleTreeGadget1 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_2, TREE_1);
        MerkleTreeGadget merkleTreeGadget2 = new MerkleTreeGadget(INSTANCE_VAR_ROOT_2, TREE_2);
        assertFalse(merkleTreeGadget1.isEqualTo(merkleTreeGadget2));
    }

}
