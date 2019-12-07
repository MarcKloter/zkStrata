package zkstrata.domain;

import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.data.schemas.wrapper.StructuredData;

import java.util.Map;

/**
 * Compiler internal representation of a zkStrata statement.
 */
public class Statement {
    private Map<String, StructuredData> subjects;
    private Proposition claim;
    private Proposition premises;

    public Statement(Map<String, StructuredData> subjects, Proposition claim) {
        this.subjects = subjects;
        this.claim = claim;
    }

    public Map<String, StructuredData> getSubjects() {
        return subjects;
    }

    public Proposition getClaim() {
        return claim;
    }

    public Proposition getPremises() {
        return premises;
    }

    public void setPremises(Proposition premises) {
        this.premises = premises;
    }

    /**
     * Adds the given proposition to the {@code claim} of this statement using an {@link AndConjunction}.
     *
     * @param proposition {@link Proposition} to add
     */
    public void addProposition(Proposition proposition) {
        this.claim = this.claim.combine(proposition);
    }
}
