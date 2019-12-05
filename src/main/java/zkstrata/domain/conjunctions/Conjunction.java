package zkstrata.domain.conjunctions;

import zkstrata.domain.Constituent;

import java.util.List;

public interface Conjunction extends Constituent {
    List<Constituent> getParts();
}
