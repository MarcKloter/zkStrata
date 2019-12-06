package zkstrata.parser.ast;

import java.util.List;

public class AbstractSyntaxTree {
    private String source;
    private String statement;
    private List<Subject> subjects;
    private Node root;

    public AbstractSyntaxTree(String source, String statement, List<Subject> subjects, Node root) {
        this.source = source;
        this.statement = statement;
        this.subjects = subjects;
        this.root = root;
    }

    public String getSource() {
        return source;
    }

    public String getStatement() {
        return statement;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public Node getRoot() {
        return root;
    }
}
