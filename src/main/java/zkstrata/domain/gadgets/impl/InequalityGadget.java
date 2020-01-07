package zkstrata.domain.gadgets.impl;

import zkstrata.analysis.Contradiction;
import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.Inequality;
import zkstrata.utils.Constants;

import java.util.*;

import static zkstrata.domain.gadgets.impl.BoundsCheckGadget.isContainedInBounds;
import static zkstrata.utils.GadgetUtils.*;

@AstElement(Inequality.class)
public class InequalityGadget extends AbstractGadget {
    @Type({Any.class})
    private Variable left;

    @Type({Any.class})
    private Variable right;

    public InequalityGadget() {
    }

    public InequalityGadget(Variable left, Variable right) {
        this.left = left;
        this.right = right;

        this.initialize();
    }

    @Contradiction
    public static void checkSelfContradiction(InequalityGadget iq) {
        if (isInstanceVariable(iq.getLeft()) && isInstanceVariable(iq.getRight()) && iq.getLeft().equals(iq.getRight()))
            throw new CompileTimeException("Contradiction.", List.of(iq.getLeft(), iq.getRight()));
    }

    @Contradiction
    public static void checkEqualityContradiction(EqualityGadget eq, InequalityGadget iq) {
        if (eq.getLeft().equals(iq.getRight()) && eq.getRight().equals(iq.getLeft())
                || eq.getLeft().equals(iq.getLeft()) && eq.getRight().equals(iq.getRight()))
            throw new CompileTimeException("Contradiction.",
                    List.of(eq.getLeft(), eq.getRight(), iq.getLeft(), iq.getRight()));
    }

    @Substitution(target = {InequalityGadget.class})
    public static Optional<Proposition> removeInstanceUnequalsInstance(InequalityGadget iq) {
        if (isInstanceVariable(iq.getLeft()) && isInstanceVariable(iq.getRight())
                && !iq.getLeft().getValue().equals(iq.getRight().getValue())) {
            return Optional.of(Proposition.trueProposition());
        }

        return Optional.empty();
    }

    @Substitution(target = {InequalityGadget.class}, context = {BoundsCheckGadget.class})
    public static Optional<Proposition> removeInequalityOutsideOfBounds(InequalityGadget iq, BoundsCheckGadget bc) {
        if (isWitnessVariable(bc.getValue())) {
            Optional<Variable> disparity = getDisparityToWitness(iq, (WitnessVariable) bc.getValue());

            if (disparity.isPresent() && !isContainedInBounds(disparity.get(), bc))
                return Optional.of(Proposition.trueProposition());
        }

        return Optional.empty();
    }

    public static Optional<Variable> getDisparityToWitness(InequalityGadget iq, WitnessVariable var) {
        if (var.equals(iq.getLeft()))
            return Optional.of(iq.getRight());

        if (var.equals(iq.getRight()))
            return Optional.of(iq.getLeft());

        return Optional.empty();
    }

    @Override
    public void initialize() {
        if (left.getType() != right.getType())
            throw new CompileTimeException("Type mismatch.", List.of(this.left, this.right));
    }

    @Override
    public int getCostEstimate() {
        return Constants.INEQUALITY_COST_ESTIMATE;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        if (getClass() != object.getClass())
            return false;

        InequalityGadget other = (InequalityGadget) object;
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
        return List.of(new BulletproofsGadgetsCodeLine("UNEQUAL %(left) %(right)", args));
    }

    public Variable getLeft() {
        return left;
    }

    public Variable getRight() {
        return right;
    }
}
