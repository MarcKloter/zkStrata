package zkstrata.codegen.representations;

import zkstrata.domain.data.types.wrapper.Variable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BulletproofsGadgetsCodeLine {
    private String format;
    private LinkedHashMap<String, Variable> variables;

    public BulletproofsGadgetsCodeLine(String format, LinkedHashMap<String, Variable> variables) {
        this.format = format;
        this.variables = variables;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, Variable> getVariables() {
        return variables;
    }
}
