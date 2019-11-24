package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ast.predicates.Inequality;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;

import java.util.List;

@ParserRule(name = "inequality")
public class InequalityParser implements PredicateParser {
    @Override
    public Inequality parse(ParserRuleContext ctx) {
        List<Value> values = ParserUtils.getValues(ctx);
        return new Inequality(values.get(0), values.get(1), ParserUtils.getPosition(ctx.getStart()));
    }
}
