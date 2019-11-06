package zkstrata.utils;

import java.math.BigInteger;

public class SemanticsUtils {
    private SemanticsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final BigInteger ED25519_PRIME_ORDER = new BigInteger("1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ed", 16);

    public static boolean testMaxBitSize(BigInteger bigInteger, int maxNumberOfBits) {
        return bigInteger.bitCount() > maxNumberOfBits;
    }
}
