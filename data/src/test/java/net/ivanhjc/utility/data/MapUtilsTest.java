package net.ivanhjc.utility.data;

import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.model.User;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivan Huang on 2018/4/2 16:57
 */
public class MapUtilsTest {

    @Test
    public void sortByValue() {

    }

    @Test
    public void sortByKey() {

    }

    @Test
    public void toStringTest() {
        Map<Object, Object> map = ArrayUtils.toMap(new Object[][]{{"key1", "value1"}, {"key2", 10}});
        System.out.println(map);
    }

    @Test
    public void fillIfEmpty() {
        Map<String, Object> map = new HashMap<>();
        map.put("2018-06-13", 20);
        map.put("2018-06-15", 20);
        map = MapUtils.fillIfEmpty(map, 7);
        System.out.println(new GsonBuilder().serializeNulls().create().toJson(map));
    }

    @Test
    public void objectToMap() throws IllegalAccessException {
        User user = new User();
        user.setId(42L);
        user.setRealName("John");
        System.out.println(MapUtils.toMapFromBean(user));
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
            String win = MapUtils.selectByChance(map);
            histogram.put(win, histogram.get(win) + 1);
        }
        System.out.println(MapUtils.getTable(histogram, "Item", "Times"));
    }

    @Test
    public void containsAll() {
        System.out.println(MapUtils.notNull("b,c", MapUtils.createMap("a", null, "b", 0, "c", null)));
    }

    @Test
    public void getNullKeys() {
        System.out.println(MapUtils.getNullKeys("b,c", MapUtils.createMap("a", null, "b", 0, "c", null)));
    }
}
