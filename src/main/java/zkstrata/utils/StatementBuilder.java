package zkstrata.utils;

import org.apache.commons.text.TextStringBuilder;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.parser.ast.Subject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StatementBuilder {
    private static final String PREFIX = "PROOF FOR ";
    private static final String INFIX = " THAT ";
    private static final String WITNESS = "";
    private static final String INSTANCE = "INSTANCE ";
    private static final String THIS = "THIS";

    private List<String> subjects = new ArrayList<>();
    private List<String> predicates = new ArrayList<>();

    private boolean fullBuildMode = true;
    private Conjunction conjunction = Conjunction.AND;

    public StatementBuilder() {

    }

    public StatementBuilder(AbstractSyntaxTree ast) {
        for (Subject subject : ast.getSubjects())
            subject(subject);

        ast.getRoot().addTo(this);
    }

    private StatementBuilder(boolean fullBuildMode, Conjunction conjunction) {
        this.fullBuildMode = fullBuildMode;
        this.conjunction = conjunction;
    }

    public static String stringLiteral(String string) {
        return String.format("'%s'", string);
    }

    public static String integerLiteral(BigInteger integer) {
        return String.format("%d", integer);
    }

    public static StatementBuilder or() {
        return new StatementBuilder(false, Conjunction.OR);
    }

    public static StatementBuilder and() {
        return new StatementBuilder(false, Conjunction.AND);
    }

    public StatementBuilder subject(String schema, String alias, boolean isWitness) {
        subjects.add(String.format("%s%s AS %s", isWitness ? WITNESS : INSTANCE, schema, alias));

        return this;
    }

    public StatementBuilder subject(Subject subject) {
        if (subject.getSchema().getName().equals(THIS)) {
            if (subject.isWitness())
                subjects.add(THIS);
        } else
            subject(subject.getSchema().getName(), subject.getAlias().getName(), subject.isWitness());

        return this;
    }

    public StatementBuilder conjunction(String predicate) {
        predicates.add(predicate);

        return this;
    }

    public StatementBuilder equality(String left, String right) {
        predicates.add(String.format("%s IS EQUAL TO %s", left, right));

        return this;
    }

    public StatementBuilder inequality(String left, String right) {
        predicates.add(String.format("%s IS UNEQUAL TO %s", left, right));

        return this;
    }

    public StatementBuilder boundsCheck(String value, String min, String max) {
        if (min == null && max == null)
            return this;
        else if (min == null)
            predicates.add(String.format("%s IS LESS THAN OR EQUAL TO %s", value, max));
        else if (max == null)
            predicates.add(String.format("%s IS GREATER THAN OR EQUAL TO %s", value, min));
        else
            predicates.add(String.format("%s IS LESS THAN OR EQUAL TO %s AND GREATER THAN OR EQUAL TO %s", value, max, min));

        return this;
    }

    public StatementBuilder mimcHash(String preimage, String image) {
        predicates.add(String.format("%s IS PREIMAGE OF %s", preimage, image));

        return this;
    }

    public StatementBuilder merkleTree(String root, BinaryTree<String> binaryTree) {
        StringBuilder stringBuilder = new StringBuilder();
        visitMerkleTree(binaryTree.getRoot(), stringBuilder);
        predicates.add(String.format("%s IS MERKLE ROOT OF %s", root, stringBuilder.toString()));

        return this;
    }

    private void visitMerkleTree(BinaryTree.Node<String> node, StringBuilder stringBuilder) {
        if (!node.isLeaf()) {
            stringBuilder.append('(');
            visitMerkleTree(node.getLeft(), stringBuilder);
            stringBuilder.append(", ");
            visitMerkleTree(node.getRight(), stringBuilder);
            stringBuilder.append(')');
        } else
            stringBuilder.append(node.getValue());
    }

    public StatementBuilder lessThan(String left, String right) {
        predicates.add(String.format("%s IS LESS THAN %s", left, right));

        return this;
    }

    public StatementBuilder greaterThan(String left, String right) {
        predicates.add(String.format("%s IS GREATER THAN %s", left, right));

        return this;
    }

    public StatementBuilder setMembership(String member, Set<String> set) {
        predicates.add(String.format("%s IS MEMBER OF (%s)", member, String.join(", ", set)));

        return this;
    }

    public String build() {
        if (fullBuildMode)
            return buildStatement();
        else
            return buildPredicates(conjunction);
    }

    private String buildPredicates(Conjunction conjunction) {
        return String.format("(%s)", String.join(conjunction.getEncoding(), predicates));
    }

    private String buildStatement() {
        TextStringBuilder builder = new TextStringBuilder();
        builder.append(PREFIX);
        builder.append(String.join(Conjunction.AND.getEncoding(), subjects));
        builder.append(INFIX);
        builder.append(buildPredicates(Conjunction.AND));
        return builder.build();
    }

    public enum Conjunction {
        AND(" AND "),
        OR(" OR ");

        private String encoding;

        Conjunction(String encoding) {
            this.encoding = encoding;
        }

        public String getEncoding() {
            return encoding;
        }
    }
}
