package com.bytedance.scene.launchmode;

import androidx.annotation.NonNull;

import com.bytedance.scene.Scene;

/**
 * Created by jiangqi on 2024/5/20
 *
 * @author jiangqi@bytedance.com
 */
public class LaunchModeOption {
    private final LaunchMode mLaunchMode;
    private final TargetSceneFinder mTargetSceneFinder;

    public LaunchModeOption(LaunchMode launchMode, TargetSceneFinder targetSceneFinder) {
        this.mLaunchMode = launchMode;
        this.mTargetSceneFinder = targetSceneFinder;
    }

    //beforeOnStart

    public void beforeOnStart(@NonNull Scene scene) {

    }
}

