package zkstrata.domain.data;

import java.util.List;

public class Selector {
    private List<String> selectors;

    public Selector(List<String> selectors) {
        this.selectors = selectors;
    }

    public Selector(String selector) {
        this.selectors = List.of(selector);
    }

    public List<String> getSelectors() {
        return this.selectors;
    }

    @Override
    public String toString() {
        return String.join(".", selectors);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return getSelectors().equals(((Selector) obj).getSelectors());
    }

    @Override
    public int hashCode() {
        return selectors.hashCode();
    }
}
