package com.bytedance.scene.utlity;

import android.support.annotation.NonNull;

public class NonNullPair<F, S> {
    @NonNull
    public final F first;
    @NonNull
    public final S second;

    private NonNullPair(@NonNull F first, @NonNull S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NonNullPair)) {
            return false;
        }
        NonNullPair<?, ?> p = (NonNullPair<?, ?>) o;
        return objectsEqual(p.first, first) && objectsEqual(p.second, second);
    }

    private static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    @Override
    public String toString() {
        return "Pair{" + String.valueOf(first) + " " + String.valueOf(second) + "}";
    }

    @NonNull
    public static <A, B> NonNullPair<A, B> create(@NonNull A a, @NonNull B b) {
        return new NonNullPair<A, B>(a, b);
    }
}
