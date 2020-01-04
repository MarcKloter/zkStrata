package zkstrata.domain.gadgets.impl;

import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ast.predicates.MerkleTree;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.Constants;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static zkstrata.utils.GadgetUtils.isInstanceVariable;

@AstElement(MerkleTree.class)
public class MerkleTreeGadget extends AbstractGadget {
    @Type({HexLiteral.class})
    private Variable root;

    private BinaryTree<Variable> tree;

    public MerkleTreeGadget() {
    }

    public MerkleTreeGadget(InstanceVariable root, BinaryTree<Variable> tree) {
        this.root = root;
        this.tree = tree;

        this.initialize();
    }

    @Override
    public void initialize() {
        checkRootHashImage();
    }

    private void checkRootHashImage() {
        if (isInstanceVariable(this.root)) {
            BigInteger image = (BigInteger) ((InstanceVariable) this.root).getValue().getValue();
            if (image.compareTo(Constants.ED25519_MAX_VALUE) > 0 || image.compareTo(BigInteger.ZERO) < 0)
                throw new CompileTimeException(String.format("Invalid root hash image. Images must be of prime order %s.",
                        Constants.ED25519_PRIME_ORDER), this.root);
        }
    }

    @Override
    public int getCostEstimate() {
        return Constants.MIMC_HASH_COST_ESTIMATE * (2 * tree.getRoot().countLeaves() - 1);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        if (getClass() != object.getClass())
            return false;

        MerkleTreeGadget other = (MerkleTreeGadget) object;
        return getRoot().equals(other.getRoot()) && getTree().equals(other.getTree());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoot(), getTree());
    }

    @Override
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        Map<String, Variable> args = new HashMap<>();
        args.put("root", root);
        StringBuilder stringBuilder = new StringBuilder("MERKLE %(root) ");
        visitTree(tree.getRoot(), stringBuilder, args);
        return List.of(new BulletproofsGadgetsCodeLine(stringBuilder.toString(), args));
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

    public Variable getRoot() {
        return root;
    }

    public BinaryTree<Variable> getTree() {
        return tree;
    }
}
