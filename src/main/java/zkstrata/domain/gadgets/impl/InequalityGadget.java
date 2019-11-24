package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.Inequality;

import java.util.*;

@AstElement(Inequality.class)
public class InequalityGadget extends AbstractGadget<InequalityGadget> {
    private static final Logger LOGGER = LogManager.getLogger(InequalityGadget.class);

    @Type({Any.class})
    private Variable left;

    @Type({Any.class})
    private Variable right;

    public InequalityGadget() {
    }

    public InequalityGadget(Variable left, Variable right) {
        this.left = left;
        this.right = right;

        this.performChecks();
    }

    @Contradiction(propositions = {InequalityGadget.class})
    public static void checkSelfContradiction(InequalityGadget iq) {
        if (iq.getLeft() instanceof InstanceVariable && iq.getRight() instanceof InstanceVariable
                && iq.getLeft().equals(iq.getRight()))
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
    public static Set<Gadget> removeInstanceUnequalsInstance(InequalityGadget iq) {
        if (iq.getLeft() instanceof InstanceVariable && iq.getRight() instanceof InstanceVariable
                && !iq.getLeft().getValue().equals(iq.getRight().getValue())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed inequality predicate of two instance variables.");
            return Collections.emptySet();
        }

        return Set.of(iq);
    }

    @Override
    public void performChecks() {
        if (left.getType() != right.getType())
            throw new CompileTimeException("Type mismatch.", List.of(this.left, this.right));
    }

    @Override
    public boolean isEqualTo(InequalityGadget other) {
        return (left.equals(other.left) && right.equals(other.right)) ||
                (right.equals(other.left)) && left.equals(other.right);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("left", left),
                Map.entry("right", right)
        );
        return new TargetFormat("UNEQUAL %(left) %(right)", args);
    }

    public Variable getLeft() {
        return left;
    }

    public Variable getRight() {
        return right;
    }
}
