package zkstrata.parser.ast;

import zkstrata.parser.ast.predicates.Predicate;

import java.util.List;

/**
 * AST root
 */
public class Statement {
    private List<Subject> subjects;
    private List<Predicate> predicates;
    public Statement(List<Subject> subjects, List<Predicate> predicates) {
        this.subjects = subjects;
        this.predicates = predicates;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }
}
