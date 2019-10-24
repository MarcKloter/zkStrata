package zkstrata.utils;

import java.math.BigInteger;

public class SemanticsUtils {
    private SemanticsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean testMaxBitSize(BigInteger bigInteger, int maxNumberOfBits) {
        return bigInteger.bitCount() > maxNumberOfBits;
    }
}
