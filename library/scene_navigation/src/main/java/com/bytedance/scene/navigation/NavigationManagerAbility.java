package com.bytedance.scene.navigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.interfaces.Function;

import java.util.List;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface NavigationManagerAbility {
    NavigationScene getNavigationScene();

    NavigationListener getNavigationListener();

    Record getCurrentRecord();

    Scene getCurrentScene();

    boolean containsRecord(Record record);

    List<Record> getCurrentRecordList();

    List<Pair<Scene, Bundle>> getCurrentSceneAndArgumentsList();

    List<Scene> getCurrentSceneList();

    void pushRecord(Record record);

    void removeRecord(Record record);

    void cancelCurrentRunningAnimation();

    boolean canExecuteNavigationStackOperation();

    String beginSuppressStackOperation(String tag);

    void endSuppressStackOperation(String tag);

    void executeOperationSafely(final Operation operation, final Runnable operationEndAction);

    boolean isDisableNavigationAnimation();

    boolean isOnlyRestoreVisibleScene();

    void restoreActivityStatus(@Nullable ActivityStatusRecord activityStatusRecord);

    void restoreActivityStatusBarNavigationBarStatus(@Nullable ActivityStatusRecord activityStatusRecord);

    NavigationSceneManager.CancellationSignalManager getCancellationSignalManager();

    void notifySceneStateChanged();

    void moveState(
            @NonNull NavigationScene navigationScene,
            @NonNull Scene scene, @NonNull State to,
            @Nullable Bundle bundle,
            boolean causedByActivityLifeCycle,
            @Nullable Runnable endAction
    );

    void moveState(
            @NonNull NavigationScene navigationScene,
            @NonNull Scene scene, @NonNull State to,
            @Nullable Bundle bundle,
            boolean causedByActivityLifeCycle,
            @Nullable Function<Scene, Void> preSceneOnStartAction,
            @Nullable Runnable endAction
    );

    boolean dispatchOnConfigurationChangedToRecord(Record record, Scene scene);

    void notifyNavigationAnimationEnd(@Nullable Scene from, @NonNull Scene to, boolean isPush);

    /**
     * Destroy a scene by record
     *
     * @param record The record containing the scene to destroy
     * @param currentRecord The current active record
     */
    void destroyByRecord(Record record, Record currentRecord);
}