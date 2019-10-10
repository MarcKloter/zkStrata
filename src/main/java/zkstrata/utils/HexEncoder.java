package zkstrata.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.math.BigInteger;

public class HexEncoder {
    private static final Logger LOGGER = LogManager.getLogger(HexEncoder.class);

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
            String msg = String.format("Invalid implementation of method encode for type %s.", type);
            throw new IllegalStateException(msg);
        }
    }

    public static String encode(BigInteger bigInteger) {
        return String.format("%02x", bigInteger);
    }

    public static String encode(Integer integer) {
        return String.format("%02x", integer);
    }

    public static String encode(String string) {
        return Hex.encodeHexString(string.getBytes());
    }
}
