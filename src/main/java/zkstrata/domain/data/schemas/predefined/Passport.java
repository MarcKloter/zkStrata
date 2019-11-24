package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;
import zkstrata.domain.data.types.custom.HexLiteral;

@Schema(name = "passport_ch")
public class Passport extends AbstractSchema {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private Date expiresOn;
    private HexLiteral rootHash;

    @Override
    public boolean hasValidationRule() {
        return true;
    }

    @Override
    public String getValidationRule() {
        return "PROOF FOR THIS THAT public.rootHash IS MERKLE ROOT OF " +
                "(((private.firstName, private.lastName), (private.dateOfBirth.day, private.dateOfBirth.month)), " +
                "((private.dateOfBirth.year, private.expiresOn.day), (private.expiresOn.month, private.expiresOn.year)))";
    }

    @Override
    public String getIdentifier() {
        return "passport_ch";
    }
}
