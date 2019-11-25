package zkstrata.parser.ast.predicates;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.exceptions.Position;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.ParserUtils;
import zkstrata.utils.StatementBuilder;

import java.util.List;

public class Inequality extends Predicate {
    private Value left;
    private Value right;

    public Inequality(Value left, Value right, Position position) {
        super(position);
        this.left = left;
        this.right = right;
    }

    @ParserRule(name = "inequality")
    public static Inequality parse(ParserRuleContext ctx) {
        List<Value> values = ParserUtils.getValues(ctx);
        return new Inequality(values.get(0), values.get(1), ParserUtils.getPosition(ctx.getStart()));
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.inequality(left.toString(), right.toString());
    }
}
