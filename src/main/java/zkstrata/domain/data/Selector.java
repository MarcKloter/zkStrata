package zkstrata.domain.data;

import java.util.List;

public class Selector {
    private List<String> selectors;

    public Selector(List<String> selectors) {
        this.selectors = selectors;
    }

    public static Selector from(List<String> selectors) {
        return new Selector(selectors);
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

        return selectors.equals(((Selector) obj).getSelectors());
    }

    @Override
    public int hashCode() {
        return selectors.hashCode();
    }
}
