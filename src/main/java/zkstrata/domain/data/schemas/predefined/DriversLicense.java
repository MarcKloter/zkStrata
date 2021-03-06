package zkstrata.domain.data.schemas.predefined;

import zkstrata.domain.data.schemas.AbstractSchema;

@Schema(name = "drivers_license_ch")
public class DriversLicense extends AbstractSchema {
    private String firstName;
    private String lastName;
    private String category;
    private Date dateOfExpiry;

    @Override
    public String getIdentifier() {
        return "drivers_license_ch";
    }
}
