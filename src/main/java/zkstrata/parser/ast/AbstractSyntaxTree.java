package zkstrata.parser.ast;

import zkstrata.parser.ast.predicates.Predicate;

import java.util.List;

public class AbstractSyntaxTree {
    private String source;
    private String statement;
    private List<Subject> subjects;
    private List<Predicate> predicates;

    public AbstractSyntaxTree(String source, String statement, List<Subject> subjects, List<Predicate> predicates) {
        this.source = source;
        this.statement = statement;
        this.subjects = subjects;
        this.predicates = predicates;
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

    public List<Predicate> getPredicates() {
        return predicates;
    }
}
