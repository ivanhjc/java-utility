package net.ivanhjc.utility.data;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.util.Pair;

import java.util.*;

/**
 * @author Ivan Huang on 2017/1/12
 */
public class ListUtils {

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
     * Check if a string is an element of a string representation of a comma-separated array <br>
     * ("a,b,c,d", "a") -> true
     *
     * @param array formatted like "a,b,c,d"
     * @param item  the element to check, if it's null or empty, return false
     * @return true or false
     */
    public static boolean contains(String array, Object item) {
        if (item == null)
            return false;

        String[] items = array.split(",");
        for (String s : items) {
            if (s.trim().equals(String.valueOf(item))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes elements from a list at specified indices. Reduces the size of the list by the number of removed elements,
     * and shifts all adjacent elements to the left.
     *
     * @param list    the list to be removed from
     * @param indices the indices of the elements to be removed
     * @see org.apache.commons.lang3.ArrayUtils#removeAll(Object[], int...)
     */
    public static void remove(List list, int... indices) {
        Integer[] inds = org.apache.commons.lang3.ArrayUtils.toObject(indices);
        Arrays.sort(inds, (o1, o2) -> o2 - o1);
        for (int i : inds) {
            list.remove(i);
        }
    }

    /**
     * Reverses the order of the given input. This method does nothing if the input is null.
     *
     * @param array the array to reverse, may be null
     * @see org.apache.commons.lang3.ArrayUtils#reverse(int[])
     */
    public static void reverse(int[] array) {
        if (array == null)
            return;

        int temp;
        int len = array.length;
        for (int i = 0; i < len / 2; i++) {
            temp = array[i];
            array[i] = array[len - 1 - i];
            array[len - 1 - i] = temp;
        }
    }

    /**
     * Converts an array of Strings to an array of int numbers
     *
     * @param array the array of integers that are of String type.
     * @return the converted array. If array is null or empty, return null or empty respectively.
     * @see org.apache.commons.lang3.ArrayUtils#toPrimitive(Integer[])
     * @see org.apache.commons.lang3.ArrayUtils#toObject(int[])
     */
    public static int[] parseInt(String[] array) {
        if (array == null)
            return null;

        int[] array2 = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            array2[i] = Integer.parseInt(array[i]);
        }
        return array2;
    }

    /**
     * Converts a string of comma-separated integers to an actual int array
     *
     * @param array the comma-separated integer array, can be null or empty, and may
     *              contain white spaces after the commas
     * @return an int array
     */
    public static int[] parseInt(String array) {
        return parseInt(splitAndTrim(array));
    }

    /**
     * Converts a string of comma-separated integers to a long array
     *
     * @param array the comma-separated integer array, can't be null or empty
     * @return a long array
     */
    public static long[] parseLong(String[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Long.parseLong(array[i]);
        }
        return result;
    }

    /**
     * Converts a string of comma-separated integers to a long array
     *
     * @param array the comma-separated integer array, can be null or empty, and may
     *              contain white spaces after the commas
     * @return a long array
     */
    public static long[] parseLong(String array) {
        return parseLong(splitAndTrim(array));
    }

    /**
     * Converts an array of long numbers to an array of Strings
     */
    public static String[] parseString(long[] array) {
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an int array to a string array, e.g. [1, 2, 3] -> "1,2,3"
     *
     * @param list the int array to convert
     * @return the string array
     * @see #parseInt(String)
     */
    public static <T> String toStringList(Iterable<T> list) {
        StringBuilder builder = new StringBuilder();
        for (T i : list) {
            builder.append(i).append(",");
        }
        return builder.deleteCharAt(builder.length() - 1).toString();
    }

    /**
     * Removes empty characters and commas within a string to subtract only the "meaningful" strings
     *
     * @param str the input string
     * @return an array of strings
     * @throws
     */
    public static String[] splitAndTrim(String str) {
        List<String> result = new ArrayList<>();
        String[] array = str.split(",");
        for (String item : array) {
            item = item.trim();
            if (!item.isEmpty())
                result.add(item);
        }
        return result.toArray(new String[0]);
    }

    /**
     * Converts an array of strings to an int array. Any non-integer strings will be removed.
     *
     * @param array the array to convert, which may contain integers and non-integers
     * @return the array of integers sorted out from the string
     */
    public static int[] toIntArrayIgnoreNonInt(String[] array) {
        List<Integer> array2 = new ArrayList<>();
        for (String item : array) {
            item = item.trim();
            if (StringUtils.isInteger(item))
                array2.add(Integer.parseInt(item));
        }
        return parseInt(array2);
    }

    public static int[] parseInt(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Converts a string of comma-separated integers and substrings to an int array. Any non-integer substrings will be removed.
     *
     * @param array the comma-separated string that may contain numbers and substrings
     * @return the array of integers sorted out from the string
     */
    public static int[] toIntArrayIgnoreNonInt(String array) {
        return toIntArrayIgnoreNonInt(splitAndTrim(array));
    }

    /**
     * Returns a string representation of an array
     *
     * @param array input array
     * @return the string formatted like "[1,2,3]"
     * @see java.util.Arrays#toString(int[])
     */
    public static String toString(int[] array) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i < array.length - 1)
                builder.append(array[i]).append(", ");
            else
                builder.append(array[i]).append("]");
        }
        return builder.toString();
    }

    public static void println(int[] array) {
        System.out.println(Arrays.toString(array));
    }

    public static void println(long[] array) {
        System.out.println(Arrays.toString(array));
    }

    public static void println(String[] array) {
        System.out.println(Arrays.toString(array));
    }

    /**
     * To make a "subtraction" between two collections and get the indices of all the elements that are in the minuend
     * but not in the subtrahend. The indices returned are those in the minuend.
     *
     * @param minuend             e.g. {"a", "b", "c", "d"}
     * @param subtrahendIndexList e.g. {1, 3}
     * @return list of the indexes of the different items, e.g. {0, 2}
     */
    public static List<Integer> diffIndexes(Object[] minuend, List<Integer> subtrahendIndexList) {
        List<Integer> diffIndexList = new ArrayList<>();
        for (int i = 0; i < minuend.length; i++) {
            if (subtrahendIndexList.contains(i))
                continue;
            diffIndexList.add(i);
        }
        return diffIndexList;
    }

    /**
     * To make a "subtraction" between two collections and get all the elements that are in the minuend
     * but not in the subtrahend.
     *
     * @param minuend             e.g. {"a", "b", "c", "d"}
     * @param subtrahendIndexList e.g. {1, 3}
     * @return list of the different items, e.g. {"a", "c"}
     */
    public static <T> List<T> diffItems(T[] minuend, List<Integer> subtrahendIndexList) {
        List<T> diffList = new ArrayList<>();
        for (int i = 0; i < minuend.length; i++) {
            if (subtrahendIndexList.contains(i))
                continue;
            diffList.add(minuend[i]);
        }
        return diffList;
    }

    /**
     * Given two lists return items only present in the first list, items present in both lists, and items only present
     * in the second list. The equality of the items is customizable. Duplicate items in a given list are treated as one
     * that would be put in its corresponding list. The original lists are kept unchanged. Thinking of the process as
     * a Venn diagram partition gives name to this method.
     *
     * @param leftList   the first list
     * @param rightList  the second list
     * @param comparator comparing rules. It can be null if the items of the lists are {@link Comparable}.
     * @return the 3 lists in the order described above
     */
    public static <T> List<List<T>> venn(List<T> leftList, List<T> rightList, Comparator<T> comparator) {
        if (leftList == null || leftList.isEmpty()) {
            return new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>(), rightList == null ? new ArrayList<>() : rightList));
        }

        if (rightList == null || rightList.isEmpty()) {
            return new ArrayList<>(Arrays.asList(leftList, new ArrayList<>(), new ArrayList<>()));
        }

        Comparator<T> finalComp = comparator == null ? Comparator.comparing(o -> ((Comparable) o)) : comparator;
        List<T> leftItems = deduplicate(leftList);
        List<T> rightItems = deduplicate(rightList);
        List<T> commonItems = new ArrayList<>();
        for (int i = 0; i < leftItems.size(); i++) {
            T leftItem = leftItems.get(i);
            for (int j = 0; j < rightItems.size(); j++) {
                T rightItem = rightItems.get(j);
                if (finalComp.compare(leftItem, rightItem) == 0) {
                    commonItems.add(rightItem);
                    leftItems.remove(leftItem);
                    rightItems.remove(rightItem);
                    i--;
                    break;
                }
            }
        }
        return new ArrayList<>(Arrays.asList(leftItems, commonItems, rightItems));
    }

    /**
     * A convenient method for {@link #merge2(int[], int[], int[])}
     */
    public static int[] merge(int[] arr1, int[] arr2) {
        int[] arr = new int[arr1.length + arr2.length];
        merge2(arr1, arr2, arr);
        return arr;
    }

    /**
     * Combines two arrays of integer into one array by comparing the first elements of each array. Pick up the smaller integer
     * and put it into the first position of the resulting array. Now the second integer of the picked array becomes the first.
     * Repeat the picking until all integers of the two arrays are put into the resulting array.
     *
     * @param arr1 the first array of integers
     * @param arr2 the second array of integers
     * @param arr  the resulting array
     */
    public static void merge2(int[] arr1, int[] arr2, int[] arr) {
        int i = 0, j = 0, k = 0;
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
     * Imagine you have 5 arrays of cards and on each card there is an integer written. Now align the 5 arrays of cards vertically
     * on the edge of a table. Then find the smallest card among the first cards of each array and pick it up and put it on the left
     * side of another table. Push the array where the card is picked forward to the edge of the table. Again, pick up the smallest
     * card in the first row and put it next to the previously picked card. Repeat the picking until all cards of all arrays are
     * placed on the other table, and the final array of cards is the result of merging the given arrays of cards.
     *
     * @param arrs the given arrays of integers to merge
     * @return one array by combining the given arrays. The original arrays are intact after this method.
     */
    public static int[] merge(int[]... arrs) {
        int len = 0;
        for (int[] a : arrs) {
            len += a.length;
        }
        int[] arr = new int[len];
        int[] indices = new int[arrs.length];
        int k = 0;
        while (true) {
            int minIdx = 0;
            int completed = 0;
            for (int i = 0; i < arrs.length; i++) {
                if (indices[i] == arrs[i].length) {
                    completed++;
                    if (completed == i + 1)
                        minIdx++;
                    continue;
                }
                if (arrs[i][indices[i]] < arrs[minIdx][indices[minIdx]])
                    minIdx = i;
            }

            if (completed == arrs.length)
                break;

            arr[k++] = arrs[minIdx][indices[minIdx]];
            indices[minIdx]++;
        }
        return arr;
    }

    /**
     * Removes duplicate items from a string list separated by comma.
     *
     * @param str the string list
     * @return a string list without duplicate items
     */
    public static String deduplicate(String str) {
        List<String> list = deduplicate(Arrays.asList(str.split(",")));
        return toStringList(list);
    }

    /**
     * Removes duplicate items from a list. You can also use Set to remove duplicates, for example, {@code return new HashSet<>(list)}
     *
     * @param list the list that may contain duplicate items
     * @return a new list that contains no duplicates from the original list
     */
    public static <T> List<T> deduplicate(List<T> list) {
        List<T> list1 = new ArrayList<>();
        for (T item : list) {
            if (!list1.contains(item)) {
                list1.add(item);
            }
        }
        return list1;
    }

    /**
     * Try to make a list of elements "continuous" by filling it with the logically vacant elements. The continuity of the elements is
     * decided by their type, as shown below:
     *
     * <ul>
     *     <li>[ ] If a list of integers is given then natural ordering is used to fill the gaps. For example, [1,3,6] will become [1,2,3,4,5,6].</li>
     *     <li>[ ] If a list of chars is given then the encoding system (such as UTF-8) ordering is used. For example, [a,d] will become [a,b,c,d].</li>
     *     <li>[ ] If a list of strings is given then logic deduction is used. For example, [ab, bc] will become [ab, ac, ad, ..., ba, bb, bc].</li>
     *     <li>[ ] If a list of dates is given then it will be filled to be continuous on days, years, etc. For example, [2020-07-17, 2020-07-19] will become [2020-07-17, 2020-07-18, 2020-07-19].</li>
     *     <li>[X] If a list of user-defined type of objects is given then the continuity of such objects should be specified also by the user.</li>
     * </ul>
     *
     * @param list  the list to be filled in
     * @param range the number of elements in the resulted list. If it's positive then the list starts with the element provided
     *              on the leftmost by order and ends with the element on the rightmost of the range by continuity, and if it's
     *              negative the list starts with the element on the leftmost of the range by continuity and ends with the element
     *              provided on the rightmost by order. Thus if the range is not enough to contain all of the provided elements some
     *              of them will be excluded from the result list. For example, given [0,2] and range 4 the result is [0,1,2,3], and
     *              if range is -4 the result is [-1,0,1,2]. If range is 0 an empty list is returned.
     * @return the filled list. The original list remains intact.
     */
    public static List<Pair<Date, Integer>> fillGaps(List<Pair<Date, Integer>> list, int range) {
        if (range == 0 || CollectionUtils.isEmpty(list))
            return new ArrayList<>();

        List<Pair<Date, Integer>> resultList = new ArrayList<>();
        list.sort(Comparator.comparing(Pair::getKey));
        Calendar cal = Calendar.getInstance();
        cal.setTime((range > 0 ? list.get(0) : list.get(list.size() - 1)).getKey());
        if (range < 0) {
            cal.add(Calendar.DATE, 1 + range);
        }
        int j = 0;
        int n = Math.abs(range);
        for (int i = 0; i < n; i++) {
            if (j == list.size() || !DateUtils.truncatedEquals(cal.getTime(), list.get(j).getKey(), Calendar.DATE)) {
                resultList.add(new Pair<>(cal.getTime(), 0));

                if (j < list.size() && DateUtils.truncatedCompareTo(cal.getTime(), list.get(j).getKey(), Calendar.DATE) > 0) {
                    j++;
                }

            } else {
                resultList.add(new Pair<>(list.get(j)));
                j++;
            }
            cal.add(Calendar.DATE, 1);
        }
        return resultList;
    }

}
