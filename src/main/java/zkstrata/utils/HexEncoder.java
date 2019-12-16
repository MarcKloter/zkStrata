package zkstrata.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.math.BigInteger;

public class HexEncoder {
    private static final Logger LOGGER = LogManager.getRootLogger();

    private HexEncoder() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Encodes the given {@link Object} as hex string in little endian encoding.
     *
     * @param object object to encode
     * @return little endian hex string representation of the given object
     */
    public static String encode(Object object) {
        Class<?> type = object.getClass();
        try {
            Method encode = HexEncoder.class.getMethod("encode", type);
            return (String) encode.invoke(HexEncoder.class, object);
        } catch (NoSuchMethodException e) {
            LOGGER.warn("No specific hex encoding method found for {}. Default to hex encoding of toString representation.", type);
            return encode(object.toString());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(String.format("Invalid implementation of method encode for type %s.", type));
        }
    }

    public static String encode(BigInteger bigInteger) {
        return pad(String.format("%x", bigInteger));
    }

    public static String encode(String string) {
        return Hex.encodeHexString(string.getBytes());
    }

    private static String pad(String string) {
        if (string.length() % 2 == 1)
            return String.format("0%s", string);
        else
            return string;
    }
}
