package zkstrata.compiler;

import zkstrata.codegen.CodeGenerator;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;

import java.util.List;
import java.util.Map;

public class Arguments {
    private CodeGenerator codeGenerator;
    private Statement statement;
    private List<Statement> premises;
    private SubjectData subjectData;

    public Arguments(
            CodeGenerator codeGenerator,
            Statement statement,
            List<Statement> premises,
            SubjectData subjectData
    ) {
        this.codeGenerator = codeGenerator;
        this.statement = statement;
        this.premises = premises;
        this.subjectData = subjectData;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public CodeGenerator getCodeGenerator() {
        return codeGenerator;
    }

    public List<Statement> getPremises() {
        return premises;
    }

    public SubjectData getSubjectData() {
        return subjectData;
    }

    public boolean hasWitnessData() {
        return !getSubjectData().getWitnessData().isEmpty();
    }

    public static class Statement {
        private String source;
        private String value;

        public Statement(String source, String value) {
            this.source = source;
            this.value = value;
        }

        public String getSource() {
            return source;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SubjectData {
        private Map<String, ValueAccessor> witnessData;
        private Map<String, ValueAccessor> instanceData;
        private Map<String, Schema> schemas;

        public SubjectData(
                Map<String, ValueAccessor> witnessData,
                Map<String, ValueAccessor> instanceData,
                Map<String, Schema> schemas
        ) {
            this.witnessData = witnessData;
            this.instanceData = instanceData;
            this.schemas = schemas;
        }

        public Map<String, ValueAccessor> getWitnessData() {
            return witnessData;
        }

        public Map<String, ValueAccessor> getInstanceData() {
            return instanceData;
        }

        public Map<String, Schema> getSchemas() {
            return schemas;
        }
    }
}
