package com.bytedance.scene.launchmode;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

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
            public boolean isTargetScene(@NonNull Scene scene, @Nullable Bundle arguments) {
                return scene.getClass() == targetClass;
            }
        };
    }

    public SingleTopLaunchModeBehavior(@NonNull TargetSceneFinder targetSceneFinder) {
        this.mTargetSceneFinder = targetSceneFinder;
    }

    @Override
    public boolean onInterceptPushOperation(@NonNull List<Pair<Scene,Bundle>> sceneList) {
        if (sceneList.size() <= 0) {
            return false;
        }
        Pair<Scene, Bundle> pair = sceneList.get(sceneList.size() - 1);
        return this.mTargetSceneFinder.isTargetScene(pair.first, pair.second);
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
