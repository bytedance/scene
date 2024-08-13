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
 * <p>
 * If you use SingleTaskLaunchModeBehavior, the Scene instance by yourself maybe be dropped
 */
public final class SingleTaskLaunchModeBehavior implements LaunchModeBehavior {
    private final TargetSceneFinder mTargetSceneFinder;
    private int mRemoveSceneCount = 0;

    public SingleTaskLaunchModeBehavior(@NonNull final Class targetClass) {
        this.mTargetSceneFinder = new TargetSceneFinder() {
            @Override
            public boolean isTargetScene(@NonNull Scene scene) {
                return scene.getClass() == targetClass;
            }
        };
    }

    public SingleTaskLaunchModeBehavior(@NonNull TargetSceneFinder targetSceneFinder) {
        this.mTargetSceneFinder = targetSceneFinder;
    }

    @Override
    public boolean onInterceptPushOperation(@NonNull List<Scene> sceneList) {
        if (sceneList.size() <= 0) {
            return false;
        }

        int targetIndex = -1;

        for (int i = sceneList.size() - 1; i >= 0; i--) {
            Scene scene = sceneList.get(i);
            if (this.mTargetSceneFinder.isTargetScene(scene)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex != -1) {
            this.mRemoveSceneCount = sceneList.size() - targetIndex - 1;
        }

        return targetIndex != -1;
    }

    @Override
    public int getPopSceneCount() {
        return this.mRemoveSceneCount;
    }

    @Override
    public void sceneOnNewIntent(@NonNull Scene scene, @Nullable Bundle arguments) {
        if (scene instanceof ActivityCompatibleBehavior) {
            ((ActivityCompatibleBehavior) scene).onNewIntent(arguments);
        }
    }
}
