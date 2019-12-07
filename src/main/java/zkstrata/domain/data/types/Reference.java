package zkstrata.domain.data.types;

import zkstrata.domain.data.Selector;

import java.util.Objects;

public class Reference implements Value {
    private Class<?> type;
    private String subject;
    private Selector selector;

    public Reference(Class<?> type, String subject, Selector selector) {
        this.type = type;
        this.subject = subject;
        this.selector = selector;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s.%s", subject, selector.toString());
    }

    @Override
    public String toHex() {
        throw new IllegalStateException("Invalid method call toHex on Reference.");
    }

    public String getSubject() {
        return subject;
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        Reference ref = (Reference) obj;
        return getType().equals(ref.getType())
                && getSubject().equals(ref.getSubject())
                && getSelector().equals(ref.getSelector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subject, selector);
    }
}
