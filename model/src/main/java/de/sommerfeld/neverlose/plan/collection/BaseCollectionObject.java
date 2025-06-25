package de.sommerfeld.neverlose.plan.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class BaseCollectionObject<T> implements CollectionObject<T> {

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
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(T object) {
        return this.list.contains(object);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseCollectionObject<?> that = (BaseCollectionObject<?>) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }
}
