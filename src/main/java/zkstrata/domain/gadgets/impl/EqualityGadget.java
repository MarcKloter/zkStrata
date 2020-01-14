package zkstrata.domain.gadgets.impl;

import zkstrata.analysis.Contradiction;
import zkstrata.analysis.Implication;
import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.*;
import zkstrata.domain.visitor.AstElement;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.Equality;
import zkstrata.utils.Constants;

import java.util.*;

import static java.lang.String.format;
import static zkstrata.utils.CombinatoricsUtils.getParity;
import static zkstrata.utils.GadgetUtils.*;

@AstElement(Equality.class)
public class EqualityGadget extends AbstractGadget {
    @Type({Any.class})
    private Variable left;

    @Type({Any.class})
    private Variable right;

    public EqualityGadget() {
    }

    public EqualityGadget(Variable left, Variable right) {
        this.left = left;
        this.right = right;

        this.initialize();
    }

    @Implication
    public static Optional<Gadget> implyEquality(EqualityGadget eq1, EqualityGadget eq2) {
        List<Variable> parity = getParity(eq1.getLeft(), eq1.getRight(), eq2.getLeft(), eq2.getRight());
        if (new HashSet<>(parity).size() == 2)
            return Optional.of(new EqualityGadget(parity.get(0), parity.get(1)));

        return Optional.empty();
    }

    @Contradiction
    public static void checkContradiction(EqualityGadget eq) {
        if (isInstanceVariable(eq.getLeft()) && isInstanceVariable(eq.getRight())
                && !eq.getLeft().equals(eq.getRight()))
            throw new CompileTimeException("Contradiction.", List.of(eq.getLeft(), eq.getRight()));
    }

    @Substitution(target = {EqualityGadget.class})
    public static Optional<Proposition> removeWitnessEqualsSelf(EqualityGadget eq) {
        if (isWitnessVariable(eq.getLeft()) && isWitnessVariable(eq.getRight()) && eq.getLeft().equals(eq.getRight()))
            return Optional.of(Proposition.trueProposition());

        return Optional.empty();
    }

    @Substitution(target = {EqualityGadget.class})
    public static Optional<Proposition> removeInstanceEqualsInstance(EqualityGadget eq) {
        if (isInstanceVariable(eq.getLeft()) && isInstanceVariable(eq.getRight())
                && eq.getLeft().getValue().equals(eq.getRight().getValue()))
            return Optional.of(Proposition.trueProposition());

        return Optional.empty();
    }

    public static Optional<Variable> getEqualityToWitness(EqualityGadget eq, WitnessVariable var) {
        if (var.equals(eq.getLeft()))
            return Optional.of(eq.getRight());

        if (var.equals(eq.getRight()))
            return Optional.of(eq.getLeft());

        return Optional.empty();
    }

    @Override
    public void initialize() {
        checkTypeMismatch();
    }

    private void checkTypeMismatch() {
        if (left.getType() != right.getType())
            throw new CompileTimeException(format("Type mismatch: %s cannot be equal to %s.",
                    left.getType().getSimpleName(), right.getType().getSimpleName()), List.of(left, right));
    }

    @Override
    public int getCostEstimate() {
        return Constants.EQUALITY_COST_ESTIMATE;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        if (getClass() != object.getClass())
            return false;

        EqualityGadget other = (EqualityGadget) object;
        return (getLeft().equals(other.getLeft()) && getRight().equals(other.getRight())) ||
                (getRight().equals(other.getLeft())) && getLeft().equals(other.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight());
    }

    @Override
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        LinkedHashMap<String, Variable> args = new LinkedHashMap<>();
        args.put("left", left);
        args.put("right", right);
        return List.of(new BulletproofsGadgetsCodeLine("EQUALS %(left) %(right)", args));
    }

    public Variable getLeft() {
        return left;
    }

    public Variable getRight() {
        return right;
    }
}
