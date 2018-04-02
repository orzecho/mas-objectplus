package eu.mdabrowski.objectplus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import lombok.Getter;

public abstract class ObjectPlus implements Serializable {
    @Getter
    private static Map<Class, Vector> extensions = new HashMap<>();

    public ObjectPlus() {
        extensions.entrySet().stream()
                .filter(e -> e.getKey().equals(this.getClass()))
                .findFirst()
                .map(e -> e.getValue())
                .orElseGet(this::createExtensionVector)
                .add(this);
    }

    private Vector createExtensionVector() {
        Vector vector = new Vector();
        extensions.put(this.getClass(), vector);
        return vector;
    }

}