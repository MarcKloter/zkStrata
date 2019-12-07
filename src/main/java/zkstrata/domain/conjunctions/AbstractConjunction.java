package zkstrata.domain.conjunctions;

import zkstrata.domain.Proposition;

import java.util.List;

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
}
