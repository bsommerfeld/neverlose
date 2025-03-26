package de.sommerfeld.topspin.plan.collection;

import java.util.List;

public interface CollectionObject<T> {
    void add(T object);
    boolean remove(T object);
    List<T> getAll();
    boolean contains(T object);
    boolean isEmpty();
}
