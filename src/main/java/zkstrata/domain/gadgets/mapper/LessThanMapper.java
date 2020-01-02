package zkstrata.domain.gadgets.mapper;

import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.domain.gadgets.impl.BoundsCheckGadget;
import zkstrata.domain.gadgets.impl.LessThanGadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.parser.ast.predicates.LessThan;

import java.math.BigInteger;
import java.util.Map;

import static zkstrata.utils.GadgetUtils.isWitnessVariable;

@AstElement(LessThan.class)
public class LessThanMapper extends AbstractMapper {
    @Type({BigInteger.class})
    private Variable left;

    @Type({BigInteger.class})
    private Variable right;

    private Boolean strict;

    @Override
    public Gadget initFrom(Map<String, Object> sourceFields) {
        super.initFrom(sourceFields);

        if (isWitnessVariable(left) && isWitnessVariable(right))
            return new LessThanGadget((WitnessVariable) left, (WitnessVariable) right);
        else if(isWitnessVariable(right))
            return new BoundsCheckGadget(right, (InstanceVariable) left, null, strict);
        else
            return new BoundsCheckGadget(left, null, (InstanceVariable) right, strict);
    }
}
