package zkstrata.domain.conjunctions;

import zkstrata.domain.Proposition;

import java.util.List;

public interface Conjunction extends Proposition {
    List<Proposition> getParts();
}
