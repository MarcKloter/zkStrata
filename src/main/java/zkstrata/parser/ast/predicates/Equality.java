package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;

public class Equality extends Predicate {
    private Value leftHand;
    private Value rightHand;

    public Equality(Value left, Value right, Position position) {
        super(position);
        this.leftHand = left;
        this.rightHand = right;
    }

    public Value getLeftHand() {
        return leftHand;
    }

    public Value getRightHand() {
        return rightHand;
    }

    public void setLeftHand(Value leftHand) {
        this.leftHand = leftHand;
    }

    public void setRightHand(Value rightHand) {
        this.rightHand = rightHand;
    }
}
