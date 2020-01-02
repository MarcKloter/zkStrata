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
    private Boolean strict;

    public LessThan(Value left, Value right, Boolean strict) {
        this.left = left;
        this.right = right;
        this.strict = strict;
    }

    @ParserRule(name = "comparison")
    public static Predicate parse(zkStrata.ComparisonContext ctx) {
        if (ctx.less_than() != null)
            return parseLessThan(ctx.less_than());

        if (ctx.less_than_eq() != null)
            return parseLessThanEq(ctx.less_than_eq());

        if (ctx.greater_than() != null)
            return parseGreaterThan(ctx.greater_than());

        if (ctx.greater_than_eq() != null)
            return parseGreaterThanEq(ctx.greater_than_eq());

        throw new InternalCompilerException("Unknown comparison rule.");
    }

    private static Predicate parseLessThan(zkStrata.Less_thanContext ctx) {
        List<Value> values = getValues(ctx);
        return new LessThan(values.get(0), values.get(1), true);
    }

    private static Predicate parseLessThanEq(zkStrata.Less_than_eqContext ctx) {
        List<Value> values = getValues(ctx);
        return new LessThan(values.get(0), values.get(1), false);
    }

    private static Predicate parseGreaterThan(zkStrata.Greater_thanContext ctx) {
        List<Value> values = getValues(ctx);
        return new LessThan(values.get(1), values.get(0), true);
    }

    private static Predicate parseGreaterThanEq(zkStrata.Greater_than_eqContext ctx) {
        List<Value> values = getValues(ctx);
        return new LessThan(values.get(1), values.get(0), false);
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }

    public Boolean getStrict() {
        return strict;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.lessThan(left.toString(), right.toString());
    }
}
