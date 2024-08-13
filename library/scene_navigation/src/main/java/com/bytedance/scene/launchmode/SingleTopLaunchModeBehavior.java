package com.bytedance.scene.launchmode;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;

import java.util.List;

/**
 * Created by jiangqi on 2024/5/20
 *
 * @author jiangqi@bytedance.com
 * If you use SingleTopLaunchModeBehavior, the Scene instance by yourself maybe be dropped
 */
public final class SingleTopLaunchModeBehavior implements LaunchModeBehavior {
    private final TargetSceneFinder mTargetSceneFinder;

    public SingleTopLaunchModeBehavior(@NonNull final Class targetClass) {
        this.mTargetSceneFinder = new TargetSceneFinder() {
            @Override
            public boolean isTargetScene(@NonNull Scene scene) {
                return scene.getClass() == targetClass;
            }
        };
    }

    public SingleTopLaunchModeBehavior(@NonNull TargetSceneFinder targetSceneFinder) {
        this.mTargetSceneFinder = targetSceneFinder;
    }

    @Override
    public boolean onInterceptPushOperation(@NonNull List<Scene> sceneList) {
        if (sceneList.size() <= 0) {
            return false;
        }
        return this.mTargetSceneFinder.isTargetScene(sceneList.get(sceneList.size() - 1));
    }

    @Override
    public int getPopSceneCount() {
        return 0;
    }

    @Override
    public void sceneOnNewIntent(@NonNull Scene scene, @Nullable Bundle arguments) {
        if (scene instanceof ActivityCompatibleBehavior) {
            ((ActivityCompatibleBehavior) scene).onNewIntent(arguments);
        }
    }
}
