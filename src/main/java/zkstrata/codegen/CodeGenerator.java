package zkstrata.codegen;

import zkstrata.domain.Proposition;

public interface CodeGenerator<T extends TargetRepresentation> {
    T generateProverTargetRepresentation(Proposition proposition);

    T generateVerifierTargetRepresentation(Proposition proposition);
}
