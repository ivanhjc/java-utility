package net.ivanhjc.utility.data;
;
import net.ivanhjc.utility.data.model.ObjectCountModel;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Ivan Huang on 2018/5/8 15:09.
 */
public class AlgorithmUtilsTest {

    private final static int[] arr = new int[]{-1, 1, 0, 37, 28, 0, -11, 0};
    private final static int[] arr2 = new int[]{1, 3, 0, 0, 3, 9, 0, 1, 1, 0, 7};

    @Test
    public void gcd() {
        int[] arr1 = new int[]{10, 33, 187, 9201, 3346, 755, 890101030, -3, 0};
        int[] arr2 = new int[]{223, 17, 9125, 42, 5012342, 20550, 9, 0};
        for (int a : arr1) {
            for (int b : arr2) {
                System.out.println(a + "," + b + ": " + AlgorithmUtils.gcd(a, b));
            }
        }
    }

    @Test
    public void count() {
        System.out.println(AlgorithmUtils.count(arr, 0));
        System.out.println(AlgorithmUtils.count(arr, 10, 100));
    }

    @Test
    public void histogram() {
        ListUtils.println(AlgorithmUtils.histogram(arr2));
    }

    @Test
    public void bubbleSort() {
        AlgorithmUtils.bubbleSort(arr);
        ListUtils.println(arr);
        AlgorithmUtils.bubbleSort2(arr);
        ListUtils.println(arr);
    }

    @Test
    public void insertionSort() {
        AlgorithmUtils.insertionSort(arr);
        ListUtils.println(arr);
    }

    @Test
    public void selectionSort() {
        AlgorithmUtils.selectionSort(arr);
        ListUtils.println(arr);
    }

    @Test
    public void selectionSort2() {
        AlgorithmUtils.selectionSort2(arr);
        ListUtils.println(arr);
    }

    @Test
    public void mergeSort() {
        int[] arr1 = new int[]{1, 2, 3, 4, 5, 0, -2, 100, 9, -7, 3};
        AlgorithmUtils.mergeSort(arr1, 0, arr1.length - 1);
        System.out.println(Arrays.toString(arr1));
    }

    @Test
    public void quickSort() {
        int[] arr1 = new int[]{2, 3, 1, 5, 7};
        AlgorithmUtils.quickSort(arr1, 0, arr1.length - 1);
        ListUtils.println(arr1);
    }

    @Test
    public void unsort() {
        int[] arr1 = new int[]{1, 2, 3, 4, 5, 0, -2, 100, 9, -7, 3};
        AlgorithmUtils.quickSort(arr1, 0, arr1.length - 1);
        ListUtils.println(arr1);
        AlgorithmUtils.unsort(arr1);
        ListUtils.println(arr1);
        AlgorithmUtils.mergeSort(arr1, 0, arr1.length - 1);
        ListUtils.println(arr1);
    }

    @Test
    public void sortByFixedOrder() {
        List<String> arr = new ArrayList<>(Arrays.asList("e","a","z","a","c","e","b"));
        String order = "z,b,c,a,e,a,b,";
        AlgorithmUtils.sortByFixedOrder(arr, order);
        System.out.println(arr);
    }

    @Test
    public void binarySearch() {
        int[] arr1 = new int[]{1, 2, 3, 4, 5};
        System.out.println(AlgorithmUtils.binarySearch(arr1, 0, arr1.length - 1, 3));
        System.out.println(AlgorithmUtils.binarySearch2(arr1, 0, arr1.length - 1, 3));
    }

    @Test
    public void inRange() {
        System.out.println(AlgorithmUtils.inRange("[0, 1]", 1.0));
        System.out.println(AlgorithmUtils.inRange("[0, 1)", 1.0));
        System.out.println(AlgorithmUtils.inRange("(0, 1]", 1.0));
        System.out.println(AlgorithmUtils.inRange("(0, 1)", 1.0));
        System.out.println(AlgorithmUtils.inRange("(0, INF)", 1.0));
        System.out.println(AlgorithmUtils.inRange("(-101, INF)", -100));
    }

    @Test
    public void compareTo() {
        System.out.println(AlgorithmUtils.compareToRange("[0, 1]", 1.0));
        System.out.println(AlgorithmUtils.compareToRange("[0, 1)", 1.0));
        System.out.println(AlgorithmUtils.compareToRange("(0, 1]", 1.0));
        System.out.println(AlgorithmUtils.compareToRange("(0, 1)", 0));
        System.out.println(AlgorithmUtils.compareToRange("(0, INF)", 1.0));
        System.out.println(AlgorithmUtils.compareToRange("(INF, -101)", -100));
    }

    @Test
    public void getRange() {
        System.out.println(AlgorithmUtils.getRange(new String[]{"[0, 1]", "(1, 5.5]", "(5.5, 9]"}, 3.9));
        System.out.println(AlgorithmUtils.getRange(new String[]{"[0, 1]", "(1, 5.5]", "(5.5, 9]"}, 10));
        System.out.println(AlgorithmUtils.getRange(new String[]{"[0, 1]", "(1, 5.5]", "(5.5, INF]"}, 1000));
    }

    @Test
    public void getHistogramOfRanges() {
        String[] ranges = {"(INF, 30)", "[30, 60)", "[60, 90)", "[90, 120)", "[120, INF)"};
        System.out.println(MapUtils.getTable(AlgorithmUtils.getHistogramOfRanges(ranges, -199, 20, 37, 99, 132, 9095, 0, 1024, 120), "range", "count"));
//        System.out.println(MapUtils.getTable(MathUtils.getHistogramOfRanges(new String[]{}, 1, 2, 3, 4, 5.5, 10, 21, -19), "range", "count"));
    }

    @Test
    public void fillGapsInListList() throws ParseException {
        List<ObjectCountModel> orderCounts = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ObjectCountModel count1 = new ObjectCountModel();
        count1.setDate(format.parse("2018-06-22"));
        count1.setCount(100);
        ObjectCountModel count2 = new ObjectCountModel();
        count2.setDate(format.parse("2018-06-26"));
        count2.setCount(300);
        orderCounts.addAll(Arrays.asList(count1, count2));
        System.out.println(StringUtils.GSON_NULL.toJson(AlgorithmUtils.fillGapsInList(orderCounts, 2)));
    }

    @Test
    public void selectByChance() {
        Map<String, Double> map = new HashMap<>();
        map.put("Ivan", 0.7);
        map.put("John", 0.01);
        map.put("Alex", 0.2);
        map.put("Mark", 0.1);

        Map<Object, Integer> histogram = new HashMap<>();
        map.forEach((k, v)-> histogram.put(k, 0));
        histogram.put(null, 0);
        for (int i = 0; i < 1000; i++) {
            String win = AlgorithmUtils.selectByChance(map);
            histogram.put(win, histogram.get(win) + 1);
        }
        System.out.println(MapUtils.getTable(histogram, "Item", "Times"));
    }
}
