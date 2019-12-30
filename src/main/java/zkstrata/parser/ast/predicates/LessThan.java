package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.InternalCompilerException;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;
import zkstrata.zkStrata;

import java.util.List;

import static zkstrata.utils.ParserUtils.*;

public class LessThan extends Predicate {
    private Value left;
    private Value right;

    public LessThan(Value left, Value right) {
        this.left = left;
        this.right = right;
    }

    @ParserRule(name = "comparison")
    public static Predicate parse(zkStrata.ComparisonContext ctx) {
        if (ctx.less_than() != null) {
            List<Value> values = getValues(ctx.less_than());
            if (ctx.less_than().instance_var() != null)
                return new BoundsCheck(values.get(0), null, values.get(1), true);
            else
                return new LessThan(values.get(0), values.get(1));
        }

        if (ctx.greater_than() != null) {
            List<Value> values = getValues(ctx.greater_than());
            if (ctx.greater_than().instance_var() != null)
                return new BoundsCheck(values.get(0), values.get(1), null, true);
            else
                return new LessThan(values.get(1), values.get(0));
        }

        throw new InternalCompilerException("Unknown comparison rule.");
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.lessThan(left.toString(), right.toString());
    }
}
