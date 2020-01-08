package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.schemas.dynamic.JsonSchema;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.utils.StatementBuilder;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaTest {
    private static final String DATA_PATH = "src/test/resources/miscellaneous/";

    private JsonSchema schema1;
    private JsonSchema schema2;
    private JsonSchema schema3;
    private JsonSchema schema4;

    @BeforeEach
    void setup() {
        this.schema1 = new JsonSchema(DATA_PATH + "schema_test_1.schema.json", "test_schema");
        this.schema2 = new JsonSchema(DATA_PATH + "schema_test_2.schema.json", "test_schema");
        this.schema3 = new JsonSchema(DATA_PATH + "schema_test_3.schema.json", "test_schema");
        this.schema4 = new JsonSchema(DATA_PATH + "schema_test_4.schema.json", "test_schema");
    }

    @Test
    void Get_Type_Undefined_Property() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                this.schema1.getType(new Selector(List.of("missing")))
        );
        assertTrue(exception.getMessage().toLowerCase().contains("missing a type definition"));
    }

    @Test
    void Get_Type_Invalid_Type_Definition() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                this.schema1.getType(new Selector(List.of("invalid")))
        );
        assertTrue(exception.getMessage().toLowerCase().contains("invalid type"));
    }

    @Test
    void Get_Type_Unknown_Type_Definition() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                this.schema1.getType(new Selector(List.of("unknown")))
        );
        assertTrue(exception.getMessage().toLowerCase().contains("unknown type"));
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                this.schema1.getValidationRule()
        );
        assertTrue(exception.getMessage().toLowerCase().contains("invalid field validationrule"));
    }

    @Test
    void Missing_Validation_Rule() {
        assertFalse(schema2.hasValidationRule());
        assertNull(schema2.getValidationRule());
    }

    @Test
    void Only_Implicit_Validation_Rule() {
        assertTrue(schema4.hasValidationRule());
        String exptected = new StatementBuilder()
                .subjectThis()
                .greaterThan("private.numberConstraint2", "23", false)
                .lessThan("private.numberConstraint1", "15", false)
                .build();
        assertEquals(exptected, schema4.getValidationRule());
    }

    @Test
    void Invalid_Number_Constraint_Value() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                this.schema3.getValidationRule()
        );
        assertTrue(exception.getMessage().toLowerCase().contains("invalid value for validation keyword"));
    }
}
