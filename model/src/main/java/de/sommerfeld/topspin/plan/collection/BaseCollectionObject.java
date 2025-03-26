package de.sommerfeld.topspin.plan.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseCollectionObject<T> implements CollectionObject<T> {

    private final List<T> list;

    protected BaseCollectionObject() {
        this.list = new ArrayList<>();
    }

    @Override
    public void add(T object) {
        this.list.add(object);
    }

    @Override
    public List<T> getAll() {
        return Collections.unmodifiableList(list);
    }
}
