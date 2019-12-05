package zkstrata.domain;

import zkstrata.domain.conjunctions.AndConjunction;
import zkstrata.domain.data.schemas.wrapper.StructuredData;

import java.util.Map;
import java.util.Set;

/**
 * Compiler internal representation of a zkStrata statement.
 */
public class Statement {
    private Map<String, StructuredData> subjects;
    private Constituent rootConstituent;
    private Set<Statement> premises;

    public Statement(Map<String, StructuredData> subjects, Constituent rootConstituent) {
        this.subjects = subjects;
        this.rootConstituent = rootConstituent;
    }

    public Map<String, StructuredData> getSubjects() {
        return subjects;
    }

    public Constituent getRootConstituent() {
        return rootConstituent;
    }

    public Set<Statement> getPremises() {
        return premises;
    }

    public void setPremises(Set<Statement> premises) {
        this.premises = premises;
    }

    /**
     * Adds the given constituent to the {@code rootConstituent} of this grammar using an {@link AndConjunction}.
     *
     * @param constituent {@link Constituent} to add
     */
    public void addConstituent(Constituent constituent) {
        this.rootConstituent = this.rootConstituent.combine(constituent);
    }
}
