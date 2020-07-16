package net.ivanhjc.utility.data;

import net.ivanhjc.utility.data.model.ObjectCountModel;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Ivan Huang on 2018/5/8 13:59.
 */
public class AlgorithmUtils {

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

    public static int count(int[] array, int a) {
        int count = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == a)
                count++;
        }
        return count;
    }

    public static int count(int[] array, int low, int high) {
        int count = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] >= low && array[i] <= high)
                count++;
        }
        return count;
    }

    public static int[] histogram(int[] array) {
        int[] count = new int[10];
        for (int i = 0; i < array.length; i++) {
            int index = array[i];
            count[index]++;
        }
        return count;
    }

    /**
     * Ascending order
     */
    public static void bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j + 1];
                    arr[j + 1] = arr[j];
                    arr[j] = temp;
                }
            }
        }
    }

    /**
     * Descending order
     */
    public static void bubbleSort2(int[] arr) {
        int swaps = 1;
        while (swaps > 0) {
            swaps = 0;
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i] < arr[i + 1]) {
                    int temp = arr[i];
                    arr[i] = arr[i + 1];
                    arr[i + 1] = temp;
                    swaps++;
                }
            }
        }
    }

    /**
     * Ascending order
     */
    public static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                int temp = arr[i];
                for (int j = i - 1; j >= 0; j--) {
                    arr[j + 1] = arr[j];
                    if (j == 0 || arr[j - 1] < temp) {
                        arr[j] = temp;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Ascending order
     */
    public static void selectionSort(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] > arr[j]) {
                    int temp = arr[j];
                    arr[j] = arr[i];
                    arr[i] = temp;
                }
            }
        }
    }

    /**
     * Ascending order
     */
    public static void selectionSort2(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            int idx = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[idx] > arr[j])
                    idx = j;
            }
            if (idx != i) {
                int temp = arr[i];
                arr[i] = arr[idx];
                arr[idx] = temp;
            }
        }
    }

    /**
     * Ascending order
     *
     * @param arr
     * @param left
     * @param right
     */
    public static void mergeSort(int[] arr, int left, int right) {
        if (left >= right)
            return;

        int mid = (left + right) / 2;
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);

        int[] arr1 = new int[mid - left + 1];
        int[] arr2 = new int[right - mid];
        for (int i = 0; i < arr1.length; i++) {
            arr1[i] = arr[left + i];
        }
        for (int i = 0; i < arr2.length; i++) {
            arr2[i] = arr[mid + 1 + i];
        }
        int i = 0, j = 0, k = left;
        while (true) {
            if (i == arr1.length) {
                if (j == arr2.length)
                    break;
                arr[k++] = arr2[j++];
                continue;
            }
            if (j == arr2.length) {
                arr[k++] = arr1[i++];
                continue;
            }

            if (arr1[i] < arr2[j]) {
                arr[k] = arr1[i++];
            } else {
                arr[k] = arr2[j++];
            }
            k++;
        }
    }

    /**
     * Ascending order
     *
     * @see Arrays#sort(int[])
     */
    public static void quickSort(int[] list, int left, int right) {
        if (left >= right)
            return;
        int pivot = list[left];
        int i = left;
        int j = right;
        while (i < j) {
            while (list[j] > pivot && i < j) {
                j--;
            }
            if (i == j)
                break;
            list[i++] = list[j];
            while (list[i] < pivot && i < j) {
                i++;
            }
            if (i == j)
                break;
            list[j--] = list[i];
        }
        list[i] = pivot;
        quickSort(list, left, i - 1);
        quickSort(list, i + 1, right);
    }

    public static void unsort(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int rdmIdx = (int) (Math.random() * (i + 1));
            int temp = arr[rdmIdx];
            arr[rdmIdx] = arr[i];
            arr[i] = temp;
        }
    }

    /**
     * Sorts a list of objects into a fixed order specified by a comma-separated string. Each element in the string is an
     * identifier for an element or elements in the list. The list can contain duplicate elements, and the ordering string
     * can contain duplicate occurrences of an element, but only the order of the first occurrence will be used. For example,
     * <ul>
     *     <li><code>([e,a,z,c,b], "c,e,a") -> [c,e,a,z,b]</code></li>
     *     <li><code>([e,a,z,c,b], "c,a,e,a,b") -> [c,a,e,b,z]</code></li>
     *     <li><code>([e,a,z,a,c,e,b], "z,b,c,a,e,a,b") -> [z,b,c,a,a,e,e]</code></li>
     * </ul>
     *
     * @param arr   the array to sort
     * @param order the ordering string
     */
    public static void sortByFixedOrder(List<String> arr, String order) {
        List<String> orderList = ListUtils.deduplicate(Arrays.asList(order.split(",")));
        int k = 0;
        for (int i = 0; i < orderList.size(); i++) {
            for (int j = k; j < arr.size(); j++) {
                if (orderList.get(i).equals(arr.get(j))) {
                    arr.add(k, arr.get(j));
                    arr.remove(j + 1);
                    k++;
                }
            }
        }
    }

    /**
     * Binary search with a recursive approach
     *
     * @param arr    the array to be searched
     * @param i      the index of the first item of the range to be searched
     * @param j      the index of the last item of the range to be searched
     * @param target the target item to be searched for
     * @return the index of the target item, or -1 if the target isn't found in the array
     */
    public static int binarySearch(int[] arr, int i, int j, int target) {
        if (target < arr[i] || target > arr[j])
            return -1;

        int mid = (i + j) / 2;
        if (target == arr[mid])
            return mid;
        else if (target > arr[mid])
            i = mid + 1;
        else
            j = mid - 1;

        return binarySearch(arr, i, j, target);
    }

    /**
     * Binary search with an iterative approach
     *
     * @param arr    the array to be searched
     * @param i      the index of the first item of the range to be searched
     * @param j      the index of the last item of the range to be searched
     * @param target the target item to be searched for
     * @return the index of the target item, or -1 if the target isn't found in the array
     */
    public static int binarySearch2(int[] arr, int i, int j, int target) {
        while (i <= j) {
            int mid = (i + j) / 2;
            if (target == arr[mid])
                return mid;
            else if (target > arr[mid])
                i = mid + 1;
            else
                j = mid - 1;
        }
        return -1;
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
     * Makes an ordered-by-date non-continuous list of counting objects continuous on dates by filling objects with count 0 into the gaps
     * between the given objects.
     *
     * @param list     the given objects
     * @param lastDays how many days from now to past
     * @return the filled list
     */
    public static List<ObjectCountModel> fillGapsInList(List<ObjectCountModel> list, int lastDays) {
        List<ObjectCountModel> filledList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1 - lastDays);
        int j = 0;
        for (int i = 0; i < lastDays; i++) {
            if (j == list.size() || !DateUtils.truncatedEquals(cal.getTime(), list.get(j).getDate(), Calendar.DATE)) {
                ObjectCountModel count = new ObjectCountModel();
                count.setDate(cal.getTime());
                count.setCount(0);
                filledList.add(count);

                if (j < list.size() && DateUtils.truncatedCompareTo(cal.getTime(), list.get(j).getDate(), Calendar.DATE) > 0) {
                    j++;
                }

            } else {
                filledList.add(list.get(j));
                j++;
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return filledList;
    }

    /**
     * Selects an item from an array of items each of which has a probability to be selected. For example, if you have these people: Ivan, John, Alex, Mark, and their
     * probabilities to be selected are 0.5, 0.1, 0.3, 0.05 respectively, this method should return one of these people according to their probabilities.
     *
     * @param map key - items to be selected, value - probabilities to be selected
     * @param <T> the type of the items
     * @return the selected item
     */
    public static <T> T selectByChance(Map<T, Double> map) {
        double r = Math.random();
        double low = 0;
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            double high = low + entry.getValue();
            if (r >= low && r < high) {
                return entry.getKey();
            }
            low = high;
        }
        return null;
    }

    /**
     * A recursive approach
     *
     * @param n A non-negative integer
     * @return
     */
    public static int calculateRecursively(int n) {
        if (n == 1 || n == 0)
            return 1;
        return calculateRecursively(n - 1) * n;
    }

    /**
     * A loop approach
     *
     * @param n
     * @return
     */
    public static int calculateWithLoop(int n) {
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
