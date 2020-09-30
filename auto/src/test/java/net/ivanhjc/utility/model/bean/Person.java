package net.ivanhjc.utility.model.bean;

/**
 * @author Ivan Huang on 2018/5/25 11:10.
 */
public class Person {
    private Integer id;
    private String name;
    private Person child;

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

    public Person getChild() {
        return child;
    }

    public void setChild(Person child) {
        this.child = child;
    }
}
