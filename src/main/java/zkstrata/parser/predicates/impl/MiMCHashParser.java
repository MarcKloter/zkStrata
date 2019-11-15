package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ast.predicates.MiMCHash;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;

import java.util.List;

@ParserRule(name = "mimc_hash")
public class MiMCHashParser implements PredicateParser {
    @Override
    public MiMCHash parse(ParserRuleContext ctx) {
        List<Value> values = ParserUtils.getValues(ctx);
        return new MiMCHash(values.get(0), values.get(1), ParserUtils.getPosition(ctx.getStart()));
    }
}