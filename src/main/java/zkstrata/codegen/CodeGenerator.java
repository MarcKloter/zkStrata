package zkstrata.codegen;

public interface CodeGenerator<S extends TargetFormat, T extends TargetStructure> {
    T generateProverTargetStructure(S source);

    T generateVerifierTargetStructure(S source);
}
