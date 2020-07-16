package net.ivanhjc.utility.data;

import com.google.gson.GsonBuilder;
import net.ivanhjc.utility.data.model.User;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.*;

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
}
