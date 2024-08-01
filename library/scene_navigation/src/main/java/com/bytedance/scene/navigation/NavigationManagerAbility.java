package com.bytedance.scene.navigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

import java.util.List;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface NavigationManagerAbility {
    NavigationScene getNavigationScene();

    NavigationListener getNavigationListener();

    Record getCurrentRecord();

    boolean containsRecord(Record record);

    List<Record> getCurrentRecordList();

    void pushRecord(Record record);

    void removeRecord(Record record);

    void cancelCurrentRunningAnimation();

    boolean canExecuteNavigationStackOperation();

    String beginSuppressStackOperation(String tag);

    void endSuppressStackOperation(String tag);

    void executeOperationSafely(final Operation operation, final Runnable operationEndAction);

    boolean isDisableNavigationAnimation();

    boolean isOnlyRestoreVisibleScene();

    void restoreActivityStatus(ActivityStatusRecord activityStatusRecord);

    NavigationSceneManager.CancellationSignalManager getCancellationSignalManager();

    void notifySceneStateChanged();
}