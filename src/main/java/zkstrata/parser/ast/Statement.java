package zkstrata.parser.ast;

import zkstrata.parser.ast.predicates.Predicate;

import java.util.List;

/**
 * AST root
 */
public class Statement {
    private String statement;
    private List<Subject> subjects;
    private List<Predicate> predicates;
    public Statement(String statement, List<Subject> subjects, List<Predicate> predicates) {
        this.statement = statement;
        this.subjects = subjects;
        this.predicates = predicates;
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
