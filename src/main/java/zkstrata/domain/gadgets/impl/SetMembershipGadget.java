package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.visitor.AstElement;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.SetMembership;
import zkstrata.utils.Constants;
import zkstrata.utils.GadgetUtils;

import java.util.*;
import java.util.stream.Collectors;

import static zkstrata.utils.GadgetUtils.isWitnessVariable;

@AstElement(SetMembership.class)
public class SetMembershipGadget extends AbstractGadget {
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

        this.initialize();
    }

    /**
     * Check whether there is an equality predicate claimed on a set of instance variables that contradict.
     *
     * @param sm {@link SetMembershipGadget} to check
     * @param eq {@link EqualityGadget} to check
     */
    @Contradiction(propositions = {SetMembershipGadget.class, EqualityGadget.class})
    public static void checkInstanceEqualityContradiction(SetMembershipGadget sm, EqualityGadget eq) {
        if (GadgetUtils.isWitnessVariable(sm.getMember())
                && sm.getSet().stream().allMatch((GadgetUtils::isInstanceVariable))) {
            Optional<Variable> equal = EqualityGadget.getEqual(eq, (WitnessVariable) sm.getMember());
            if (equal.isPresent()
                    && GadgetUtils.isInstanceVariable(equal.get())
                    && !sm.getSet().contains(equal.get())) {
                List<Variable> variables = new ArrayList<>(sm.getSet());
                variables.add(equal.get());
                throw new CompileTimeException("Contradiction.", variables);
            }
        }
    }

    @Substitution(target = {SetMembershipGadget.class})
    public static Optional<Proposition> removeSelfContained(SetMembershipGadget sm) {
        if (sm.getSet().contains(sm.getMember())) {
            LOGGER.info("Removed set membership predicate where the member is part of the set declaration (tautology).");
            return Optional.of(Proposition.trueProposition());
        }

        return Optional.empty();
    }

    @Substitution(target = {SetMembershipGadget.class}, context = {EqualityGadget.class})
    public static Optional<Proposition> removeEqualityContained(SetMembershipGadget sm, EqualityGadget eq) {
        if (isWitnessVariable(sm.getMember())) {
            Optional<Variable> equal = EqualityGadget.getEqual(eq, (WitnessVariable) sm.getMember());
            if (equal.isPresent() && sm.getSet().contains(equal.get())) {
                LOGGER.info("Removed set membership predicate where the member is part of the set declaration (tautology).");
                return Optional.of(Proposition.trueProposition());
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        if (getClass() != object.getClass())
            return false;

        SetMembershipGadget other = (SetMembershipGadget) object;
        return getMember().equals(other.getMember()) && getSet().equals(other.getSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMember(), getSet());
    }

    @Override
    public int getCostEstimate() {
        return Constants.MIMC_HASH_COST_ESTIMATE * set.size() + 5 * set.size() + 2;
    }

    @Override
    public List<TargetFormat> toTargetFormat() {
        Map<String, Variable> args = new HashMap<>();
        args.put("member", member);

        String setString = set.stream().map(variable -> {
            String key = String.format("var%s", args.size());
            args.put(key, variable);
            return String.format("%%(%s)", key);
        }).collect(Collectors.joining(" "));

        return List.of(new TargetFormat(String.format("SET_MEMBER %%(member) %s", setString), args));
    }

    public Variable getMember() {
        return member;
    }

    public Set<Variable> getSet() {
        return set;
    }
}
