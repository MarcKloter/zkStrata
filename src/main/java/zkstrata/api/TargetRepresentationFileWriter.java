package zkstrata.api;

import zkstrata.codegen.TargetRepresentation;

public interface TargetRepresentationFileWriter<T extends TargetRepresentation> {
    void write(T targetRepresentation);
}
