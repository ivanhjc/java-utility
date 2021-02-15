package net.ivanhjc.utility.model.bean;

import java.util.Optional;

/**
 * @author Ivan Huang on 2018/5/25 11:10.
 */
public class Person {
    private Integer id;
    private String name;
    private Person child;
    private String firstName;
    private String lastName;
    private Integer age;

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

    public static int compareFirstNames(Person p1, Person p2) {
        return p1.getName().compareTo(p2.getName());
    }

    public int compare(Person person) {
        return this.getName().compareTo(person.getName());
    }

    public boolean isSamePerson(Person other) {
        return isSamePerson(this, other);
    }

    public static boolean isSamePerson(Person p1, Person p2) {
        return p1.getId().equals(p2.getId());
    }

    public String toString() {
        return "[id=" + id + ", name=" + name + "]";
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
