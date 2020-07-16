package net.ivanhjc.utility.data;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Huang on 2017/6/27
 */
public class NumberUtils {
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
     *         {@code (100.00400, 2, true) -> "100"} <br>
     *         {@code (100.00800, 4, false) -> "100.0080"} <br>
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
