package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.Inequality;
import zkstrata.utils.Constants;

import java.util.*;

import static zkstrata.utils.GadgetUtils.*;

@AstElement(Inequality.class)
public class InequalityGadget extends AbstractGadget {
    private static final Logger LOGGER = LogManager.getRootLogger();

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

    @Contradiction(propositions = {InequalityGadget.class})
    public static void checkSelfContradiction(InequalityGadget iq) {
        if (isInstanceVariable(iq.getLeft()) && isInstanceVariable(iq.getRight()) && iq.getLeft().equals(iq.getRight()))
            throw new CompileTimeException("Contradiction.", List.of(iq.getLeft(), iq.getRight()));
    }

    @Contradiction(propositions = {EqualityGadget.class, InequalityGadget.class})
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
            LOGGER.info("Removed inequality predicate of two instance variables.");
            return Optional.of(Proposition.trueProposition());
        }

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
    public List<TargetFormat> toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("left", left),
                Map.entry("right", right)
        );
        return List.of(new TargetFormat("UNEQUAL %(left) %(right)", args));
    }

    public Variable getLeft() {
        return left;
    }

    public Variable getRight() {
        return right;
    }
}
