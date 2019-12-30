package zkstrata.parser.ast.predicates;

import org.antlr.v4.runtime.ParserRuleContext;
import zkstrata.parser.ParserRule;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;

import java.util.List;

import static zkstrata.utils.ParserUtils.getValues;

public class MiMCHash extends Predicate {
    private Value preimage;
    private Value image;

    public MiMCHash(Value preimage, Value image) {
        this.preimage = preimage;
        this.image = image;
    }

    @ParserRule(name = "mimc_hash")
    public static MiMCHash parse(ParserRuleContext ctx) {
        List<Value> values = getValues(ctx);
        return new MiMCHash(values.get(0), values.get(1));
    }

    public Value getPreimage() {
        return preimage;
    }

    public Value getImage() {
        return image;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.mimcHash(preimage.toString(), image.toString());
    }
}
