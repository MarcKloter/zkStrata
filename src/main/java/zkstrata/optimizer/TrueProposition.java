package zkstrata.optimizer;

import zkstrata.codegen.representations.BulletproofsGadgetsCodeLine;
import zkstrata.domain.Proposition;
import zkstrata.domain.gadgets.Gadget;

import java.util.Collections;
import java.util.List;

public class TrueProposition implements Proposition {
    @Override
    public List<BulletproofsGadgetsCodeLine> toBulletproofsGadgets() {
        return Collections.emptyList();
    }

    @Override
    public List<List<Gadget>> getEvaluationPaths() {
        return Collections.emptyList();
    }

    @Override
    public int getCostEstimate() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrueProposition;
    }

    @Override
    public int hashCode() {
        return TrueProposition.class.hashCode();
    }

    @Override
    public String toString() {
        return "TRUE";
    }
}
