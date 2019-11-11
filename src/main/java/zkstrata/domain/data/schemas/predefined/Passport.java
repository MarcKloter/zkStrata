package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;
import zkstrata.domain.data.schemas.metadata.MerkleTree;

import java.math.BigInteger;

@Schema(name = "passport_ch")
public class Passport extends AbstractSchema {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private Date expiresOn;

    private MerkleTree metaData;

    @Override
    public String getStatement() {
        return "PROOF FOR THIS THAT self.metaData.rootHash IS MERKLE ROOT OF " +
                "(((self.firstName, self.lastName), (self.dateOfBirth.day, self.dateOfBirth.month)), " +
                "((self.dateOfBirth.year, self.expiresOn.day), (self.expiresOn.month, self.expiresOn.year)))";
    }

    @Override
    public String getIdentifier() {
        return "passport_ch";
    }
}
