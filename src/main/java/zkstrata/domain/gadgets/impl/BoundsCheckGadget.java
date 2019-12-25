package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.analysis.Implication;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Null;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.utils.Constants;

import java.math.BigInteger;
import java.util.*;

import static zkstrata.utils.GadgetUtils.*;

@AstElement(BoundsCheck.class)
public class BoundsCheckGadget extends AbstractGadget {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final BigInteger MIN_VALUE = BigInteger.ZERO;
    private static final BigInteger MAX_VALUE = Constants.UNSIGNED_64BIT_MAX;

    @Type({BigInteger.class})
    private WitnessVariable value;

    @Type({Null.class, BigInteger.class})
    private InstanceVariable min;

    @Type({Null.class, BigInteger.class})
    private InstanceVariable max;

    public BoundsCheckGadget() {
    }

    public BoundsCheckGadget(WitnessVariable value, InstanceVariable min, InstanceVariable max) {
        this.value = value;
        this.min = min;
        this.max = max;

        this.performChecks();
    }

    @Implication(assumption = {EqualityGadget.class, BoundsCheckGadget.class})
    public static Optional<Gadget> implyBounds(EqualityGadget eq, BoundsCheckGadget bc) {
        if (eq.getLeft().equals(bc.getValue()) && eq.getRight() instanceof WitnessVariable)
            return Optional.of(new BoundsCheckGadget((WitnessVariable) eq.getRight(), bc.getMin(), bc.getMax()));

        if (eq.getRight().equals(bc.getValue()) && eq.getLeft() instanceof WitnessVariable)
            return Optional.of(new BoundsCheckGadget((WitnessVariable) eq.getLeft(), bc.getMin(), bc.getMax()));

        return Optional.empty();
    }

    @Implication(assumption = {BoundsCheckGadget.class})
    public static Optional<Gadget> implyEquality(BoundsCheckGadget bc) {
        if (bc.getMaxValue().subtract(bc.getMinValue()).equals(BigInteger.valueOf(0))) {
            return Optional.of(new EqualityGadget(bc.getValue(), bc.getMin()));
        }

        return Optional.empty();
    }

    @Contradiction(propositions = {BoundsCheckGadget.class, BoundsCheckGadget.class})
    public static void checkTwoBoundsChecksContradiction(BoundsCheckGadget bc1, BoundsCheckGadget bc2) {
        if (bc1.getValue().equals(bc2.getValue())) {
            if (bc1.getMinValue().compareTo(bc2.getMaxValue()) > 0)
                throw new CompileTimeException("Contradiction.", List.of(bc1.getMin(), bc2.getMax()));

            if (bc1.getMaxValue().compareTo(bc2.getMinValue()) < 0)
                throw new CompileTimeException("Contradiction.", List.of(bc1.getMax(), bc2.getMin()));
        }
    }

    @Contradiction(propositions = {BoundsCheckGadget.class})
    public static void checkSelfBoundsContradiction(BoundsCheckGadget bc) {
        if (bc.getMinValue().compareTo(bc.getMaxValue()) > 0)
            throw new CompileTimeException("Contradiction.", List.of(bc.getMin(), bc.getMax()));
    }

    @Contradiction(propositions = {EqualityGadget.class, BoundsCheckGadget.class})
    public static void checkEqualityBoundsContradiction(EqualityGadget eq, BoundsCheckGadget bc) {
        Optional<Variable> equal = EqualityGadget.getEqual(eq, bc.getValue());

        if (equal.isPresent() && isInstanceVariable(equal.get())) {
            BigInteger value = (BigInteger) ((Literal) equal.get().getValue()).getValue();
            if (value.compareTo(bc.getMinValue()) < 0)
                throw new CompileTimeException("Contradiction.", List.of(eq.getRight(), bc.getMin()));
            if (value.compareTo(bc.getMaxValue()) > 0)
                throw new CompileTimeException("Contradiction.", List.of(eq.getRight(), bc.getMax()));
        }
    }

    @Substitution(target = {BoundsCheckGadget.class})
    public static Optional<Proposition> replaceEquality1(BoundsCheckGadget bc) {
        if (bc.getMaxValue().subtract(bc.getMinValue()).equals(BigInteger.valueOf(0))) {
            LOGGER.info("Replaced bounds predicate with max = min by equality predicate.");
            return Optional.of(new EqualityGadget(bc.getValue(), bc.getMin()));
        }

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class, BoundsCheckGadget.class})
    public static Optional<Proposition> replaceEquality2(BoundsCheckGadget bc1, BoundsCheckGadget bc2) {
        if (bc1.getValue().equals(bc2.getValue())) {
            if (bc1.getMaxValue().subtract(bc2.getMinValue()).equals(BigInteger.valueOf(0))) {
                LOGGER.info("Removed equality predicate of two instance variables.");
                return Optional.of(new EqualityGadget(bc1.getValue(), bc2.getMin()));
            }

            if (bc2.getMaxValue().subtract(bc1.getMinValue()).equals(BigInteger.valueOf(0))) {
                LOGGER.info("Removed equality predicate of two instance variables.");
                return Optional.of(new EqualityGadget(bc1.getValue(), bc1.getMin()));
            }
        }

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class}, context = {BoundsCheckGadget.class})
    public static Optional<Proposition> removeLooseBounds(BoundsCheckGadget target, BoundsCheckGadget context) {
        if (target.getValue().equals(context.getValue())
                && target.getMinValue().compareTo(context.getMinValue()) <= 0
                && target.getMaxValue().compareTo(context.getMaxValue()) >= 0) {
            LOGGER.info("Remove bounds predicate that is more loose than its context.");
            return Optional.of(Proposition.trueProposition());
        }

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class, BoundsCheckGadget.class})
    public static Optional<Proposition> mergeBounds(BoundsCheckGadget bc1, BoundsCheckGadget bc2) {
        if (bc1.getValue().equals(bc2.getValue())) {
            InstanceVariable upperBound = bc1.getMaxValue().compareTo(bc2.getMaxValue()) <= 0 ? bc1.getMax() : bc2.getMax();
            InstanceVariable lowerBound = bc1.getMinValue().compareTo(bc2.getMinValue()) >= 0 ? bc1.getMin() : bc2.getMin();

            LOGGER.info("Merge two bounds predicates of the same witness variable.");

            return Optional.of(new BoundsCheckGadget(bc1.getValue(), lowerBound, upperBound));
        }

        return Optional.empty();
    }

    public BigInteger getMinValue() {
        return (BigInteger) min.getValue().getValue();
    }

    public BigInteger getMaxValue() {
        return (BigInteger) max.getValue().getValue();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        if (getClass() != object.getClass())
            return false;

        BoundsCheckGadget other = (BoundsCheckGadget) object;
        return getValue().equals(other.getValue()) && getMin().equals(other.getMin()) && getMax().equals(other.getMax());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getMin(), getMax());
    }

    @Override
    public void initialize() {
        if (this.min == null)
            this.min = InstanceVariable.of(MIN_VALUE);

        if (this.max == null)
            this.max = InstanceVariable.of(MAX_VALUE);
    }

    @Override
    public int getCostEstimate() {
        return Constants.BOUNDS_CHECK_COST_ESTIMATE;
    }

    @Override
    public List<TargetFormat> toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("value", value),
                Map.entry("min", min),
                Map.entry("max", max)
        );
        return List.of(new TargetFormat("BOUND %(value) %(min) %(max)", args));
    }

    public WitnessVariable getValue() {
        return value;
    }

    public InstanceVariable getMin() {
        return min;
    }

    public InstanceVariable getMax() {
        return max;
    }
}
