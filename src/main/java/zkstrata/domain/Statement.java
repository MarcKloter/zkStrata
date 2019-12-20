package zkstrata.domain;

import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.data.schemas.wrapper.StructuredData;

import java.util.List;
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
     * Adds the given validation rules to the {@code claim} of this statement using an {@link AndConjunction}.
     *
     * @param validationRules {@link Statement} to add
     */
    public void setValidationRules(List<Statement> validationRules) {
        for (Statement validationRule : validationRules)
            this.claim = this.claim.combine(validationRule.getClaim());
    }
}
