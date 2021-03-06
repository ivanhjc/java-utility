package net.ivanhjc.utility.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Ivan Huang on 2018/4/2 16:57
 */
public class MapUtils extends org.apache.commons.collections.MapUtils {

    /**
     * Create a HashMap in one line.
     *
     * @param objects even number of objects, arranged as "key, value, key, value, ...". Non-conformed format would result in an empty map.
     * @return the created HashMap
     */
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

    /**
     * Sort a map by value.
     *
     * @param map the map to sort
     * @param <K> key type
     * @param <V> value type
     * @return a new sorted map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getValue));
        Map<K, V> result = new LinkedHashMap<>();
        list.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    /**
     * Sort a map by key.
     *
     * @param map the map to sort
     * @param <K> key type
     * @param <V> value type
     * @return a new sorted map
     */
    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getKey));
        Map<K, V> result = new LinkedHashMap<>();
        list.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    /**
     * @param map
     * @param lastDays
     * @return
     */
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
     * Check if all the keys provided in a comma-separated string list have a non-null value in a map.
     *
     * @param keys comma-separated string list
     * @param map  the map to look up
     */
    public static boolean notNull(String keys, Map map) {
        for (String key : keys.split(",")) {
            if (map.get(key) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return keys provided in a comma-separated string list that point to null in a map.
     *
     * @param keys comma-separated string list
     * @param map  the map to look up
     */
    public static String getNullKeys(String keys, Map map) {
        StringBuilder builder = new StringBuilder();
        for (String key : keys.split(",")) {
            if (map.get(key) == null) {
                builder.append(key).append(",");
            }
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * Extract all entries containing the keys specified into a new map.
     *
     * @param map the map to extract from
     * @param keys the keys to extract by
     */
    public static <K, V> Map<K, V> extract(Map<K, V> map, K[] keys) {
        Map<K, V> newMap = new HashMap<>();
        if (map == null || map.size() == 0)
            return newMap;

        for (K key : keys) {
            newMap.put(key, map.get(key));
        }
        return newMap;
    }

    /**
     * Extract all entries containing the keys specified into a new map.
     *
     * @param list the list of maps to extract from
     * @param keys the keys to extract by
     */
    public static <K, V> List<Map<K, V>> extract(List<Map<K, V>> list, K[] keys) {
        List<Map<K, V>> newList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list))
            return newList;

        list.forEach(map -> newList.add(extract(map, keys)));
        return newList;
    }
}
