package zkstrata.parser.ast.predicates;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.Position;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.ParserUtils;
import zkstrata.utils.StatementBuilder;
import zkstrata.zkStrata;

import java.util.List;

public class BoundsCheck extends Predicate {
    private Value value;
    private Value min;
    private Value max;

    public BoundsCheck(Value value, Value min, Value max, Position tokenInfo) {
        super(tokenInfo);
        this.value = value;
        this.min = min;
        this.max = max;
    }

    @ParserRule(name = "bounds_check")
    public static BoundsCheck parse(ParserRuleContext ctx) {
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

    public Value getValue() {
        return value;
    }

    public Value getMin() {
        return min;
    }

    public Value getMax() {
        return max;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.boundsCheck(
                value.toString(),
                min == null ? null : min.toString(),
                max == null ? null : max.toString()
        );
    }
}
