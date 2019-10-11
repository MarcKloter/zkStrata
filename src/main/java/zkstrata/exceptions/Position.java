package zkstrata.exceptions;

import java.io.Serializable;

public class Position implements Serializable {
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
}
