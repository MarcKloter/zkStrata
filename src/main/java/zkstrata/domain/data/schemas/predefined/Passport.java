package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;

import java.math.BigInteger;
import java.util.Date;

@Schema(name = "passport_ch")
public class Passport extends AbstractSchema {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String placeOfBirth;
    private Date issuedOn;
    private Date expiresOn;

    private BigInteger count; // TODO: remove
    private DriversLicense owner; // TODO: remove
}
