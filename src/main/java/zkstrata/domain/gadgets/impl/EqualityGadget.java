package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.analysis.Implication;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.*;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.Equality;

import java.util.*;

@AstElement(Equality.class)
public class EqualityGadget extends AbstractGadget<EqualityGadget> {
    private static final Logger LOGGER = LogManager.getLogger(EqualityGadget.class);

    @Type({Any.class})
    private Variable leftHand;

    @Type({Any.class})
    private Variable rightHand;

    public EqualityGadget() {

    }

    public EqualityGadget(Variable leftHand, Variable rightHand) {
        this.leftHand = leftHand;
        this.rightHand = rightHand;

        this.performChecks();
    }

    @Implication(premise = {EqualityGadget.class, EqualityGadget.class})
    public static Optional<Gadget> implyEquality(EqualityGadget eq1, EqualityGadget eq2) {
        List<Variable> parity = getParity(eq1, eq2);
        if (new HashSet<>(parity).size() == 2)
            return Optional.of(new EqualityGadget(parity.get(0), parity.get(1)));

        return Optional.empty();
    }

    @Contradiction(propositions = {EqualityGadget.class})
    public static void checkContradiction(EqualityGadget eq) {
        if (eq.getLeft() instanceof InstanceVariable && eq.getRight() instanceof InstanceVariable
                && !eq.getLeft().equals(eq.getRight()))
            throw new CompileTimeException("Contradiction.", Set.of(eq.getLeft(), eq.getRight()));
    }

    @Substitution(target = {EqualityGadget.class})
    public static List<Gadget> removeWitnessEqualsSelf(EqualityGadget eq) {
        if (eq.getLeft() instanceof WitnessVariable && eq.getRight() instanceof WitnessVariable
                && eq.getLeft().equals(eq.getRight())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed equality predicate of single witness variable.");
            return Collections.emptyList();
        }

        return List.of(eq);
    }

    @Substitution(target = {EqualityGadget.class})
    public static List<Gadget> removeInstanceEqualsInstance(EqualityGadget eq) {
        if (eq.getLeft() instanceof InstanceVariable && eq.getRight() instanceof InstanceVariable
                && eq.getLeft().getValue().equals(eq.getRight().getValue())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed equality predicate of two instance variables.");
            return Collections.emptyList();
        }

        return List.of(eq);
    }

    /**
     * Checks whether two equality gadgets have a common witness variable and returns the other two variables.
     *
     * @param eq1 {@link EqualityGadget} to check
     * @param eq2 {@link EqualityGadget} to check
     * @return {@link List} containing two variables if the given gadgets have a common witness variable, empty list otherwise.
     */
    private static List<Variable> getParity(EqualityGadget eq1, EqualityGadget eq2) {
        if (eq1.getLeft() instanceof WitnessVariable) {
            if (eq2.getLeft() instanceof WitnessVariable && eq1.getLeft().equals(eq2.getLeft()))
                return List.of(eq1.getRight(), eq2.getRight());
            if (eq2.getRight() instanceof WitnessVariable && eq1.getLeft().equals(eq2.getRight()))
                return List.of(eq1.getRight(), eq2.getLeft());
        } else if (eq1.getRight() instanceof WitnessVariable) {
            if (eq2.getLeft() instanceof WitnessVariable && eq1.getRight().equals(eq2.getLeft()))
                return List.of(eq1.getLeft(), eq2.getRight());
            if (eq2.getRight() instanceof WitnessVariable && eq1.getRight().equals(eq2.getRight()))
                return List.of(eq1.getLeft(), eq2.getLeft());
        }

        return Collections.emptyList();
    }

    public Variable getLeft() {
        return leftHand;
    }

    public Variable getRight() {
        return rightHand;
    }

    @Override
    public void performChecks() {
        if (leftHand.getType() != rightHand.getType())
            throw new CompileTimeException("Type mismatch.", Set.of(this.leftHand, this.rightHand));
    }

    @Override
    public boolean isEqualTo(EqualityGadget other) {
        return (leftHand.equals(other.leftHand) && rightHand.equals(other.rightHand)) ||
                (rightHand.equals(other.leftHand)) && leftHand.equals(other.rightHand);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("leftHand", leftHand),
                Map.entry("rightHand", rightHand)
        );
        return new TargetFormat("EQUALS %(leftHand) %(rightHand)", args);
    }
}
