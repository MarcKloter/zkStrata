package zkstrata.parser.predicates.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.parser.ast.predicates.LessThan;
import zkstrata.parser.ast.predicates.Predicate;
import zkstrata.parser.ast.types.Value;
import zkstrata.parser.predicates.ParserRule;
import zkstrata.parser.predicates.PredicateParser;
import zkstrata.utils.ParserUtils;
import zkstrata.zkStrata;

import java.util.List;

@ParserRule(name = "comparison")
public class ComparisonParser implements PredicateParser {
    @Override
    public Predicate parse(ParserRuleContext ctx) {
        zkStrata.ComparisonContext comparisonContext = (zkStrata.ComparisonContext) ctx;

        if (comparisonContext.less_than() != null) {
            List<Value> values = ParserUtils.getValues(comparisonContext.less_than());
            if (comparisonContext.less_than().instance_var() != null)
                return new BoundsCheck(values.get(0), null, values.get(1), ParserUtils.getPosition(ctx.getStart()));
            else
                return new LessThan(values.get(0), values.get(1), ParserUtils.getPosition(ctx.getStart()));
        }

        if (comparisonContext.greater_than() != null) {
            List<Value> values = ParserUtils.getValues(comparisonContext.greater_than());
            if (comparisonContext.greater_than().instance_var() != null)
                return new BoundsCheck(values.get(0), values.get(1), null, ParserUtils.getPosition(ctx.getStart()));
            else
                return new LessThan(values.get(1), values.get(0), ParserUtils.getPosition(ctx.getStart()));
        }

        throw new InternalCompilerException("Unknown comparison rule.");
    }
}
