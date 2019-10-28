package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;

import java.util.List;

public class Identifier extends Value<String> {
    private String subject;
    private List<String> selectors;

    public Identifier(String subject, List<String> selectors, Position position) {
        super(position);
        this.subject = subject;
        this.selectors = selectors;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getSelectors() {
        return selectors;
    }

    @Override
    public String getValue() {
        return String.format("%s.%s", subject, String.join(".", selectors));
    }
}
