package zkstrata.domain.gadgets.mapper;

import zkstrata.codegen.TargetFormat;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.exceptions.InternalCompilerException;

import java.util.List;

public abstract class AbstractMapper extends AbstractGadget {
    private static final String ERROR = "Illegal call to method %s of mapper %s.";

    @Override
    public boolean equals(Object obj) {
        throw new InternalCompilerException(ERROR, "equals", getClass());
    }

    @Override
    public int hashCode() {
        throw new InternalCompilerException(ERROR, "hashCode", getClass());
    }

    @Override
    public List<TargetFormat> toTargetFormat() {
        throw new InternalCompilerException(ERROR, "toTargetFormat", getClass());
    }

    @Override
    public int getCostEstimate() {
        throw new InternalCompilerException(ERROR, "getCostEstimate", getClass());
    }
}
