package utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HashCracker {

    /**
     * Searches for @param hashToFind using {@link #getHashCodeFromString(String) getHashCodeFromString}
     * in @param range.
     * @param from Start of range to find @param hashToFind in.
     * @param to End of range to find @param hashToFind in.
     * @param hashToFind The hash code in SHA-1 base64 encoding.
     * @return The corresponding string that is the value for the SHA-1 base 64 encoding given by @param hashToFind
     * if found in range, else returns null.
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String searchInRange(String from, String to, String hashToFind, byte len)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String fillerString = createFillerString(len, 'a');
        final long lower = alphabeticToDecimal(from);
        final long upper = alphabeticToDecimal(to);
        final BigInteger search = new BigInteger(hashToFind, 16);
        for (long curr = lower; curr <= upper && !Thread.currentThread().isInterrupted(); ++curr) {
            String alpha = decimalToAlphabetic(curr);
            String currAlphabetic = fillerString.substring(alpha.length()) + alpha;
            if (getHashCodeFromString(currAlphabetic).equals(search)) {
                return currAlphabetic;
            }
        }
        return null;
    }

    private static String createFillerString(int len, char filler) {
        final char[] fillerChars = new char[len];
        Arrays.fill(fillerChars, filler);
        return new String(fillerChars);
    }

    /**
     * Converts string to corresponding SHA-1 encoding in base64.
     * @param str String to convert to SHA-1 base64
     * @return The converted SHA-1 base64 string.
     */
    private static BigInteger getHashCodeFromString(String str)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.update(str.getBytes("UTF-8"));
        final byte[] byteData = crypt.digest(); // Get SHA-1 encoding.
        return new BigInteger(1, byteData);
    }


    /**
     * PRE: @param str contains only english alphabetical lowercase.
     * Converts string containing alphabetical lowercase to corresponding decimal value.
     * @param str Alphabetical string to convert.
     * @return The decimal value of the string
     */
    public static long alphabeticToDecimal(String str) {
        List<Character> chars = str.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        int[] pow = { chars.size() - 1 };
        return chars.stream().map(c -> (long) (c - 'a')).reduce(0L,
                (acc, val) -> (val * (long) Math.pow(26, pow[0]--)) + acc);
    }

    /**
     * Converts decimal value to corresponding lowercase alphabetic string.
     * @param num Number to convert to alphabetical string.
     * @return The string value corresponding with @param num
     */
    public static String decimalToAlphabetic(long num) {
        char[] str = Long.toString(num, 26).toCharArray();
        for (int i = 0; i < str.length; i++) {
            str[i] += str[i] > '9' ? 10 : 49;
        }
        return new String(str);
    }
}
