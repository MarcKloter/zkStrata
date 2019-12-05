package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.analysis.Implication;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.*;
import zkstrata.domain.visitor.AstElement;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.Equality;
import zkstrata.utils.CombinatoricsUtils;

import java.util.*;

import static zkstrata.utils.GadgetUtils.*;

@AstElement(Equality.class)
public class EqualityGadget extends AbstractGadget<EqualityGadget> {
    private static final Logger LOGGER = LogManager.getRootLogger();

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
        List<Variable> parity = CombinatoricsUtils.getParity(eq1.getLeft(), eq1.getRight(), eq2.getLeft(), eq2.getRight());
        if (new HashSet<>(parity).size() == 2)
            return Optional.of(new EqualityGadget(parity.get(0), parity.get(1)));

        return Optional.empty();
    }

    @Contradiction(propositions = {EqualityGadget.class})
    public static void checkContradiction(EqualityGadget eq) {
        if (isInstanceVariable(eq.getLeft()) && isInstanceVariable(eq.getRight())
                && !eq.getLeft().equals(eq.getRight()))
            throw new CompileTimeException("Contradiction.", List.of(eq.getLeft(), eq.getRight()));
    }

    @Substitution(target = {EqualityGadget.class})
    public static Set<Gadget> removeWitnessEqualsSelf(EqualityGadget eq) {
        if (isWitnessVariable(eq.getLeft()) && isWitnessVariable(eq.getRight()) && eq.getLeft().equals(eq.getRight())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed equality predicate of single witness variable.");
            return Collections.emptySet();
        }

        return Set.of(eq);
    }

    @Substitution(target = {EqualityGadget.class})
    public static Set<Gadget> removeInstanceEqualsInstance(EqualityGadget eq) {
        if (isInstanceVariable(eq.getLeft()) && isInstanceVariable(eq.getRight())
                && eq.getLeft().getValue().equals(eq.getRight().getValue())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed equality predicate of two instance variables.");
            return Collections.emptySet();
        }

        return Set.of(eq);
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
        if (isWitnessVariable(eq.getLeft()) && eq.getLeft().equals(var))
            return Optional.of(eq.getRight());

        if (isWitnessVariable(eq.getRight()) && eq.getRight().equals(var))
            return Optional.of(eq.getLeft());

        return Optional.empty();
    }

    @Override
    public void performChecks() {
        if (left.getType() != right.getType())
            throw new CompileTimeException("Type mismatch.", List.of(this.left, this.right));
    }

    @Override
    public boolean isEqualTo(EqualityGadget other) {
        return (left.equals(other.left) && right.equals(other.right)) ||
                (right.equals(other.left)) && left.equals(other.right);
    }

    @Override
    public List<TargetFormat> toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("left", left),
                Map.entry("right", right)
        );
        return List.of(new TargetFormat("EQUALS %(left) %(right)", args));
    }

    public Variable getLeft() {
        return left;
    }

    public Variable getRight() {
        return right;
    }
}
