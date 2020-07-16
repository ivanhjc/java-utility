package net.ivanhjc.utility.data;

import java.util.*;

/**
 * @author Ivan Huang on 2017/1/12
 */
public class ListUtils {

    /**
     * Check if a string is an element of a string representation of a comma-separated array <br>
     * ("a", "a,b,c,d") -> true
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
     * Given two lists finds items only present in the first list, items only present in the second list, and items present in both lists respectively.
     * The equality of two items is customary. Duplicate items are treated as one and kept in its corresponding list accordingly. The original lists are
     * kept unchanged. Thinking of the process as a Venn diagram gives name to this method.
     *
     * @param leftList  the first list
     * @param rightList the second list
     * @param c         comparing rules, null if the items of the lists are {@link Comparable}
     * @return the 3 lists as described above
     */
    public static <T> List<List<T>> vennPartition(List<T> leftList, List<T> rightList, Comparator<T> c) {
        if (leftList == null || leftList.isEmpty()) {
            return new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>(), rightList == null ? new ArrayList<>() : rightList));
        }

        if (rightList == null || rightList.isEmpty()) {
            return new ArrayList<>(Arrays.asList(leftList, new ArrayList<>(), new ArrayList<>()));
        }

        Comparator<T> comparator = c == null ? (o1, o2) -> ((Comparable) o1).compareTo(o2) : c;
        List<T> leftItems = deduplicate(leftList);
        List<T> rightItems = deduplicate(rightList);
        List<T> commonItems = new ArrayList<>();
        for (int i = 0; i < leftItems.size(); i++) {
            T leftItem = leftItems.get(i);
            for (int j = 0; j < rightItems.size(); j++) {
                T rightItem = rightItems.get(j);
                if (comparator.compare(leftItem, rightItem) == 0) {
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
}
