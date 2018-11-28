package com.bytedance.scene.utlity;

public interface Predicate<T> {

    boolean apply(T t);
}