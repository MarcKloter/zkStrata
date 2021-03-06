package zkstrata.parser.ast.predicates;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;

import java.util.List;

import static zkstrata.utils.ParserUtils.getValues;

public class Equality extends Predicate {
    private Value left;
    private Value right;

    public Equality(Value left, Value right) {
        this.left = left;
        this.right = right;
    }

    @ParserRule(name = "equality")
    public static Equality parse(ParserRuleContext ctx) {
        List<Value> values = getValues(ctx);
        return new Equality(values.get(0), values.get(1));
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.equality(left.toString(), right.toString());
    }
}
