package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.predicates.MerkleTree;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.ParserUtils;
import zkstrata.zkStrata;
import zkstrata.zkStrataBaseVisitor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ParserRule(name = "merkle_tree")
public class MerkleTreeParser implements PredicateParser {
    @Override
    public MerkleTree parse(ParserRuleContext ctx) {
        ParseTreeVisitor.TypeVisitor typeVisitor = new ParseTreeVisitor.TypeVisitor();
        Value root = ((zkStrata.Merkle_treeContext) ctx).instance_var().accept(typeVisitor);

        BinaryTree<Value> tree = new BinaryTree<>(((zkStrata.Merkle_treeContext) ctx).subtree().accept(new SubtreeVisitor()));

        return new MerkleTree(root, tree, ParserUtils.getPosition(ctx.getStart()));
    }

    private class SubtreeVisitor extends zkStrataBaseVisitor<BinaryTree.Node<Value>> {
        @Override
        public BinaryTree.Node<Value> visitSubtree(zkStrata.SubtreeContext ctx) {
            List<BinaryTree.Node<Value>> values = ctx.children.stream()
                    .map(this::visitChild)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (values.size() != 2)
                throw new InternalCompilerException("Expected 2 children in subtree, found %s.", values.size());

            return new BinaryTree.Node<>(values.get(0), values.get(1));
        }

        private BinaryTree.Node<Value> visitChild(ParseTree child) {
            if (child instanceof zkStrata.LeafContext) {
                ParseTreeVisitor.TypeVisitor typeVisitor = new ParseTreeVisitor.TypeVisitor();
                return new BinaryTree.Node<>(child.getChild(0).accept(typeVisitor));
            }

            if (child instanceof zkStrata.SubtreeContext)
                return child.accept(new SubtreeVisitor());

            return null;
        }
    }
}
