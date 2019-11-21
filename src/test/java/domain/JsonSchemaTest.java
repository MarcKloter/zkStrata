package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;
import zkstrata.domain.data.types.custom.HexLiteral;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaTest {
    private static final String DATA_PATH = "src/test/resources/miscellaneous/";

    private JsonSchema schema1;
    private JsonSchema schema2;

    @BeforeEach
    void setup() {
        this.schema1 = new JsonSchema(DATA_PATH + "schema_test_1.schema.json", "test_schema");
        this.schema2 = new JsonSchema(DATA_PATH + "schema_test_2.schema.json", "test_schema");
    }

    @Test
    void Get_Type_Undefined_Property() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema1.getType(new Selector(List.of("missing")));
        });
    }

    @Test
    void Get_Type_Invalid_Type_Definition() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema1.getType(new Selector(List.of("invalid")));
        });
    }

    @Test
    void Get_Type_Unknown_Type_Definition() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema1.getType(new Selector(List.of("unknown")));
        });
    }

    @Test
    void Get_Type_Hex_Literal() {
        assertEquals(HexLiteral.class, this.schema2.getType(new Selector(List.of("string_hex"))));
    }

    @Test
    void Get_Type_String() {
        assertEquals(String.class, this.schema2.getType(new Selector(List.of("string"))));
    }

    @Test
    void Get_Type_Number() {
        assertEquals(BigInteger.class, this.schema2.getType(new Selector(List.of("number"))));
    }

    @Test
    void Get_Validation_Rule_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.schema1.getValidationRule();
        });
    }

    @Test
    void Missing_Validation_Rule() {
        assertNull(schema2.getValidationRule());
    }
}
