package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;
import zkstrata.zkStrata;

import java.util.List;

@ParserRule(name = "bounds_check")
public class BoundsCheckParser implements PredicateParser {
    @Override
    public BoundsCheck parse(ParserRuleContext ctx) {
        zkStrata.Bounds_checkContext boundsCheckContext = (zkStrata.Bounds_checkContext) ctx;

        if (boundsCheckContext.min_max() != null) {
            List<Value> values = ParserUtils.getValues(boundsCheckContext.min_max());
            return new BoundsCheck(values.get(0), values.get(1), values.get(2), ParserUtils.getPosition(ctx.getStart()));
        }

        if (boundsCheckContext.max_min() != null) {
            List<Value> values = ParserUtils.getValues(boundsCheckContext.max_min());
            return new BoundsCheck(values.get(0), values.get(2), values.get(1), ParserUtils.getPosition(ctx.getStart()));
        }

        throw new InternalCompilerException("Unknown bounds_check rule.");
    }
}
