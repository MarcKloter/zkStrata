package zkstrata.analysis;

import zkstrata.domain.gadgets.Gadget;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Inference {
    private Set<Gadget> premises;
    private Gadget conclusion;
    private Set<Inference> derivedFrom;

    public Inference(Set<Gadget> premises, Gadget conclusion, Set<Inference> derivedFrom) {
        this.premises = premises;
        this.conclusion = conclusion;
        this.derivedFrom = derivedFrom;
    }

    public static Inference from(Set<Inference> premises, Gadget conclusion) {
        return new Inference(premises.stream().map(Inference::getPremises)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()), conclusion, premises);
    }

    public Set<Gadget> getPremises() {
        return premises;
    }

    public Gadget getConclusion() {
        return conclusion;
    }

    public Set<Inference> getDerivedFrom() {
        return derivedFrom;
    }

    /**
     * Check whether the given {@link Inference} is an equal or simpler implication of the same conclusion.
     *
     * @param other {@link Inference} to check against this
     * @return {@code true} if the given {@code other} is a simpler implication, {@code false} otherwise
     */
    public boolean canBeImpliedFrom(Inference other) {
        if (!conclusion.equals(other.conclusion))
            return false;

        for (Gadget premise : other.getPremises()) {
            if (!premises.contains(premise))
                return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        if (conclusion == null || premises == null)
            return false;

        Inference other = (Inference) obj;

        return conclusion.equals(other.getConclusion()) && premises.equals(other.getPremises());
    }

    @Override
    public int hashCode() {
        return Objects.hash(premises, conclusion);
    }
}
