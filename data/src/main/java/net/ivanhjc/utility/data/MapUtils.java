package net.ivanhjc.utility.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Ivan Huang on 2018/4/2 16:57
 */
public class MapUtils {

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getKey));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static Map<String, Object> fillIfEmpty(Map<String, Object> map, int lastDays) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1 - lastDays);
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < lastDays; i++) {
            result.put(format.format(cal.getTime()), 0);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        for (Map.Entry entry : map.entrySet()) {
            result.put((String) entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Formats a map to a 2-column table in which the keys are put in the first column and values in the second.
     *
     * @param map     the map to format
     * @param keyName the type name of the keys
     * @param valName key type name of the values
     * @param <K>     key type
     * @param <V>     value type
     * @return the formatted string
     */
    public static <K, V> String getTable(Map<K, V> map, String keyName, String valName) {
        String pattern = "%-20s%-20s%n";
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(pattern, keyName, valName));
        for (Map.Entry entry : map.entrySet()) {
            builder.append(String.format(pattern, entry.getKey(), entry.getValue()));
        }
        builder.append("total number of ").append(keyName).append("s: ").append(map.entrySet().size());
        return builder.toString();
    }

    /**
     * Converts a JavaBean object to map
     */
    public static Map<String, Object> toMapFromBean(Object bean) throws IllegalAccessException {
        Map<String, Object> result = new HashMap<>();
        Class cls = bean.getClass();
        while (cls != Object.class) {
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                result.put(field.getName(), field.get(bean));
            }
            cls = cls.getSuperclass();
        }
        return result;
    }

    /**
     * Converts a JSON string a map
     */
    public static Map<String, String> toMapFromJSON(String json) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        Map<String, String> map = new HashMap<>();
        jsonObject.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue().getAsString()));
        return map;
    }

    public static Map<String, Object> createMap(Object... objects) {
        Map<String, Object> map = new HashMap<>();
        if (objects == null || objects.length == 0 || objects.length % 2 != 0) {
            return map;
        }
        for (int i = 0; i < objects.length; i += 2) {
            map.put(String.valueOf(objects[i]), objects[i + 1]);
        }
        return map;
    }
}
