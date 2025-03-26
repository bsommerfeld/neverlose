package de.sommerfeld.topspin.plan.collection;

import java.util.List;

public interface CollectionObject<T> {
    void add(T object);
    boolean remove(T object); // Add remove capability needed by ViewModel
    List<T> getAll(); // Get unmodifiable list
    List<T> getModifiableList(); // Get modifiable list (use carefully)
    // boolean contains(T object); // Might be useful for listeners
}
