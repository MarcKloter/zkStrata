package zkstrata.domain;

import zkstrata.domain.data.schemas.wrapper.StructuredData;

import java.util.List;

/**
 * Compiler internal representation of a zkStrata statement.
 */
public class Statement {
    private List<StructuredData> subjects;
    private Proposition claim;
    private Proposition premise;
    private Proposition validationRules;

    public Statement(Proposition claim, Proposition premise, Proposition validationRules) {
        this.claim = claim;
        this.premise = premise;
        this.validationRules = validationRules;
    }

    public Statement(List<StructuredData> subjects, Proposition claim) {
        this.subjects = subjects;
        this.claim = claim;
        this.premise = null;
        this.validationRules = null;
    }

    public List<StructuredData> getSubjects() {
        return subjects;
    }

    public Proposition getClaim() {
        return claim;
    }

    public void setClaim(Proposition claim) {
        this.claim = claim;
    }

    public Proposition getPremise() {
        return premise;
    }

    public Proposition getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(Proposition validationRules) {
        this.validationRules = this.validationRules == null ? validationRules : this.validationRules.combine(validationRules);
    }

    public void addPremise(Proposition premise) {
        this.premise = this.premise == null ? premise : this.premise.combine(premise);
    }
}
