package net.ivanhjc.utility.model;

import java.lang.reflect.Field;

/**
 * @author Ivan Huang on 2018/5/25 11:10.
 */
public class Person {
    private Integer id;
    private String name;

    public Person() {
    }

    public Person(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        try {
            StringBuilder builder = new StringBuilder(this.getClass().getSimpleName()).append("[");
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                builder.append(field.getName()).append(":").append(field.get(this)).append(",");
            }
            builder.deleteCharAt(builder.length() - 1).append("]");
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
