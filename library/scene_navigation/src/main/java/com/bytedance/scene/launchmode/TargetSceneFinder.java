package com.bytedance.scene.launchmode;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;

/**
 * Created by jiangqi on 2024/5/20
 *
 * @author jiangqi@bytedance.com
 */
public interface TargetSceneFinder {
    boolean isTargetScene(@NonNull Scene scene, @Nullable Bundle arguments);
}
