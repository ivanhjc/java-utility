package net.ivanhjc.utility.data.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Car {
    public Long id;
    public String license;
    public Integer type;
    public List<Component> components;
    public Map<String, Object> owners;
    public Date purchaseDate;
    public List list;
    public Map map;

    public static class Component {
        public Long id;
        public String name;
    }
}
