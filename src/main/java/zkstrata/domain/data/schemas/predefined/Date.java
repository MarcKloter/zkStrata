package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;

import java.math.BigInteger;

@Schema(name = "date")
public class Date extends AbstractSchema {
    private BigInteger day;
    private BigInteger month;
    private BigInteger year;

    @Override
    public String getIdentifier() {
        return "date";
    }
}
