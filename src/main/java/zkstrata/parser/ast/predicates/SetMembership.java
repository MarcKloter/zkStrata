package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;

import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.setMembership(member.toString(), set.stream().map(Value::toString).collect(Collectors.toSet()));
    }
}
