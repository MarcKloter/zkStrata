package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.custom.HexLiteral;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JsonAccessorTest {
    private static final String DATA_PATH = "src/test/resources/miscellaneous/";

    private JsonAccessor accessor;

    @BeforeEach
    void setup() {
        this.accessor = new JsonAccessor(DATA_PATH + "accessor_test.json");
    }

    @Test
    void Not_Existent_File_Should_Throw() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JsonAccessor(DATA_PATH + "nonexistent_file.json");
        });
    }

    @Test
    void Get_Value_Missing() {
        Value value = this.accessor.getValue(new Selector(List.of("missing")));
        assertNull(value);
    }

    @Test
    void Get_Value_Invalid() {
        Value value = this.accessor.getValue(new Selector(List.of("string", "string")));
        assertNull(value);
    }

    @Test
    void Get_Value_Object() {
        Value value = this.accessor.getValue(new Selector(List.of("object")));
        assertNull(value);
    }

    @Test
    void Get_Value_Array() {
        Value value = this.accessor.getValue(new Selector(List.of("array")));
        assertNotNull(value);
        assertEquals(value.getClass(), Literal.class);
        Literal literal = (Literal) value;
        assertTrue(List.class.isAssignableFrom(literal.getType()));
        List list = (List) literal.getValue();
        assertEquals(3, list.size());
        assertEquals("element0", list.get(0));
        assertEquals("element1", list.get(1));
        assertEquals("element2", list.get(2));
    }

    @Test
    void Get_Value_BigInteger_1() {
        Value value = this.accessor.getValue(new Selector(List.of("number1")));
        assertNotNull(value);
        assertEquals(value.getClass(), Literal.class);
        Literal literal = (Literal) value;
        assertEquals(BigInteger.class, literal.getType());
        assertEquals(BigInteger.valueOf(42), literal.getValue());
    }

    @Test
    void Get_Value_BigInteger_2() {
        Value value = this.accessor.getValue(new Selector(List.of("number2")));
        assertNotNull(value);
        assertEquals(value.getClass(), Literal.class);
        Literal literal = (Literal) value;
        assertEquals(BigInteger.class, literal.getType());
        assertEquals(BigInteger.valueOf(8932614873L), literal.getValue());
    }

    @Test
    void Get_Value_String() {
        Value value = this.accessor.getValue(new Selector(List.of("string")));
        assertNotNull(value);
        assertEquals(value.getClass(), Literal.class);
        Literal literal = (Literal) value;
        assertEquals(String.class, literal.getType());
        assertEquals("John", literal.getValue());
    }

    @Test
    void Get_Value_HexString() {
        Value value = this.accessor.getValue(new Selector(List.of("hexString")));
        assertNotNull(value);
        assertEquals(value.getClass(), HexLiteral.class);
        assertEquals(new BigInteger("abcdef", 16), ((HexLiteral) value).getValue());
    }

    @Test
    void Get_Empty_KeySet() {
        Set<String> keySet = this.accessor.getKeySet(List.of("string"));
        assertEquals(Collections.emptySet(), keySet);
    }
}
