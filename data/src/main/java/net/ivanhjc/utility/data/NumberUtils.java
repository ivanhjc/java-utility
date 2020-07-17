package net.ivanhjc.utility.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Huang on 2017/6/27
 */
public class NumberUtils {

    /**
     * Factorial - a recursive approach
     *
     * @param n A non-negative integer
     * @return the factorial of n
     */
    public static int factorial(int n) {
        if (n == 1 || n == 0)
            return 1;
        return factorial(n - 1) * n;
    }

    /**
     * Factorial - a loop approach
     *
     * @param n A non-negative integer
     * @return the factorial of n
     */
    public static int factorialLoop(int n) {
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Returns the greatest common divisor of two integers
     *
     * @see org.apache.commons.math3.util.ArithmeticUtils#gcd(int, int)
     * @see java.math.BigInteger#gcd(BigInteger)
     */
    public static int gcd(int a, int b) {
        if (b == 0)
            return Math.abs(a);
        return gcd(b, a % b);
    }

    /**
     * Checks if a number is in the range specified by the given range string. A range is formatted using the standard mathematical notation,
     * such as [0, 10) or (-1, 9). To represent infinity, use "INF", such as (INF, 0) or (0, INF).
     *
     * @param range the range string
     * @param n     the number to check
     * @return true if the number is in the range, false otherwise.
     */
    public static boolean inRange(String range, double n) {
        char leftBracket = range.charAt(0);
        if (leftBracket != '[' && leftBracket != '(')
            throw new IllegalArgumentException("The argument \"range\" is not formatted correctly. " +
                    "A range is formatted using the standard mathematical notation, such as [0, 10) or (-1, 9).");

        char rightBracket = range.charAt(range.length() - 1);
        int idx = range.indexOf(',');
        String minStr = range.substring(1, idx);
        String maxStr = range.substring(idx + 1, range.length() - 1).trim();
        double min = minStr.equals("INF") ? Double.NEGATIVE_INFINITY : Double.valueOf(minStr);
        double max = maxStr.equals("INF") ? Double.POSITIVE_INFINITY : Double.valueOf(maxStr);

        return (leftBracket == '[' ? n >= min : n > min) && (rightBracket == ']' ? n <= max : n < max);
    }

    /**
     * Checks if a number is in the range specified by the given range string, beyond the lower limit of the range, or beyond the upper limit of the range.
     * A range is formatted using the standard mathematical notation, such as [0, 10) or (-1, 9).
     *
     * @param range the range string
     * @param n     the number to check
     * @return in the range -> 0 <br>
     * beyond the lower limit -> -1 <br>
     * beyond the upper limit -> 1.
     * @see #inRange(String, double)
     */
    public static int compareToRange(String range, double n) {
        char leftBracket = range.charAt(0);
        if (leftBracket != '[' && leftBracket != '(')
            throw new IllegalArgumentException("The argument \"range\" is not formatted correctly. " +
                    "A range is formatted using the standard mathematical notation, such as [0, 10) or (-1, 9)");

        char rightBracket = range.charAt(range.length() - 1);
        int idx = range.indexOf(',');
        String minStr = range.substring(1, idx);
        String maxStr = range.substring(idx + 1, range.length() - 1).trim();
        double min = minStr.equals("INF") ? Double.NEGATIVE_INFINITY : Double.valueOf(minStr);
        double max = maxStr.equals("INF") ? Double.POSITIVE_INFINITY : Double.valueOf(maxStr);

        boolean inLeft = leftBracket == '[' ? n >= min : n > min;
        boolean inRight = rightBracket == ']' ? n <= max : n < max;
        return !inLeft ? -1 : !inRight ? 1 : 0;
    }

    /**
     * Giving an array of ranges in ascending order, returns the range which contains the target number.
     *
     * @param ranges the array of ranges
     * @param target the target number to check
     * @return the range string
     * @see #inRange(String, double)
     * @see #compareToRange(String, double)
     */
    public static String getRange(String[] ranges, double target) {
        int i = 0, j = ranges.length - 1;
        while (i <= j) {
            int mid = (i + j) / 2;
            if (inRange(ranges[mid], target)) {
                return ranges[mid];
            } else if (compareToRange(ranges[mid], target) > 0) {
                i = mid + 1;
            } else {
                j = mid - 1;
            }
        }
        return null;
    }

    /**
     * Gets the counts of numbers that fall into each range specified in ranges.
     *
     * @param ranges  the array of ranges to check
     * @param numbers the numbers to count
     * @return the count of each range
     * @see #inRange(String, double)
     */
    public static Map<String, Integer> getHistogramOfRanges(String[] ranges, double... numbers) {
        Map<String, Integer> hist = new LinkedHashMap<>();
        for (String range : ranges) {
            hist.put(range, 0);
        }
        for (double n : numbers) {
            String range = getRange(ranges, n);
            if (range == null)
                range = "Not in any range";
            Integer count = hist.get(range);
            if (count == null)
                count = 0;
            hist.put(range, count + 1);
        }
        return hist;
    }

    /**
     * Get a case study of the unary bitwise complement operator <b>~</b>
     *
     * @param start the start number of the range
     * @param end   the last number of the range
     */
    public static void caseStudyBitwiseComplement(int start, int end) {
        String pattern = String.format("%%-10s%%-10s%%-10s%%-10s%%-%1$ds%%-%1$ds%n", Byte.SIZE + 2);
        System.out.printf(pattern, "type", "radix", "number", "~number", "binary", "~binary");
        for (int number = start; number > end; number--) {
            System.out.printf(pattern, "byte", 10, number, ~number, getBinaryStringByte(number), getBinaryStringByte(~number));
        }
    }

    public static void getShift(byte start, byte end, String operator, int shift) {
        String pattern = String.format("%%-10s%%-10s%%-10s%%-10s%%-%1$ds%%-%1$ds%n", Byte.SIZE + 2);
        String pattern2 = "%" + Byte.SIZE + "s";
        System.out.printf(pattern, "type", "radix", "number", "number " + operator + " " + shift,
                "binary", "binary " + operator + " " + shift);
        for (byte number = start; number > end; number--) {
            byte temp = 0;
            if (operator.equals("<<"))
                temp = (byte) (temp << shift);
            else if (operator.equals(""))
                System.out.printf(pattern, "byte", 10, number, number,
                        String.format(pattern2, Integer.toBinaryString(number & 0xFF)).replace(' ', '0'),
                        String.format(pattern2, Integer.toBinaryString(~number & 0xFF)).replace(' ', '0'));
        }
    }

    public static String getBinaryStringByte(int number) {
        return String.format("%8s", Integer.toBinaryString(number & 0xFF)).replace(' ', '0');
    }

    public static String getBinaryStringInteger(int number) {
        return String.format("%32s", Integer.toBinaryString(number)).replace(' ', '0');
    }

    /**
     * Format a binary string into equal-size groups of characters
     *
     * @param s         the binary string to be formatted
     * @param delimiter a string that separates those groups
     * @param subLen    the size of one group of characters
     * @return the formatted string
     */
    public static String format(String s, String delimiter, int subLen) {
        String[] strs = StringUtils.splitEqually(s, subLen, 0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            sb.append(strs[i]);
            if (i < strs.length - 1)
                sb.append(delimiter);
        }
        return sb.toString();
    }

    private boolean isPrime(int n) {
        if (n < 2 || (n != 2 && isEven(n)))
            return false;

        for (int i = 3; i <= Math.sqrt(n); i++) {
            if (n % i == 0)
                return false;
        }
        return true;
    }

    private boolean isEven(int n) {
        return n % 2 == 0;
    }

    public static List<Integer> factors(int n) {
        List<Integer> list = new ArrayList<>();
        for (int i = 2; i < n / 2; i++) {
            if (n % i == 0)
                list.add(i);
        }
        return list;
    }

    /**
     * Formats a decimal number to that preserving trailing zeros or dropping them
     *
     * @param n         the number to format
     * @param precision the number of digits after decimal points to preserve
     * @param trimZeros true if throwing away trailing zeros, false otherwise
     * @return {@code (100.00800, 2, true) -> "100.01"} <br>
     * {@code (100.00400, 2, true) -> "100"} <br>
     * {@code (100.00800, 4, false) -> "100.0080"} <br>
     */
    public static String formatDecimal(BigDecimal n, int precision, boolean trimZeros) {
        if (trimZeros) {
            String format = "%-" + (precision + 2) + "s";
            String pattern = String.format(format, "0.").replace(" ", "#");
            return new DecimalFormat(pattern).format(n);
        } else {
            return n.setScale(precision, BigDecimal.ROUND_HALF_UP).toString();
        }
    }

    /**
     * A wrapping method for {@link #formatDecimal(BigDecimal, int, boolean)}
     */
    public static String formatDecimal(double n, int precision, boolean trimZeros) {
        return formatDecimal(new BigDecimal(n), precision, trimZeros);
    }
}
