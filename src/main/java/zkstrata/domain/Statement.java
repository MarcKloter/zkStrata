package zkstrata.domain;

import zkstrata.analysis.Inference;
import zkstrata.domain.data.schemas.wrapper.StructuredData;
import zkstrata.domain.gadgets.Gadget;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiler internal representation of a zkStrata statement.
 */
public class Statement {
    private Map<String, StructuredData> subjects;
    private List<Gadget> gadgets;
    private Set<Statement> premises;
    private Set<Inference> inferences;

    public Statement(Map<String, StructuredData> subjects, List<Gadget> gadgets) {
        this.subjects = subjects;
        this.gadgets = gadgets;
    }

    public Map<String, StructuredData> getSubjects() {
        return subjects;
    }

    public List<Gadget> getGadgets() {
        return gadgets;
    }

    public Set<Inference> getInferences() {
        return inferences;
    }

    public Set<Statement> getPremises() {
        return premises;
    }

    public void setPremises(Set<Statement> premises) {
        this.premises = premises;
    }

    public void setInferences(Set<Inference> inferences) {
        this.inferences = inferences;
    }

    public void addGadgets(List<Gadget> gadgets) {
        this.gadgets.addAll(gadgets);
    }
}
