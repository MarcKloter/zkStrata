package zkstrata.api;

import zkstrata.codegen.TargetStructure;

public interface TargetRepresentationFileWriter<T extends TargetStructure> {
    void write(T targetRepresentation);
}
