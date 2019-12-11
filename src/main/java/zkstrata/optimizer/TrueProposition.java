package zkstrata.optimizer;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.Proposition;
import zkstrata.domain.gadgets.Gadget;

import java.util.Collections;
import java.util.List;

public class TrueProposition implements Proposition {
    public static boolean isTrueProposition(Proposition proposition) {
        return proposition instanceof TrueProposition;
    }

    @Override
    public List<TargetFormat> toTargetFormat() {
        return List.of(new TargetFormat("", Collections.emptyMap()));
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
