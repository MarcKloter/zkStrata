package zkstrata.parser.ast.predicates;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;
import zkstrata.zkStrata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static zkstrata.utils.ParserUtils.getValues;

public class SetMembership extends Predicate {
    private Value member;
    private Set<Value> set;

    public SetMembership(Value member, Set<Value> set) {
        this.member = member;
        this.set = set;
    }

    @ParserRule(name = "set_membership")
    public static SetMembership parse(ParserRuleContext ctx) {
        zkStrata.Set_membershipContext setMembershipContext = (zkStrata.Set_membershipContext) ctx;
        List<Value> member = getValues(setMembershipContext);
        List<Value> set = getValues(setMembershipContext.set());

        return new SetMembership(member.get(0), new HashSet<>(set));
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
