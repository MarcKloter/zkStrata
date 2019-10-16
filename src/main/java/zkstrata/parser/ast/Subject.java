package zkstrata.parser.ast;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

public class Subject {
    private Schema schema;
    private Alias alias;
    private boolean witness;

    public Subject(boolean witness) {
        this.witness = witness;
    }

    public void setAlias(String name, Position position) {
        this.alias = new Alias(name, position);
    }

    public void setSchema(String name, Position position) {
        this.schema = new Schema(name, position);
    }

    public Schema getSchema() {
        return schema;
    }

    public Alias getAlias() {
        return alias;
    }

    public boolean isWitness() {
        return witness;
    }

    public class Schema extends AbstractTraceable {
        private String name;

        private Schema(String name, Position position) {
            super(position);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public class Alias extends AbstractTraceable {
        private String name;

        private Alias(String name, Position position) {
            super(position);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
