package com.bytedance.scene.launchmode;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;

import java.util.List;

/**
 * Created by jiangqi on 2024/5/21
 *
 * @author jiangqi@bytedance.com
 */
public interface LaunchModeBehavior {
    boolean onInterceptPushOperation(@NonNull List<Scene> sceneList);

    int getPopSceneCount();

    void sceneOnNewIntent(@NonNull Scene scene, @Nullable Bundle arguments);
}
