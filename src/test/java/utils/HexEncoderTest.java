package utils;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import zkstrata.utils.HexEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HexEncoderTest {
    @Test
    void Encode_Unknown_Object() {
        String value = "StringValue";
        UnknownEncodeable unknownEncodeable = new UnknownEncodeable(value);
        String expected = Hex.encodeHexString(value.getBytes());
        assertEquals(expected, HexEncoder.encode(unknownEncodeable));
    }

    public static class UnknownEncodeable {
        private String value;

        public UnknownEncodeable(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
