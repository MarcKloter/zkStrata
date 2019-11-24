package zkstrata.domain.gadgets.impl;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ast.predicates.MerkleTree;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.Constants;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@AstElement(MerkleTree.class)
public class MerkleTreeGadget extends AbstractGadget<MerkleTreeGadget> {
    @Type({HexLiteral.class})
    private InstanceVariable root;

    private BinaryTree<Variable> tree;

    public MerkleTreeGadget() {
    }

    public MerkleTreeGadget(InstanceVariable root, BinaryTree<Variable> tree) {
        this.root = root;
        this.tree = tree;

        this.performChecks();
    }
// TODO: All instance variables warning (we don't know whether this fails or succeeds)

    @Override
    public void performChecks() {
        BigInteger image = (BigInteger) ((this.root.getValue()).getValue());
        if (image.compareTo(Constants.ED25519_MAX_VALUE) > 0
                || image.compareTo(BigInteger.ZERO) < 0)
            throw new CompileTimeException(String.format("Invalid root hash image. Images must be of prime order %s.",
                    Constants.ED25519_PRIME_ORDER), this.root);

    }

    @Override
    public boolean isEqualTo(MerkleTreeGadget other) {
        return root.equals(other.root) && tree.equals(other.tree);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = new HashMap<>();
        args.put("root", root);
        StringBuilder stringBuilder = new StringBuilder("MERKLE %(root) ");
        visitTree(tree.getRoot(), stringBuilder, args);
        return new TargetFormat(stringBuilder.toString(), args);
    }

    private void visitTree(BinaryTree.Node<Variable> node, StringBuilder stringBuilder, Map<String, Variable> args) {
        if (!node.isLeaf()) {
            stringBuilder.append('(');
            visitTree(node.getLeft(), stringBuilder, args);
            stringBuilder.append(' ');
            visitTree(node.getRight(), stringBuilder, args);
            stringBuilder.append(')');
        } else {
            String key = String.format("var%s", args.size());
            args.put(key, node.getValue());
            stringBuilder.append(String.format("%%(%s)", key));
        }
    }

    public InstanceVariable getRoot() {
        return root;
    }

    public BinaryTree<Variable> getTree() {
        return tree;
    }
}
