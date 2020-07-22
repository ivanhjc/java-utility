package net.ivanhjc.utility.data;

import java.lang.reflect.Field;

public class BeanUtils extends org.apache.commons.beanutils.BeanUtils {

    /**
     * Find properties specified in a string that are null in the target bean object.
     *
     * @param fieldsToCheck written using the simplest form that tells the properties. For primitive data types (Integer, String, Date, etc.) those
     *                      properties are simply a comma-separated list. For nested object types the form "person[age,name]" is used. For example,
     *                      "id,name,type,Monitor[id,name,age]" may be the fields for checking for a SchoolClass object. Fields not present in the object
     *                      are deemed null as well.
     * @param obj
     */
    public static StringBuilder getNullProperties(String fieldsToCheck, Object obj) {
        if (StringUtils.isBlank(fieldsToCheck)) {
            return new StringBuilder();
        }

        StringUtils.split(fieldsToCheck, ",", SplitRegex.DROPPED);
        StringBuilder builder = new StringBuilder();
        Class cls = obj.getClass();
        for (String str : fieldsToCheck.split(",")) {
            boolean isObject = str.contains("[");
            String fieldName = isObject ? StringUtils.substringBefore(str, "[") : str;
            Field field;
            Object value;
            try {
                field = cls.getDeclaredField(fieldName);
                field.setAccessible(true);
                value = field.get(obj);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                builder.append(fieldName).append(",");
                continue;
            }

            if (value == null) {
                builder.append(fieldName).append(", ");
            } else if (isObject) {
                String children = StringUtils.substringBetween(str, "[", "]");
                StringBuilder builder1 = getNullProperties(children, value);
                if (builder1.length() > 0) {
                    builder.append(fieldName).append("[").append(builder1).append("], ");
                }
            }
        }

        return builder;
    }
}
