package eu.mdabrowski.objectplus;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Person extends ObjectPlus {
    private String name;

    public Person(String name) {
        super();
        this.name = name;
    }
}
