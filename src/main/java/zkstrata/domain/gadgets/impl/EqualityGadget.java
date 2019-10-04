package zkstrata.domain.gadgets.impl;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.gadgets.*;
import zkstrata.optimizer.GlobalOptimizationRule;
import zkstrata.optimizer.LocalOptimizationRule;
import zkstrata.parser.ast.predicates.Equality;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AstElement(Equality.class)
public class EqualityGadget extends AbstractGadget<EqualityGadget> {
    @Type({String.class, Integer.class})
    private Variable leftHand;

    @Type({String.class, Integer.class})
    private Variable rightHand;

    @Override
    public void onInit() {
    }

    @Override
    public boolean isEqualTo(EqualityGadget other) {
        return false;
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("leftHand", leftHand),
                Map.entry("rightHand", rightHand)
        );
        return new TargetFormat("EQUAL %(leftHand) %(rightHand)", args);
    }

    @LocalOptimizationRule
    public Optional<Gadget> checkInstanceEqualsInstance() {
        System.out.println("Called checkInstanceEqualsInstance");
        // TODO: how are we going to display warnings (return an object?)
        if (leftHand instanceof InstanceVariable && rightHand instanceof InstanceVariable
                && leftHand.getValue().equals(rightHand.getValue()))
            return Optional.empty();

        return Optional.of(this);
    }


    @LocalOptimizationRule(context = {EqualityGadget.class, EqualityGadget.class})
    public Optional<Gadget> testOptimizationMethod1(EqualityGadget eq1, EqualityGadget eq2) {
        System.out.println("Called testOptimizationMethod1");
        // TODO: (LocalOptimizationRule) W1 == W2 (remove), W1 == 5 (context), W2 == 5 (context)

        return Optional.of(this);
    }

    @LocalOptimizationRule(context = {EqualityGadget.class})
    public Optional<Gadget> testOptimizationMethod2(EqualityGadget equalityGadget) {
        System.out.println("Called testOptimizationMethod2");

        return Optional.of(this);
    }

    @LocalOptimizationRule(context = {BoundsCheckGadget.class, EqualityGadget.class})
    public Optional<Gadget> testOptimizationMethod3(BoundsCheckGadget boundsCheckGadget, EqualityGadget equalityGadget) {
        System.out.println("Called testOptimizationMethod3");

        return Optional.of(this);
    }

    @GlobalOptimizationRule(gadgets = {EqualityGadget.class, EqualityGadget.class, EqualityGadget.class})
    public List<Gadget> testOptimizationMethod4(EqualityGadget eq1, EqualityGadget eq2, EqualityGadget eq3) {
        System.out.println("Called testOptimizationMethod4");

        return List.of(eq1, eq2, eq3);
    }
}
