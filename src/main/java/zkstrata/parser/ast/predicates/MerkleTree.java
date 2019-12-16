package zkstrata.parser.ast.predicates;

import org.antlr.v4.runtime.tree.ParseTree;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.Position;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.ParserUtils;
import zkstrata.utils.StatementBuilder;
import zkstrata.zkStrata;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MerkleTree extends Predicate {
    private Value root;
    private BinaryTree<Value> tree;

    public MerkleTree(Value root, BinaryTree<Value> tree, Position position) {
        super(position);
        this.root = root;
        this.tree = tree;
    }

    public static BinaryTree.Node<Value> visitSubtree(zkStrata.SubtreeContext ctx) {
        List<BinaryTree.Node<Value>> values = ctx.children.stream()
                .map(MerkleTree::visitChild)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new BinaryTree.Node<>(values.get(0), values.get(1));
    }

    private static BinaryTree.Node<Value> visitChild(ParseTree child) {
        if (child instanceof zkStrata.LeafContext) {
            ParseTreeVisitor.TypeVisitor typeVisitor = new ParseTreeVisitor.TypeVisitor();
            return new BinaryTree.Node<>(child.getChild(0).accept(typeVisitor));
        }

        if (child instanceof zkStrata.SubtreeContext)
            return visitSubtree((zkStrata.SubtreeContext) child);

        return null;
    }

    @ParserRule(name = "merkle_tree")
    public static MerkleTree parse(zkStrata.Merkle_treeContext ctx) {
        ParseTreeVisitor.TypeVisitor typeVisitor = new ParseTreeVisitor.TypeVisitor();
        Value root = ctx.instance_var().accept(typeVisitor);

        BinaryTree<Value> tree = new BinaryTree<>(visitSubtree(ctx.subtree()));

        return new MerkleTree(root, tree, ParserUtils.getPosition(ctx.getStart()));
    }

    public Value getRoot() {
        return root;
    }

    public BinaryTree<Value> getTree() {
        return tree;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.merkleTree(root.toString(), visitBinaryTree(tree));
    }

    private BinaryTree<String> visitBinaryTree(BinaryTree binaryTree) {
        return new BinaryTree<>(visitBinaryTreeNode(binaryTree.getRoot()));
    }

    private BinaryTree.Node<String> visitBinaryTreeNode(BinaryTree.Node node) {
        if (!node.isLeaf()) {
            return new BinaryTree.Node<>(visitBinaryTreeNode(node.getLeft()), visitBinaryTreeNode(node.getRight()));
        } else {
            if (node.getValue() instanceof Value)
                return new BinaryTree.Node<>(node.getValue().toString());
            else
                throw new InternalCompilerException("Expected BinaryTree.Node of class Value, found %s.",
                        node.getValue().getClass().getSimpleName());
        }
    }
}
