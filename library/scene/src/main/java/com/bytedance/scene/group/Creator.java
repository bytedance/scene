package com.bytedance.scene.group;

/**
 * Created by JiangQi on 8/8/18.
 */

public interface Creator<V> {
    V call();
}