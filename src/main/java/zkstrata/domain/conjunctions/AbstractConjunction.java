package zkstrata.domain.conjunctions;

import zkstrata.domain.Proposition;
import zkstrata.optimizer.Substitution;

import java.util.List;
import java.util.Optional;

public abstract class AbstractConjunction implements Conjunction {
    private List<Proposition> parts;

    public AbstractConjunction(List<Proposition> parts) {
        this.parts = parts;
    }

    @Override
    public List<Proposition> getParts() {
        return parts;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().replace("Conjunction", "").toUpperCase();
    }

    @Override
    public boolean isEmpty() {
        return getParts().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return getParts().equals(((Conjunction) obj).getParts());
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }



    @Substitution(target = {Conjunction.class, Conjunction.class})
    public static Optional<Proposition> removeDuplicateConjunction(Conjunction first, Conjunction second) {
        return first.getParts().containsAll(second.getParts()) ? Optional.of(first) : Optional.empty();
    }
}
