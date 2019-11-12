package zkstrata.domain.data.schemas;

import zkstrata.domain.data.Selector;

public interface Schema {
    Class<?> getType(Selector selector);
    String getIdentifier();
    String getSource();
    String getValidationRule();
}
