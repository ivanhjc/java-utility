package net.ivanhjc.utility.data;

import org.junit.Test;
import java.math.BigDecimal;

/**
 * @author Ivan Huang on 2018/8/31 18:38.
 */
public class NumberUtilsTest {

    @Test
    public void gcd() {
        int[] arr1 = new int[]{10, 33, 187, 9201, 3346, 755, 890101030, -3, 0};
        int[] arr2 = new int[]{223, 17, 9125, 42, 5012342, 20550, 9, 0};
        for (int a : arr1) {
            for (int b : arr2) {
                System.out.println(a + "," + b + ": " + NumberUtils.gcd(a, b));
            }
        }
    }

    @Test
    public void inRange() {
        System.out.println(NumberUtils.inRange("[0, 1]", 1.0));
        System.out.println(NumberUtils.inRange("[0, 1)", 1.0));
        System.out.println(NumberUtils.inRange("(0, 1]", 1.0));
        System.out.println(NumberUtils.inRange("(0, 1)", 1.0));
        System.out.println(NumberUtils.inRange("(0, INF)", 1.0));
        System.out.println(NumberUtils.inRange("(-101, INF)", -100));
    }

    @Test
    public void compareTo() {
        System.out.println(NumberUtils.compareToRange("[0, 1]", 1.0));
        System.out.println(NumberUtils.compareToRange("[0, 1)", 1.0));
        System.out.println(NumberUtils.compareToRange("(0, 1]", 1.0));
        System.out.println(NumberUtils.compareToRange("(0, 1)", 0));
        System.out.println(NumberUtils.compareToRange("(0, INF)", 1.0));
        System.out.println(NumberUtils.compareToRange("(INF, -101)", -100));
    }

    @Test
    public void getRange() {
        System.out.println(NumberUtils.getRange(new String[]{"[0, 1]", "(1, 5.5]", "(5.5, 9]"}, 3.9));
        System.out.println(NumberUtils.getRange(new String[]{"[0, 1]", "(1, 5.5]", "(5.5, 9]"}, 10));
        System.out.println(NumberUtils.getRange(new String[]{"[0, 1]", "(1, 5.5]", "(5.5, INF]"}, 1000));
    }

    @Test
    public void getHistogramOfRanges() {
        String[] ranges = {"(INF, 30)", "[30, 60)", "[60, 90)", "[90, 120)", "[120, INF)"};
        System.out.println(MapUtils.getTable(NumberUtils.getHistogramOfRanges(ranges, -199, 20, 37, 99, 132, 9095, 0, 1024, 120), "range", "count"));
//        System.out.println(MapUtils.getTable(MathUtils.getHistogramOfRanges(new String[]{}, 1, 2, 3, 4, 5.5, 10, 21, -19), "range", "count"));
    }
    
    @Test
    public void formatDecimal() {
        System.out.println(NumberUtils.formatDecimal(new BigDecimal(100.00800), 4, false));
        System.out.println(NumberUtils.formatDecimal(100.008004, 5, true));
    }
}
