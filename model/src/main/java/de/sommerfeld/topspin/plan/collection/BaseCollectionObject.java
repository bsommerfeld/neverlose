package de.sommerfeld.topspin.plan.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class BaseCollectionObject<T> implements CollectionObject<T> {
    // Changed to protected to allow potential subclass access if needed,
    // but primarily accessed via getModifiableList now.
    protected final List<T> list;

    protected BaseCollectionObject() {
        this.list = new ArrayList<>();
    }

    @Override
    public void add(T object) {
        if (object != null) {
            this.list.add(object);
        }
    }

    @Override
    public boolean remove(T object) {
        return this.list.remove(object);
    }

    @Override
    public List<T> getAll() {
        // Return an unmodifiable view for safe external reading
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<T> getModifiableList() {
        // Provide access to the modifiable list for internal manipulation
        // or careful external use (like syncing listeners). Use with caution.
        return list;
    }

    // Optional: Contains check might be useful
    public boolean contains(T object) {
        return this.list.contains(object);
    }

    // Need equals/hashCode if collections themselves are compared
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseCollectionObject<?> that = (BaseCollectionObject<?>) o;
        // Compare based on the content of the list
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        // Hash based on the content of the list
        return Objects.hash(list);
    }
}
