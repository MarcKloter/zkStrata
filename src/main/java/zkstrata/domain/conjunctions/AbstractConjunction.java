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
}
