package zkstrata.utils;

import org.apache.commons.codec.binary.Hex;

import java.lang.reflect.Method;

public class HexEncoder {
    public static String encode(Object object) {
        Class<?> type = object.getClass();
        try {
            Method encode = HexEncoder.class.getMethod("encode", type);
            return (String) encode.invoke(HexEncoder.class, object);
        } catch (NoSuchMethodException e) {
            // TODO: display warning (defaulted to this)
            return encode(object.toString());
        } catch (ReflectiveOperationException e) {
            String msg = String.format("Invalid implementation of method encode for type %s.", type);
            throw new IllegalStateException(msg);
        }
    }

    public static String encode(Integer integer) {
        return Integer.toHexString(integer);
    }

    public static String encode(String string) {
        return Hex.encodeHexString(string.getBytes());
    }
}
