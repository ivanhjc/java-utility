package net.ivanhjc.utility.data;

import net.ivanhjc.utility.data.model.Person;
import org.junit.Test;

import java.util.*;

/**
 * @author Ivan Huang on 2018/2/28
 */
public class ListUtilsTest {

    private String format = "%-30s%-10s";
    private String pointer = "->";

    @Test
    public void remove() {
        String[] testArray1 = {"a", "b", "c", "d", "e", "f"};
        int[] indices = {1, 3, 5};

        List<String> list1 = new ArrayList<>(Arrays.asList(testArray1));
        System.out.printf(format, list1, pointer);
        ListUtils.remove(list1, indices);
        System.out.println(list1);

        List<String> list2 = new LinkedList<>(Arrays.asList(testArray1));
        System.out.printf(format, list2, pointer);
        ListUtils.remove(list2, indices);
        System.out.println(list2);
    }

    @Test
    public void reverse() {
        int[] testArray2 = {1, 2, 3, 4, 5, 6};
        System.out.printf(format, Arrays.toString(testArray2), pointer);
        ListUtils.reverse(testArray2);
        System.out.println(Arrays.toString(testArray2));
    }

    @Test
    public void splitAndTrim() {
        ListUtils.println(ListUtils.splitAndTrim(",1,2,3,,3,4, 5,  6, 7 ,  "));
        ListUtils.println(ListUtils.splitAndTrim("   a ,,,, b ,c, d,,, , ,    ,3,4, 5,  6, 7 ,  "));
    }

    @Test
    public void toStringTest() {
        int[] testArray = {1, 2, 3, 4, 5, 6};
        System.out.println(ListUtils.toString(testArray));
        System.out.println(Arrays.toString(testArray));
    }

    @Test
    public void parseInt() {
        ListUtils.println(ListUtils.parseInt(new String[]{"1", "2", "3", "4"}));
        ListUtils.println(ListUtils.parseInt("1,2,3,4,5"));
        ListUtils.println(ListUtils.parseInt(",1,2,3,,3,4, 5,  6, 7 ,  "));
    }

    @Test
    public void parseLong() {
        ListUtils.println(ListUtils.parseLong(new String[]{"1", "2", "3", "4"}));
        ListUtils.println(ListUtils.parseLong("1,2,3,4,5"));
        ListUtils.println(ListUtils.parseLong(",1,2,3,,3,4, 5,  6, 7 ,  "));
    }

    @Test
    public void parseString() {
        ListUtils.println(ListUtils.parseString(new long[]{1, 2, 3}));
    }

    @Test
    public void toStringList() {
        System.out.println(ListUtils.toStringList(Arrays.asList(1, 3, 3, 4, 5)));
    }

    @Test
    public void toIntArrayIgnoreNonInt() {
        ListUtils.println(ListUtils.toIntArrayIgnoreNonInt(new String[]{"1", "a", "b", "3"}));
        ListUtils.println(ListUtils.toIntArrayIgnoreNonInt("1,a,b,c, d,e,  f, , f, 2, 4 ,"));
    }

    @Test
    public void toArrayTest() {
        long[] array = ListUtils.parseLong("1,2,3,4");
        ListUtils.println(array);
    }

    @Test
    public void VennPartition() {
        System.out.println(ListUtils.vennPartition(Arrays.asList("a", "b", "a", "c"), Arrays.asList("a", "b", "c"), null));
        System.out.println(ListUtils.vennPartition(null, Arrays.asList("a", "b", "c"), null));
        System.out.println(ListUtils.vennPartition(
                Arrays.asList(new Person(1, "A"), new Person(2, "B")),
                Arrays.asList(new Person(1, "test"), new Person(3, "C")),
                Comparator.comparing(Person::getId)));
    }

    @Test
    public void merge() {
        int[] arr1 = new int[]{1, 3, 5, 7};
        int[] arr2 = new int[]{2, 7, 9, 10, 50};
        int[] arr3 = new int[]{-9, 0, 4, 8, 42};
        ListUtils.println(ListUtils.merge(arr1, arr2));
        ListUtils.println(ListUtils.merge(arr1, arr2, arr3));
    }

    @Test
    public void merge2() {
        int[] arr1 = new int[]{1, 3, 5, 7};
        int[] arr2 = new int[]{2, 7, 9, 10, 50};
        int[] arr4 = new int[arr1.length + arr2.length];
        ListUtils.merge2(arr1, arr2, arr4);
        ListUtils.println(arr4);
    }

    @Test
    public void removeDuplicates() {
        System.out.println(ListUtils.deduplicate("2,5,a,a,3,4,2,1,4"));
    }
}
