package zkstrata.exceptions;

import java.io.Serializable;

public abstract class Position implements Serializable {
    private String target;
    private int line;
    private int charPositionInLine;

    public Position(String target, int line, int charPositionInLine) {
        this.target = target;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public String getTarget() {
        return target;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return charPositionInLine;
    }

    public static class Relative extends Position {
        public Relative(String target, int line, int charPositionInLine) {
            super(target, line, charPositionInLine);
        }
    }

    public static class Absolute extends Position {
        private String source;
        private String statement;

        public Absolute(String source, String statement, Position position) {
            super(position.getTarget(), position.getLine(), position.getPosition());
            this.source = source;
            this.statement = statement;
        }

        public Absolute(String source, String statement, String target, int line, int charPositionInLine) {
            super(target, line, charPositionInLine);
            this.source = source;
            this.statement = statement;
        }

        public String getSource() {
            return source;
        }

        public String getStatement() {
            return statement;
        }
    }
}
