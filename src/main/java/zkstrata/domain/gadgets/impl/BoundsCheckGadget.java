package zkstrata.domain.gadgets.impl;

import zkstrata.analysis.Contradiction;
import zkstrata.analysis.Implication;
import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Null;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.utils.Constants;
import zkstrata.utils.GadgetUtils;

import java.math.BigInteger;
import java.util.*;

import static zkstrata.domain.Proposition.trueProposition;
import static zkstrata.domain.gadgets.impl.EqualityGadget.getEqualityToWitness;
import static zkstrata.domain.gadgets.impl.InequalityGadget.getDisparityToWitness;
import static zkstrata.utils.GadgetUtils.*;

public class BoundsCheckGadget extends AbstractGadget {
    private static final BigInteger MIN_VALUE = BigInteger.ZERO;
    private static final BigInteger MAX_VALUE = Constants.UNSIGNED_64BIT_MAX;

    @Type({BigInteger.class})
    private Variable value;

    @Type({Null.class, BigInteger.class})
    private InstanceVariable min;

    @Type({Null.class, BigInteger.class})
    private InstanceVariable max;

    private Boolean strictComparison;

    public BoundsCheckGadget(Variable value, InstanceVariable min, InstanceVariable max, boolean strictComparison) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.strictComparison = strictComparison;

        this.initialize();
    }

    public BoundsCheckGadget(Variable value, InstanceVariable min, InstanceVariable max) {
        this(value, min, max, false);
    }

    @Implication
    public static Optional<Gadget> implyBounds(EqualityGadget eq, BoundsCheckGadget bc) {
        if (isWitnessVariable(bc.getValue())) {
            Optional<Variable> equal = getEqualityToWitness(eq, (WitnessVariable) bc.getValue());

            if (equal.isPresent() && isWitnessVariable(equal.get()))
                return Optional.of(new BoundsCheckGadget((WitnessVariable) equal.get(), bc.getMin(), bc.getMax()));
        }

        return Optional.empty();
    }

    @Implication
    public static Optional<Gadget> implyEquality(BoundsCheckGadget bc) {
        if (bc.getMaxValue().equals(bc.getMinValue())) {
            return Optional.of(new EqualityGadget(bc.getValue(), bc.getMin()));
        }

        return Optional.empty();
    }

    @Contradiction
    public static void checkTwoBoundsChecksContradiction(BoundsCheckGadget first, BoundsCheckGadget second) {
        if (haveSameValue(first, second)) {
            if (first.getMinValue().compareTo(second.getMaxValue()) > 0)
                throw new CompileTimeException("Contradiction.", List.of(first.getMin(), second.getMax()));

            if (first.getMaxValue().compareTo(second.getMinValue()) < 0)
                throw new CompileTimeException("Contradiction.", List.of(first.getMax(), second.getMin()));
        }
    }

    @Contradiction
    public static void checkSelfBoundsContradiction(BoundsCheckGadget bc) {
        if (bc.getMinValue().compareTo(bc.getMaxValue()) > 0)
            throw new CompileTimeException("Contradiction.", List.of(bc.getMin(), bc.getMax()));
    }

    @Contradiction
    public static void checkEqualityBoundsContradiction(EqualityGadget eq, BoundsCheckGadget bc) {
        if (isWitnessVariable(bc.getValue())) {
            Optional<Variable> equal = getEqualityToWitness(eq, (WitnessVariable) bc.getValue());

            if (equal.isPresent() && isInstanceVariable(equal.get())) {
                BigInteger value = (BigInteger) (((InstanceVariable) equal.get()).getValue()).getValue();
                if (value.compareTo(bc.getMinValue()) < 0)
                    throw new CompileTimeException("Contradiction.", List.of(eq.getRight(), bc.getMin()));
                if (value.compareTo(bc.getMaxValue()) > 0)
                    throw new CompileTimeException("Contradiction.", List.of(eq.getRight(), bc.getMax()));
            }
        }
    }

    @Contradiction
    public static void checkInstanceContradiction(BoundsCheckGadget bc) {
        if (isInstanceVariable(bc.getValue()) && !isContainedInBounds(bc.getValue(), bc))
            throw new CompileTimeException("Contradiction.", List.of(bc.getValue(), bc.getMin(), bc.getMax()));
    }

    @Substitution(target = {BoundsCheckGadget.class})
    public static Optional<Proposition> removeInstanceComparison(BoundsCheckGadget bc) {
        if (isInstanceVariable(bc.getValue()) && isContainedInBounds(bc.getValue(), bc))
            return Optional.of(Proposition.trueProposition());

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class}, context = {InequalityGadget.class})
    public static Optional<Proposition> replaceUnequalBounds(BoundsCheckGadget bc, InequalityGadget iq) {
        if (isWitnessVariable(bc.getValue())) {
            Optional<Variable> disparity = getDisparityToWitness(iq, (WitnessVariable) bc.getValue());

            if (disparity.isPresent() && isVariableEqualToBigInteger(disparity.get(), bc.getMaxValue()))
                return Optional.of(new BoundsCheckGadget(bc.getValue(), bc.getMin(), subtractOne(bc.getMax())));

            if (disparity.isPresent() && isVariableEqualToBigInteger(disparity.get(), bc.getMinValue()))
                return Optional.of(new BoundsCheckGadget(bc.getValue(), addOne(bc.getMin()), bc.getMax()));
        }

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class}, context = {EqualityGadget.class})
    public static Optional<Proposition> removeValueEquality(BoundsCheckGadget bc, EqualityGadget eq) {
        if (isWitnessVariable(bc.getValue())) {
            Optional<Variable> equal = getEqualityToWitness(eq, (WitnessVariable) bc.getValue());

            if (equal.isPresent() && isContainedInBounds(equal.get(), bc))
                return Optional.of(trueProposition());
        }

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class})
    public static Optional<Proposition> replaceMinEqualsMax(BoundsCheckGadget bc) {
        if (bc.getMaxValue().equals(bc.getMinValue()))
            return Optional.of(new EqualityGadget(bc.getValue(), bc.getMin()));

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class}, context = {BoundsCheckGadget.class})
    public static Optional<Proposition> replaceMinEqualsMaxTwoGadgets(BoundsCheckGadget first, BoundsCheckGadget second) {
        if (haveSameValue(first, second)) {
            if (first.getMaxValue().equals(second.getMinValue()))
                return Optional.of(new EqualityGadget(first.getValue(), second.getMin()));

            if (second.getMaxValue().equals(first.getMinValue()))
                return Optional.of(new EqualityGadget(first.getValue(), first.getMin()));
        }

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class}, context = {BoundsCheckGadget.class})
    public static Optional<Proposition> removeLooseBounds(BoundsCheckGadget target, BoundsCheckGadget context) {
        if (haveSameValue(target, context)
                && target.getMinValue().compareTo(context.getMinValue()) <= 0
                && target.getMaxValue().compareTo(context.getMaxValue()) >= 0)
            return Optional.of(trueProposition());

        return Optional.empty();
    }

    @Substitution(target = {BoundsCheckGadget.class, BoundsCheckGadget.class})
    public static Optional<Proposition> mergeBounds(BoundsCheckGadget first, BoundsCheckGadget second) {
        if (haveSameValue(first, second)) {
            InstanceVariable upperBound = getUpperBound(first, second);
            InstanceVariable lowerBound = getLowerBound(first, second);

            return Optional.of(new BoundsCheckGadget(first.getValue(), lowerBound, upperBound));
        }

        return Optional.empty();
    }

    public static boolean isContainedInBounds(Variable variable, BoundsCheckGadget bc) {
        if (isInstanceVariable(variable) && isOfTypeBigInteger(variable)) {
            BigInteger value = (BigInteger) ((InstanceVariable) variable).getValue().getValue();

            return bc.getMinValue().compareTo(value) <= 0 && bc.getMaxValue().compareTo(value) >= 0;
        }

        return false;
    }

    private static boolean isVariableEqualToBigInteger(Variable variable, BigInteger value) {
        return isInstanceVariable(variable) && value.equals((((InstanceVariable) variable).getValue()).getValue());
    }

    private static boolean haveSameValue(BoundsCheckGadget first, BoundsCheckGadget second) {
        return first.getValue().equals(second.getValue());
    }

    private static InstanceVariable getUpperBound(BoundsCheckGadget first, BoundsCheckGadget second) {
        return first.getMaxValue().compareTo(second.getMaxValue()) <= 0 ? first.getMax() : second.getMax();
    }

    private static InstanceVariable getLowerBound(BoundsCheckGadget first, BoundsCheckGadget second) {
        return first.getMinValue().compareTo(second.getMinValue()) >= 0 ? first.getMin() : second.getMin();
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
        if (Boolean.TRUE.equals(this.strictComparison)) {
            this.min = GadgetUtils.addOne(this.min);
            this.max = GadgetUtils.subtractOne(this.max);
        }

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
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("value", value),
                Map.entry("min", min),
                Map.entry("max", max)
        );
        return List.of(new BulletproofsGadgetsCodeLine("BOUND %(value) %(min) %(max)", args));
    }

    public Variable getValue() {
        return value;
    }

    public InstanceVariable getMin() {
        return min;
    }

    public InstanceVariable getMax() {
        return max;
    }
}
