package zkstrata.domain.gadgets.impl;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Nullable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.utils.SemanticsUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@AstElement(BoundsCheck.class)
public class BoundsCheckGadget extends AbstractGadget<BoundsCheckGadget> {
    private final static BigInteger MIN = BigInteger.valueOf(0);
    private final static BigInteger MAX = new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    @Type({BigInteger.class})
    private WitnessVariable value;

    @Type({Nullable.class, BigInteger.class})
    private InstanceVariable min;

    @Type({Nullable.class, BigInteger.class})
    private InstanceVariable max;

    @Override
    public boolean isEqualTo(BoundsCheckGadget other) {
        return false;
    }

    @Override
    public void performChecks() throws CompileTimeException {
        if (this.min == null)
            this.min = new InstanceVariable(new Literal(MIN), null);

        if (this.max == null)
            this.max = new InstanceVariable(new Literal(MAX), null);

        BigInteger min = (BigInteger) this.min.getValue().getValue();
        BigInteger max = (BigInteger) this.max.getValue().getValue();

        if (SemanticsUtils.testMaxBitSize(min, 256))
            throw new CompileTimeException("The lower bound cannot be longer than 32 bytes.", this.min.getPosition());

        if (SemanticsUtils.testMaxBitSize(max, 256))
            throw new CompileTimeException("The upper bound cannot be longer than 32 bytes.", this.max.getPosition());

        if (max.compareTo(MIN) < 1)
            throw new CompileTimeException(String.format("The upper bound must be greater than %s.", MIN), this.max.getPosition());

        if (min.compareTo(MAX) > -1)
            throw new CompileTimeException(String.format("The lower bound must be less than %s.", MAX), this.min.getPosition());

        if (min.compareTo(max) == 0)
            throw new CompileTimeException("The lower and upper bound cannot be equal.", List.of(this.min.getPosition(), this.max.getPosition()));

        if (min.compareTo(max) > 0)
            throw new CompileTimeException("The lower bound must be less than the upper bound.", List.of(this.min.getPosition(), this.max.getPosition()));
    }
    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("value", value),
                Map.entry("min", min),
                Map.entry("max", max)
        );
        return new TargetFormat("BOUNDS %(value) %(min) %(max)", args);
    }

    // TODO: semantic check that min < max

}
