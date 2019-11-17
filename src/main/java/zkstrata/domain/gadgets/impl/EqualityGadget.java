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
    private Variable left;

    @Type({Any.class})
    private Variable right;

    public EqualityGadget() {
    }

    public EqualityGadget(Variable left, Variable right) {
        this.left = left;
        this.right = right;

        this.performChecks();
    }

    @Implication(assumption = {EqualityGadget.class, EqualityGadget.class})
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
    public static Set<Gadget> removeWitnessEqualsSelf(EqualityGadget eq) {
        if (eq.getLeft() instanceof WitnessVariable && eq.getRight() instanceof WitnessVariable
                && eq.getLeft().equals(eq.getRight())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed equality predicate of single witness variable.");
            return Collections.emptySet();
        }

        return Set.of(eq);
    }

    @Substitution(target = {EqualityGadget.class})
    public static Set<Gadget> removeInstanceEqualsInstance(EqualityGadget eq) {
        if (eq.getLeft() instanceof InstanceVariable && eq.getRight() instanceof InstanceVariable
                && eq.getLeft().getValue().equals(eq.getRight().getValue())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed equality predicate of two instance variables.");
            return Collections.emptySet();
        }

        return Set.of(eq);
    }

    /**
     * Checks whether two equality gadgets have a common witness variable and returns the other two variables.
     *
     * @param eq1 {@link EqualityGadget} to check
     * @param eq2 {@link EqualityGadget} to check
     * @return {@link List} containing two variables if the given gadgets have a common witness variable,
     * empty list otherwise.
     */
    private static List<Variable> getParity(EqualityGadget eq1, EqualityGadget eq2) {
        if (eq1.getLeft() instanceof WitnessVariable) {
            if (eq2.getLeft() instanceof WitnessVariable && eq1.getLeft().equals(eq2.getLeft()))
                return List.of(eq1.getRight(), eq2.getRight());
            if (eq2.getRight() instanceof WitnessVariable && eq1.getLeft().equals(eq2.getRight()))
                return List.of(eq1.getRight(), eq2.getLeft());
        }

        if (eq1.getRight() instanceof WitnessVariable) {
            if (eq2.getLeft() instanceof WitnessVariable && eq1.getRight().equals(eq2.getLeft()))
                return List.of(eq1.getLeft(), eq2.getRight());
            if (eq2.getRight() instanceof WitnessVariable && eq1.getRight().equals(eq2.getRight()))
                return List.of(eq1.getLeft(), eq2.getLeft());
        }

        return Collections.emptyList();
    }

    /**
     * Checks whether the given {@link WitnessVariable} is part of the provided {@link EqualityGadget},
     * If so, returns the {@link Variable} that the {@code var} is equal to, empty {@link Optional} otherwise.
     *
     * @param eq  {@link EqualityGadget} to analyze
     * @param var {@link WitnessVariable} to check for
     * @return {@link Optional} containing a {@link Variable} the given {@link WitnessVariable} is equal to
     */
    public static Optional<Variable> getEqual(EqualityGadget eq, WitnessVariable var) {
        if (eq.getLeft() instanceof WitnessVariable && eq.getLeft().equals(var))
            return Optional.of(eq.getRight());

        if (eq.getRight() instanceof WitnessVariable && eq.getRight().equals(var))
            return Optional.of(eq.getLeft());

        return Optional.empty();
    }

    @Override
    public void performChecks() {
        if (left.getType() != right.getType())
            throw new CompileTimeException("Type mismatch.", Set.of(this.left, this.right));
    }

    @Override
    public boolean isEqualTo(EqualityGadget other) {
        return (left.equals(other.left) && right.equals(other.right)) ||
                (right.equals(other.left)) && left.equals(other.right);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("left", left),
                Map.entry("right", right)
        );
        return new TargetFormat("EQUALS %(left) %(right)", args);
    }

    public Variable getLeft() {
        return left;
    }

    public Variable getRight() {
        return right;
    }
}
