package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.SetMembership;

import java.util.*;
import java.util.stream.Collectors;

import static zkstrata.utils.GadgetUtils.isWitnessVariable;

@AstElement(SetMembership.class)
public class SetMembershipGadget extends AbstractGadget<SetMembershipGadget> {
    private static final Logger LOGGER = LogManager.getRootLogger();

    @Type({Any.class})
    private Variable member;

    @Type({Any.class})
    private Set<Variable> set;

    public SetMembershipGadget() {

    }

    public SetMembershipGadget(Variable member, Set<Variable> set) {
        this.member = member;
        this.set = set;

        this.performChecks();
    }

    @Substitution(target = {SetMembershipGadget.class})
    public static Set<Gadget> removeSelfContained(SetMembershipGadget sm) {
        if (sm.getSet().contains(sm.getMember())) {
            // TODO: maybe add statements information
            LOGGER.info("Removed set membership predicate where the member is part of the set declaration (tautology).");
            return Collections.emptySet();
        }

        return Set.of(sm);
    }

    @Substitution(target = {SetMembershipGadget.class}, context = {EqualityGadget.class})
    public static Set<Gadget> removeEqualityContained(SetMembershipGadget sm, EqualityGadget eq) {
        if (isWitnessVariable(sm.getMember())) {
            Optional<Variable> equal = EqualityGadget.getEqual(eq, (WitnessVariable) sm.getMember());
            if (equal.isPresent() && sm.getSet().contains(equal.get())) {
                // TODO: maybe add statements information
                LOGGER.info("Removed set membership predicate where the member is part of the set declaration (tautology).");
                return Collections.emptySet();
            }
        }

        return Set.of(sm);
    }

    @Override
    public boolean isEqualTo(SetMembershipGadget other) {
        return member.equals(other.member) && set.equals(other.set);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = new HashMap<>();
        args.put("member", member);

        String setString = set.stream().map(variable -> {
            String key = String.format("var%s", args.size());
            args.put(key, variable);
            return String.format("%%(%s)", key);
        }).collect(Collectors.joining(" "));

        return new TargetFormat(String.format("SET_MEMBER %%(member) %s", setString), args);
    }

    public Variable getMember() {
        return member;
    }

    public Set<Variable> getSet() {
        return set;
    }
}
