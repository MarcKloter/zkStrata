package zkstrata.parser.ast;

import java.util.List;

public class AbstractSyntaxTree {
    private String source;
    private String statement;
    private List<Subject> subjects;
    private Clause clause;

    public AbstractSyntaxTree(String source, String statement, List<Subject> subjects, Clause clause) {
        this.source = source;
        this.statement = statement;
        this.subjects = subjects;
        this.clause = clause;
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

    public Clause getClause() {
        return clause;
    }
}
