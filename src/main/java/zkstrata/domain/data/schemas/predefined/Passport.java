package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;

import java.math.BigInteger;

@Schema(name = "passport_ch")
public class Passport extends AbstractSchema {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String placeOfBirth;
    private Date issuedOn;
    private Date expiresOn;

    private BigInteger count; // TODO: remove
    private BigInteger count2; // TODO: remove
    private BigInteger count3; // TODO: remove
    private BigInteger count4; // TODO: remove
    private BigInteger count5; // TODO: remove
    private DriversLicense owner; // TODO: remove

    private String hash;

    @Override
    public String getStatement() {
        return "PROOF FOR THIS THAT self.firstName IS EQUAL TO 'John'";
        /*return "PROOF FOR THIS THAT this.hash IS MERKLE ROOT OF (((firstName, lastName), (dateOfBirth, placeOfBirth)), " +
                "((issuedOn, expiresOn), (count, owner)))";*/
    }

    @Override
    public String getIdentifier() {
        return "passport_ch";
    }
}
