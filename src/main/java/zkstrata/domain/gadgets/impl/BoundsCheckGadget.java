package zkstrata.domain.gadgets.impl;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.parser.ast.predicates.BoundsCheck;

import java.util.Map;

@AstElement(BoundsCheck.class)
public class BoundsCheckGadget extends AbstractGadget<BoundsCheckGadget> {
    @Type({Integer.class})
    private WitnessVariable value;

    @Type({Integer.class})
    private InstanceVariable min;

    @Type({Integer.class})
    private InstanceVariable max;

    @Override
    public boolean isEqualTo(BoundsCheckGadget other) {
        return false;
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
