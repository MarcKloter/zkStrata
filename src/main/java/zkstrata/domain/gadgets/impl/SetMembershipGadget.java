package zkstrata.domain.gadgets.impl;

import zkstrata.analysis.Contradiction;
import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
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

import static java.lang.String.format;
import static zkstrata.domain.gadgets.impl.EqualityGadget.getEqualityToWitness;
import static zkstrata.utils.GadgetUtils.isWitnessVariable;

@AstElement(SetMembership.class)
public class SetMembershipGadget extends AbstractGadget {
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
    @Contradiction
    public static void checkInstanceEqualityContradiction(SetMembershipGadget sm, EqualityGadget eq) {
        if (GadgetUtils.isWitnessVariable(sm.getMember())
                && sm.getSet().stream().allMatch((GadgetUtils::isInstanceVariable))) {
            Optional<Variable> equal = getEqualityToWitness(eq, (WitnessVariable) sm.getMember());
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
        if (sm.getSet().contains(sm.getMember()))
            return Optional.of(Proposition.trueProposition());

        return Optional.empty();
    }

    @Substitution(target = {SetMembershipGadget.class}, context = {EqualityGadget.class})
    public static Optional<Proposition> removeEqualityContained(SetMembershipGadget sm, EqualityGadget eq) {
        if (isWitnessVariable(sm.getMember())) {
            Optional<Variable> equal = getEqualityToWitness(eq, (WitnessVariable) sm.getMember());

            if (equal.isPresent() && sm.getSet().contains(equal.get()))
                return Optional.of(Proposition.trueProposition());
        }

        return Optional.empty();
    }

    @Override
    public void initialize() {
        checkSetTypeHomogeneity();
    }

    private void checkSetTypeHomogeneity() {
        for (Variable element : this.set) {
            if (this.member.getType() != element.getType())
                throw new CompileTimeException(format("Type mismatch: %s cannot be equal to %s.",
                        member.getType().getSimpleName(), element.getType().getSimpleName()), List.of(member, element));
        }
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
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        LinkedHashMap<String, Variable> args = new LinkedHashMap<>();
        args.put("member", member);

        String setString = set.stream().map(variable -> {
            String key = String.format("var%s", args.size());
            args.put(key, variable);
            return String.format("%%(%s)", key);
        }).collect(Collectors.joining(" "));

        return List.of(new BulletproofsGadgetsCodeLine(String.format("SET_MEMBER %%(member) %s", setString), args));
    }

    public Variable getMember() {
        return member;
    }

    public Set<Variable> getSet() {
        return set;
    }
}
