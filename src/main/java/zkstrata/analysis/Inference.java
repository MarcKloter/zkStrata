package zkstrata.analysis;

import zkstrata.domain.gadgets.Gadget;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Inference {
    private Set<Gadget> assumptions;
    private Gadget conclusion;
    private Set<Inference> derivedFrom;

    public Inference(Set<Gadget> assumptions, Gadget conclusion, Set<Inference> derivedFrom) {
        this.assumptions = assumptions;
        this.conclusion = conclusion;
        this.derivedFrom = derivedFrom;
    }

    public static Inference from(Set<Inference> premises, Gadget conclusion) {
        return new Inference(premises.stream().map(Inference::getAssumptions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()), conclusion, premises);
    }

    public Set<Gadget> getAssumptions() {
        return assumptions;
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

        for (Gadget premise : other.getAssumptions()) {
            if (!assumptions.contains(premise))
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

        if (conclusion == null || assumptions == null)
            return false;

        Inference other = (Inference) obj;

        return conclusion.equals(other.getConclusion()) && assumptions.equals(other.getAssumptions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(assumptions, conclusion);
    }
}
