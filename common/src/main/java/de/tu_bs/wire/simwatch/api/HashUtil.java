package de.tu_bs.wire.simwatch.api;

import java.math.BigInteger;

/**
 * Implements Chaum, van Heijst, and Pfitzmann's hashing algorithm to shorten a hex-String to 20
 * bits (5 hex characters). This algorithm provably is strongly collision free
 */
public class HashUtil {

    public static BigInteger prime = new BigInteger("1048343", 10);
    public static BigInteger base1 = new BigInteger("1337", 10);
    public static BigInteger base2 = new BigInteger("13337", 10);

    BigInteger p;
    BigInteger g0;
    BigInteger g1;

    public HashUtil(BigInteger p, BigInteger g0, BigInteger g1) {
        this.p = p;
        this.g0 = g0;
        this.g1 = g1;
    }

    /**
     * Shortens the given string to 5 characters by hashing
     *
     * @param str any hex-String
     * @return a hex-String of length 5
     */
    public static String shorten(String str) {
        BigInteger unhashedValue = new BigInteger(str, 16);
        BigInteger hashValue = new HashUtil(prime, base1, base2).hash(unhashedValue);
        return fill(hashValue.toString(16));
    }

    private static String fill(String str) {
        if (str.length() >= 5) {
            return str;
        } else {
            return "0" + str;
        }
    }

    private static BigInteger[] chop(BigInteger in, int bits) {
        BigInteger left = in.shiftRight(bits);
        BigInteger right = in.subtract(left.shiftLeft(bits));
        return new BigInteger[]{left, right};
    }

    private static BigInteger concat(BigInteger left, BigInteger right) {
        return concat(left, right, right.bitLength());
    }

    private static BigInteger concat(BigInteger left, BigInteger right, int rightBitLength) {
        if (right.bitLength() > rightBitLength) {
            throw new IllegalArgumentException("right side has " + right.bitLength() + " bits. Cannot be extended to "
                    + rightBitLength + " bits");
        }
        return left.shiftLeft(rightBitLength).add(right);
    }

    private BigInteger hash(BigInteger in) {
        int t = getBitsOut();
        int m = getMaxBitsIn() * 2;
        int n = in.bitLength();
        int k = (int) Math.ceil((double) n / (m - t - 1));
        BigInteger[] xi = new BigInteger[k + 1];
        for (int i = 0; i < k - 1; i++) {
            BigInteger[] tmp = chop(in, n - (m - t - 1));
            n -= (m - t - 1);
            xi[i] = tmp[0];
            in = tmp[1];
        }
        int d = m - t - 1 - in.bitLength();
        xi[k - 1] = in.shiftLeft(d);
        xi[k] = BigInteger.valueOf(d);
        BigInteger[] gi = new BigInteger[k + 1];
        gi[0] = compress(xi[0]);
        for (int i = 1; i < k + 1; i++) {
            gi[i] = compress(concat(concat(gi[i - 1], BigInteger.ONE), xi[i], m - t - 1));
        }
        return gi[k];
    }

    private int getMaxBitsIn() {
        return p.bitLength() - 1;
    }

    private int getBitsOut() {
        return p.bitLength();
    }

    private BigInteger compress(BigInteger in) {
        if (in.bitLength() > getMaxBitsIn() * 2) {
            throw new IllegalArgumentException("Given number has " + in.bitLength() + " bits. Maximum is "
                    + (getMaxBitsIn() * 2));
        }
        if (in.bitLength() > getMaxBitsIn()) {
            BigInteger[] tmp = chop(in, in.bitLength() - getMaxBitsIn());
            return compress(tmp[0], tmp[1]);
        } else {
            return compress(BigInteger.ZERO, in);
        }
    }

    private BigInteger compress(BigInteger in1, BigInteger in2) {
        if (in1.bitLength() > getMaxBitsIn()) {
            throw new IllegalArgumentException("First number has too many bits");
        }
        if (in2.bitLength() > getMaxBitsIn()) {
            throw new IllegalArgumentException("Second number has too many bits");
        }
        BigInteger result = g0.modPow(in1, p).multiply(g1.modPow(in2, p)).mod(p);
        if (result.bitLength() > getBitsOut()) {
            System.err.println("Compression error. Compressed data has " + result.bitLength() + " bits. Maximum is " + getBitsOut());
        }
        return result;
    }
}
