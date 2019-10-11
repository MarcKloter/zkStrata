package zkstrata.utils;

import java.math.BigInteger;

public class SemanticsUtils {
    public static boolean testMaxBitSize(BigInteger bigInteger, int maxNumberOfBits) {
        return bigInteger.bitCount() > maxNumberOfBits;
    }
}
