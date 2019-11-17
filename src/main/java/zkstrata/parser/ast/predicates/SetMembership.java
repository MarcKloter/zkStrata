package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;

import java.util.Set;

public class SetMembership extends Predicate {
    private Value member;
    private Set<Value> set;

    public SetMembership(Value member, Set<Value> set, Position position) {
        super(position);
        this.member = member;
        this.set = set;
    }

    public Value getMember() {
        return member;
    }

    public Set<Value> getSet() {
        return set;
    }
}
