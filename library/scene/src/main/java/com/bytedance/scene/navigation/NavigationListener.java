package com.bytedance.scene.navigation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/1/18.
 */
public interface NavigationListener {
    void navigationChange(@Nullable Scene from, @NonNull Scene to, boolean isPush);
}
