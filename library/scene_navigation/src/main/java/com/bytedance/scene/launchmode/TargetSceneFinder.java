package com.bytedance.scene.launchmode;

import androidx.annotation.NonNull;

import com.bytedance.scene.Scene;

/**
 * Created by jiangqi on 2024/5/20
 *
 * @author jiangqi@bytedance.com
 */
public interface TargetSceneFinder {
    boolean isTargetScene(@NonNull Scene scene);
}
