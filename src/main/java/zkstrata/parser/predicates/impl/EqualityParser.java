package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ast.predicates.Equality;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;

import java.util.List;

@ParserRule(name = "equality")
public class EqualityParser implements PredicateParser {
    @Override
    public Equality parse(ParserRuleContext ctx) {
        List<Value> values = ParserUtils.getValues(ctx);
        return new Equality(values.get(0), values.get(1), ParserUtils.getPosition(ctx.getStart()));
    }
}
