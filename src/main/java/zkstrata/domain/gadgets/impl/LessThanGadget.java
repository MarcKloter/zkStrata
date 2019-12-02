package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.analysis.Implication;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.LessThan;

import java.math.BigInteger;
import java.util.*;

import static zkstrata.utils.GadgetUtils.*;

@AstElement(LessThan.class)
public class LessThanGadget extends AbstractGadget<LessThanGadget> {
    private static final Logger LOGGER = LogManager.getRootLogger();

    @Type({BigInteger.class})
    private WitnessVariable left;

    @Type({BigInteger.class})
    private WitnessVariable right;

    public LessThanGadget() {
    }

    public LessThanGadget(WitnessVariable left, WitnessVariable right) {
        this.left = left;
        this.right = right;

        this.performChecks();
    }

    @Contradiction(propositions = {LessThanGadget.class})
    public static void checkSelfContradiction(LessThanGadget lt) {
        if (lt.getLeft().equals(lt.getRight()))
            throw new CompileTimeException("Contradiction.", List.of(lt.getLeft(), lt.getRight()));
    }

    @Contradiction(propositions = {EqualityGadget.class, LessThanGadget.class})
    public static void checkEqualityContradiction(EqualityGadget eq, LessThanGadget lt) {
        if (eq.getLeft().equals(lt.getLeft()) && eq.getRight().equals(lt.getRight())
                || eq.getRight().equals(lt.getRight()) && eq.getRight().equals(lt.getLeft()))
            throw new CompileTimeException("Contradiction.",
                    List.of(eq.getLeft(), eq.getRight(), lt.getLeft(), lt.getRight()));
    }

    @Substitution(target = LessThanGadget.class, context = {EqualityGadget.class, EqualityGadget.class})
    public static Set<Gadget> removeExposedComparison(LessThanGadget lt, EqualityGadget eq1, EqualityGadget eq2) {
        Variable left = EqualityGadget.getEqual(eq1, lt.getLeft())
                .orElse(EqualityGadget.getEqual(eq2, lt.getLeft())
                        .orElse(null));

        Variable right = EqualityGadget.getEqual(eq1, lt.getRight())
                .orElse(EqualityGadget.getEqual(eq2, lt.getRight())
                        .orElse(null));

        if (isInstanceVariable(left) && isBigInteger(left) && isInstanceVariable(right) && isBigInteger(right)) {
            BigInteger leftValue = (BigInteger) ((InstanceVariable) left).getValue().getValue();
            BigInteger rightValue = (BigInteger) ((InstanceVariable) right).getValue().getValue();

            if (leftValue.compareTo(rightValue) < 0) {
                // TODO: maybe add statements information
                LOGGER.info("Removed a witness comparison of two exposed witnesses (tautology).");
                return Collections.emptySet();
            }
        }

        return Set.of(lt);
    }

    @Implication(assumption = {LessThanGadget.class, LessThanGadget.class})
    public static Optional<Gadget> implyTransitivity(LessThanGadget lt1, LessThanGadget lt2) {
        if (lt1.getRight().equals(lt2.getLeft()) && !lt1.getLeft().equals(lt2.getRight()))
            return Optional.of(new LessThanGadget(lt1.getLeft(), lt2.getRight()));

        if (lt1.getLeft().equals(lt2.getRight()) && !lt2.getLeft().equals(lt1.getRight()))
            return Optional.of(new LessThanGadget(lt2.getLeft(), lt1.getRight()));

        return Optional.empty();
    }

    @Implication(assumption = {LessThanGadget.class, EqualityGadget.class})
    public static Optional<Gadget> implyEquality(LessThanGadget lt, EqualityGadget eq) {
        Optional<Variable> left = EqualityGadget.getEqual(eq, lt.getLeft());
        if (left.isPresent() && isWitnessVariable(left.get()) && !left.get().equals(lt.getRight()))
            return Optional.of(new LessThanGadget((WitnessVariable) left.get(), lt.getRight()));

        Optional<Variable> right = EqualityGadget.getEqual(eq, lt.getRight());
        if (right.isPresent() && isWitnessVariable(right.get()) && !lt.getLeft().equals(right.get()))
            return Optional.of(new LessThanGadget(lt.getLeft(), (WitnessVariable) right.get()));

        return Optional.empty();
    }

    @Implication(assumption = {LessThanGadget.class, BoundsCheckGadget.class})
    public static Optional<Gadget> implyBounds(LessThanGadget lt, BoundsCheckGadget bc) {
        if (bc.getValue().equals(lt.getLeft()))
            return Optional.of(new BoundsCheckGadget(lt.getRight(), bc.getMin(), null));

        if (bc.getValue().equals(lt.getRight()))
            return Optional.of(new BoundsCheckGadget(lt.getLeft(), null, bc.getMax()));

        return Optional.empty();
    }

    @Override
    public boolean isEqualTo(LessThanGadget other) {
        return left.equals(other.left) && right.equals(other.right);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("left", left),
                Map.entry("right", right)
        );
        return new TargetFormat("LESS_THAN %(left) %(right)", args);
    }

    public WitnessVariable getLeft() {
        return left;
    }

    public WitnessVariable getRight() {
        return right;
    }
}
