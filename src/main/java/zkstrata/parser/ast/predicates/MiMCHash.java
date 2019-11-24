package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;

public class MiMCHash extends Predicate {
    private Value preimage;
    private Value image;

    public MiMCHash(Value preimage, Value image, Position position) {
        super(position);
        this.preimage = preimage;
        this.image = image;
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
