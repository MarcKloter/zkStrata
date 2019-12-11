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

    /**
     * Checks whether the {@code first} and {@code second} are of the same type and connect the same parts.
     * If this is the case, remove the {@code second}.
     *
     * @param first  {@link Conjunction} to check
     * @param second {@link Conjunction} to check
     * @return {@link Optional} of {@code first} if the check succeeds, an empty {@link Optional} otherwise
     */
    @Substitution(target = {Conjunction.class, Conjunction.class})
    public static Optional<Proposition> removeDuplicateConjunction(Conjunction first, Conjunction second) {
        return first.equals(second) ? Optional.of(first) : Optional.empty();
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

        Conjunction other = (Conjunction) obj;
        return getParts().containsAll(other.getParts()) && other.getParts().containsAll(getParts());
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }
}
