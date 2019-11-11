package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;

import java.util.Date;

@Schema(name = "drivers_license_ch")
public class DriversLicense extends AbstractSchema {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String category;

    @Override
    public String getIdentifier() {
        return "drivers_license_ch";
    }
}
