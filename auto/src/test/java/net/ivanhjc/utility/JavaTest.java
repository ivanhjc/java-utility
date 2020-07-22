package net.ivanhjc.utility;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.data.MapUtils;
import net.ivanhjc.utility.model.bean.Person;
import net.ivanhjc.utility.model.enums.RespCode;
import net.ivanhjc.utility.model.enums.Season;
import org.apache.commons.math3.util.MathUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ivan Huang on 2018/3/6
 */
public class JavaTest {

    @Test
    public void nullTest() {

        /*
        null instanceof Object        ->   false
        (String) null                 ->   null
        String.valueOf(null)          ->   "null"
        Integer.equals(null)          ->   false
        Integer.valueOf(null)         ->   java.lang.NumberFormatException: null
        "1101".equals(null)           ->   false
        */

        String f = "%-30s%-5s%s%n";
        System.out.printf(f, "null instanceof Object", "->", null instanceof Object);

        Map<String, Object> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", 20);

        try {
            Person a1 = (Person) map.get("k3");
            System.out.printf(f, "(String) null", "->", a1);
            /*System.out.printf(f, "(String) null", "->", a1);
            String a2 = String.valueOf(map.get("k3"));
            System.out.printf(f, "String.valueOf(null)", "->", "\"" + a2 + "\"");
            Integer b1 = 1;
            Integer b2 = (Integer) map.get("k3");
            System.out.printf(f, "Integer.equals(null)", "->", b1.equals(b2));
            System.out.printf(f, "Integer.valueOf()", "->", Integer.valueOf(null));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void bigDecimalTest() {
        BigDecimal a = new BigDecimal("0.00");
        System.out.println(a.equals(BigDecimal.ZERO));
        System.out.println(a.compareTo(BigDecimal.ZERO) == 0);
        Map<String, Object> map = new HashMap<>();
        map.put("num", 0.1);
        map.put("sec", 1727L);
//        BigDecimal decimal = (BigDecimal) map.get("num"); -> java.lang.ClassCastException: java.lang.Double cannot be cast to java.math.BigDecimal
        double min = new BigDecimal(((long) map.get("sec"))).divide(new BigDecimal(60.0), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
        System.out.println("sec: " + min);
        System.out.println("sec2: " + (((long) map.get("sec")) / 60.0));

        int x = 2;
        double y = Integer.valueOf(x).doubleValue();
        System.out.println(y);
    }

    @Test
    public void enumTest() {
        Season season = Season.AUTUMN;
        System.out.println(season.getDesc());

        // Test if enum member field can be altered
        RespCode code = RespCode.FAILURE;
        code.message = "hello";
        System.out.printf("code: %s, message: %s%n", code.code, code.message);


    }

    @Test
    public void testInheritance() {
        ClassB b = new ClassB();
        b.sayHello();
        b.sayHelloInChinese();
        System.out.println(b instanceof InterA);
    }

    interface InterA {
        void sayHello();
    }

    class ClassA implements InterA {
        @Override
        public void sayHello() {
            System.out.println("Hello");
        }
    }


    class ClassB extends ClassA {
        void sayHelloInChinese() {
            System.out.println("你好");
        }
    }

    @Test
    public void calendarTest() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1 - 7);
        System.out.println(cal.getTime());
    }

    @Test
    public void charTest() {
        char c = '[';
//        System.out.println(c != '[' || c != '(');
        System.out.println();
    }

    @Test
    public void splitTest() {
//        ArrayUtils.println("a".split(","));
        System.out.println(Math.round(201.9));
        System.out.println((double) 2 / 3);
        System.out.println(((double) 2) / 3);
    }

    @Test
    public void immutableMap() {
        Map<String, Object> map = new HashMap<>();
        List<String> strs = new ArrayList<>(Arrays.asList("0", "0", "1", "2", "1", "0"));
        map.put("strs", strs);
        map.put("strs", strs.stream().distinct().collect(Collectors.toList()));
        System.out.println(map.get("strs"));
    }

    @Test
    public void bloomFilter() {
        BloomFilter<Integer> filter = BloomFilter.create(Funnels.integerFunnel(), 500, 0.01);
        for (int i = 0; i < 500; i++) {
            filter.put(i);
        }

        System.out.println(filter.mightContain(200));
        System.out.println(filter.mightContain(100));
        System.out.println(filter.mightContain(300));
        System.out.println(filter.mightContain(200));
        System.out.println(filter.mightContain(0));
        System.out.println(filter.mightContain(-1));
        System.out.println(filter.mightContain(-200));
        System.out.println(filter.mightContain(600));

        IntStream.range(0, 100).forEach(filter::put);
        Hashing.murmur3_32();
    }

    @Test
    public void stream() {
        /*List<Integer> list = Arrays.asList(1, 2, 3, 4);
        list = list.stream().filter(n -> n > 2).collect(Collectors.toList());
        System.out.println(list);*/
        System.out.println(new GsonBuilder().serializeNulls().create().toJson(MapUtils.createMap("a", null, null, null, null, 3)));
    }

    @Test
    public void fooLoopTest() {
//        for (int i = 0; i < getAbs(10); i++) {
//            System.out.println(i);
//        }
        int[] n = {1, 2, 3, 4};
        for (int i : getAbs(n)) {
            System.out.println(i);
        }
    }

    private int[] getAbs(int[] n) {
        System.out.println(n);
        return n;
    }
}
