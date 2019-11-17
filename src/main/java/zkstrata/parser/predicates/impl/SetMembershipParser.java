package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ast.predicates.SetMembership;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;
import zkstrata.zkStrata;

import java.util.HashSet;
import java.util.List;

@ParserRule(name = "set_membership")
public class SetMembershipParser implements PredicateParser {
    @Override
    public SetMembership parse(ParserRuleContext ctx) {
        zkStrata.Set_membershipContext setMembershipContext = (zkStrata.Set_membershipContext) ctx;
        List<Value> member = ParserUtils.getValues(setMembershipContext);
        List<Value> set = ParserUtils.getValues(setMembershipContext.set());

        return new SetMembership(member.get(0), new HashSet<>(set), ParserUtils.getPosition(ctx.getStart()));
    }
}
