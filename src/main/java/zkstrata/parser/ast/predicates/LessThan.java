package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.Position;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.IntegerLiteral;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.ParserUtils;
import zkstrata.utils.StatementBuilder;
import zkstrata.zkStrata;

import java.math.BigInteger;
import java.util.List;

public class LessThan extends Predicate {
    private Value left;
    private Value right;

    public LessThan(Value left, Value right, Position position) {
        super(position);
        this.left = left;
        this.right = right;
    }

    @ParserRule(name = "comparison")
    public static Predicate parse(zkStrata.ComparisonContext ctx) {
        if (ctx.less_than() != null) {
            List<Value> values = ParserUtils.getValues(ctx.less_than());
            if (ctx.less_than().instance_var() != null)
                return new BoundsCheck(values.get(0), null, trySubtractOne(values.get(1)), ParserUtils.getPosition(ctx.getStart()));
            else
                return new LessThan(values.get(0), values.get(1), ParserUtils.getPosition(ctx.getStart()));
        }

        if (ctx.greater_than() != null) {
            List<Value> values = ParserUtils.getValues(ctx.greater_than());
            if (ctx.greater_than().instance_var() != null)
                return new BoundsCheck(values.get(0), tryAddOne(values.get(1)), null, ParserUtils.getPosition(ctx.getStart()));
            else
                return new LessThan(values.get(1), values.get(0), ParserUtils.getPosition(ctx.getStart()));
        }

        throw new InternalCompilerException("Unknown comparison rule.");
    }

    /**
     * Tries to add one to the given {@link Value}.
     * <p>
     * This is required because the {@link BoundsCheck} is defined as MIN <= WITNESS <= MAX.
     *
     * @param value {@link Value} to add one to.
     * @return {@link IntegerLiteral} containing the given value + 1, or {@code value} if not a numeric type
     */
    private static Value tryAddOne(Value value) {
        if (value instanceof IntegerLiteral)
            return new IntegerLiteral(((IntegerLiteral) value).getValue().add(BigInteger.ONE).toString(), value.getPosition());
        else
            return value;
    }

    /**
     * Tries to subtract one from the given {@link Value}.
     * <p>
     * This is required because the {@link BoundsCheck} is defined as MIN <= WITNESS <= MAX.
     *
     * @param value {@link Value} to subtract one from.
     * @return {@link IntegerLiteral} containing the given value - 1, or {@code value} if not a numeric type
     */
    private static Value trySubtractOne(Value value) {
        if (value instanceof IntegerLiteral)
            return new IntegerLiteral(((IntegerLiteral) value).getValue().subtract(BigInteger.ONE).toString(), value.getPosition());
        else
            return value;
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
