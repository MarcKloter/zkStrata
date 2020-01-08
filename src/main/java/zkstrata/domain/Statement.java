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
    private Proposition validationRule;

    public Statement(Proposition claim, Proposition premise, Proposition validationRule) {
        this.claim = claim;
        this.premise = premise;
        this.validationRule = validationRule;
    }

    public Statement(List<StructuredData> subjects, Proposition claim) {
        this.subjects = subjects;
        this.claim = claim;
        this.premise = null;
        this.validationRule = null;
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

    public Proposition getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(Proposition validationRule) {
        this.validationRule = this.validationRule.combine(validationRule);
    }

    public void addPremise(Proposition premise) {
        this.premise = this.premise.combine(premise);
    }
}
