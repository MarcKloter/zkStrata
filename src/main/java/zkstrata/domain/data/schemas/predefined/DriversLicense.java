package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;

import java.math.BigInteger;

@Schema(name = "drivers_license_ch")
public class DriversLicense extends AbstractSchema {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String placeOfOrigin;
    private Date dateOfIssue;
    private Date dateOfExpiry;
    private String issuer;
    private BigInteger number;
    private String categoryA;
    private String categoryB;
    private String categoryC;
    private String categoryD;
    private String categoryBE;
    private String categoryCE;
    private String categoryDE;

    @Override
    public String getIdentifier() {
        return "drivers_license_ch";
    }
}
