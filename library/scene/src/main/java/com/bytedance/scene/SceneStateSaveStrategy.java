package com.bytedance.scene;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by jiangqi on 2023/3/30
 *
 * @author jiangqi@bytedance.com
 *
 * The simplest implementation
 *
 * public class DefaultSceneStateSaveStrategy implements SceneStateSaveStrategy {
 *
 *     @Nullable
 *     @Override
 *     public Bundle onRestoreInstanceState(@NonNull Bundle hostSavedInstanceState) {
 *         return hostSavedInstanceState;
 *     }
 *
 *     @Override
 *     public void onSaveInstanceState(@NonNull Bundle hostOutState, @NonNull Bundle sceneOutState) {
 *         hostOutState.putAll(sceneOutState);
 *     }
 *
 *     @Override
 *     public void onClear() {
 *
 *     }
 * }
 *
 */
public interface SceneStateSaveStrategy {
    //Extract the scene data from the data saved by the host at the last onSaveInstanceState
    @Nullable
    Bundle onRestoreInstanceState(@NonNull Bundle hostSavedInstanceState);

    //Store the scene's data in the host's data so that it can be rebuilt the next time it is restored via onRestoreInstanceState
    void onSaveInstanceState(@NonNull Bundle hostOutState, @NonNull Bundle sceneOutState);

    //The scene is normally destroyed and no longer used. The previous data can be cleaned up
    void onClear();
}