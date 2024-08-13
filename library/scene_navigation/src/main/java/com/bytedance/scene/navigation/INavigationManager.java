package com.bytedance.scene.navigation;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/4
 *
 * @author jiangqi@bytedance.com
 */
public interface INavigationManager {

    public void saveToBundle(Bundle bundle);

    public void restoreFromBundle(Context context, Bundle bundle, SceneComponentFactory rootSceneComponentFactory, State targetState);

    public String getStackHistory();

    @NonNull
    public String beginSuppressStackOperation(@NonNull String tagPrefix);

    public void endSuppressStackOperation(@NonNull String suppressTag);

    public void dispatchCurrentChildState(State state);

    public void dispatchChildrenState(State state, boolean reverseOrder, boolean causeByActivityLifecycle);

    public void setResult(Scene scene, Object result);

    public void remove(@NonNull Scene scene);

    public void pop();

    public void pop(PopOptions popOptions);

    public void popTo(Class<? extends Scene> clazz, NavigationAnimationExecutor animationFactory);

    public void popToRoot(NavigationAnimationExecutor animationFactory);

    public void pushRoot(@NonNull final Scene scene);

    public void push(@NonNull final Scene scene, @NonNull PushOptions pushOptions);

    public void recreate(@NonNull Scene scene);

    public void changeTranslucent(@NonNull final Scene scene, boolean translucent);

    public void executePendingOperation();

    public boolean canPop();

    public Scene getCurrentScene();

    public List<Scene> getCurrentSceneList();

    public Record findRecordByScene(Scene scene);

    public Record getCurrentRecord();

    public void addOnBackPressedListener(@NonNull LifecycleOwner lifecycleOwner, @NonNull OnBackPressedListener onBackPressedListener);

    public void removeOnBackPressedListener(@NonNull OnBackPressedListener onBackPressedListener);

    public boolean interceptOnBackPressed();

    public void cancelCurrentRunningAnimation();

    public boolean pop(InteractionNavigationPopAnimationFactory animationFactory);

    public boolean isInteractionNavigationPopSupport(InteractionNavigationPopAnimationFactory animationFactory);

    public void forceExecutePendingNavigationOperation();

    public void recycleInvisibleScenes();
}
