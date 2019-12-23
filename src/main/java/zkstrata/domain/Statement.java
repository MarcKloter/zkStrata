package zkstrata.domain;

import zkstrata.domain.data.schemas.wrapper.StructuredData;

import java.util.Map;

/**
 * Compiler internal representation of a zkStrata statement.
 */
public class Statement {
    private Map<String, StructuredData> subjects;
    private Proposition claim;
    private Proposition premises;
    private Proposition validationRules;

    public Statement(Proposition claim, Proposition premises, Proposition validationRules) {
        this.claim = claim;
        this.premises = premises;
        this.validationRules = validationRules;
    }

    public Statement(Map<String, StructuredData> subjects, Proposition claim) {
        this.subjects = subjects;
        this.claim = claim;
        this.premises = null;
        this.validationRules = null;
    }

    public Map<String, StructuredData> getSubjects() {
        return subjects;
    }

    public Proposition getClaim() {
        return claim;
    }

    public void setClaim(Proposition claim) {
        this.claim = claim;
    }

    public Proposition getPremises() {
        return premises;
    }

    public Proposition getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(Proposition validationRules) {
        this.validationRules = this.validationRules == null ? validationRules : this.validationRules.combine(validationRules);
    }

    public void addPremises(Proposition premises) {
        this.premises = this.premises == null ? premises : this.premises.combine(premises);
    }
}
