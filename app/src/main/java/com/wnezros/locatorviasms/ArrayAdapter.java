package com.wnezros.locatorviasms;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayAdapter<T> extends android.widget.ArrayAdapter<T> {
    private final List<T> _objects;

    public ArrayAdapter(Context context, @LayoutRes int resource) {
        this(context, resource, 0, new ArrayList<T>());
    }

    public ArrayAdapter(Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        this(context, resource, textViewResourceId, new ArrayList<T>());
    }
    public ArrayAdapter(Context context, @LayoutRes int resource, @NonNull T[] objects) {
        this(context, resource, 0, Arrays.asList(objects));
    }

    public ArrayAdapter(Context context, @LayoutRes int resource, @IdRes int textViewResourceId,
                        @NonNull T[] objects) {
        this(context, resource, textViewResourceId, Arrays.asList(objects));
    }

    public ArrayAdapter(Context context, @LayoutRes int resource, @NonNull List<T> objects) {
        this(context, resource, 0, objects);
    }

    public ArrayAdapter(Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<T> objects) {
        super(context, resource, textViewResourceId, objects);
        _objects = objects;
    }

    public void removeAt(int index) {
        _objects.remove(index);
        notifyDataSetChanged();
    }

    public T[] toArray(T[] array) {
        return _objects.toArray(array);
    }
}