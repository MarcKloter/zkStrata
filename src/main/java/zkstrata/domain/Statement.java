package zkstrata.domain;

import zkstrata.analysis.Inference;
import zkstrata.domain.data.schemas.wrapper.StructuredData;
import zkstrata.domain.gadgets.Gadget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiler internal representation of a zkStrata statement.
 */
public class Statement {
    private Map<String, StructuredData> subjects;
    private List<Gadget> gadgets;
    private Set<Inference> inferences;

    public Statement(Map<String, StructuredData> subjects, List<Gadget> gadgets) {
        this.subjects = subjects;
        this.gadgets = gadgets;
    }

    // TODO: addGadget(Gadget gadget)
    // TODO removeGadget(Gadget gadget)

    public Map<String, StructuredData> getSubjects() {
        return subjects;
    }

    public List<Gadget> getGadgets() {
        return gadgets;
    }

    public Set<Inference> getInferences() {
        return inferences;
    }

    public void setInferences(Set<Inference> inferences) {
        this.inferences = inferences;
    }

    public Map<String, String> getSupplementaryStatements() {
        Map<String, String> supplementaryStatements = new HashMap<>();
        for (Map.Entry<String, StructuredData> subject : subjects.entrySet()) {
            supplementaryStatements.put(subject.getKey(), subject.getValue().getSchema().getStatement());
        }
        return supplementaryStatements;
    }

}
