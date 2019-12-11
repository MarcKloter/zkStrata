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

    boolean isEmpty();

    List<List<Proposition>> getCohesivePropositions();
}
