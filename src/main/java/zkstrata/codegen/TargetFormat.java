package zkstrata.codegen;

import zkstrata.domain.data.types.wrapper.Variable;

import java.util.Map;

public class TargetFormat {
    private String format;
    private Map<String, Variable> args;

    public TargetFormat(String format, Map<String, Variable> args) {
        this.format = format;
        this.args = args;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, Variable> getArgs() {
        return args;
    }
}
