package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaTest {
    private static final String DATA_PATH = "src/test/resources/miscellaneous/";

    private JsonSchema schema;

    @BeforeEach
    void setup() {
        this.schema = new JsonSchema(DATA_PATH + "schema_test.schema.json", "test_schema");
    }

    @Test
    void Get_Type_Undefined_Property() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema.getType(new Selector(List.of("missing")));
        });
    }

    @Test
    void Get_Type_Invalid_Type_Definition() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema.getType(new Selector(List.of("invalid")));
        });
    }

    @Test
    void Get_Type_Unknown_Type_Definition() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema.getType(new Selector(List.of("unknown")));
        });
    }

    @Test
    void Get_Validation_Rule_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema.getValidationRule();
        });
    }
}
