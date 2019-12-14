package zkstrata.domain.conjunctions;

import zkstrata.domain.Proposition;
import zkstrata.exceptions.InternalCompilerException;

import java.util.List;

public interface Conjunction extends Proposition {
    /**
     * Creates a new instance of conjunction {@code clazz} connecting the provided {@code parts}.
     *
     * @param clazz class extending {@link Conjunction} to instantiate
     * @param parts parts to connect through the new conjunction
     * @return {@link Conjunction} of class {@code clazz} containing the given {@code parts}
     */
    static Conjunction createInstanceOf(Class<? extends Conjunction> clazz, List<Proposition> parts) {
        try {
            return clazz.getConstructor(List.class).newInstance(parts);
        } catch (ReflectiveOperationException e) {
            throw new InternalCompilerException("Error during invocation of List.class parameterized constructor of %s.",
                    clazz);
        }
    }

    /**
     * Returns all parts that are connected through this conjunction.
     *
     * @return list of {@link Proposition}
     */
    List<Proposition> getParts();

    /**
     * Returns a disjunct list of combinations of {@link Proposition} formed from the parts of this conjunction, where
     * each combination (list of {@link Proposition}) is cohesive (this part of the conjunction can only be proven by
     * showing, that all of the {@link Proposition} evaluate to true).
     *
     * @return list of proposition combinations (list of {@link Proposition})
     */
    List<List<Proposition>> getCohesivePropositions();
}
