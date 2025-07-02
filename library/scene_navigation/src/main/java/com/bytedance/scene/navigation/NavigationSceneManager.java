/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.navigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.util.Pair;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.SceneTrace;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.interfaces.Function;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.SceneMemoryRecyclePolicy;
import com.bytedance.scene.launchmode.LaunchModeBehavior;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.pop.CoordinatePopOptionOperation;
import com.bytedance.scene.navigation.push.CoordinatePushOptionOperation;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.queue.NavigationRunnable;
import com.bytedance.scene.navigation.reuse.IReuseScene;
import com.bytedance.scene.utlity.Action1;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignalList;
import com.bytedance.scene.utlity.ConfigurationUtility;
import com.bytedance.scene.utlity.NavigationSceneViewUtility;
import com.bytedance.scene.utlity.NonNullPair;
import com.bytedance.scene.utlity.Predicate;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.Utility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * @hide
 */
@RestrictTo(LIBRARY)
public class NavigationSceneManager implements INavigationManager, NavigationManagerAbility {
    private static final String TAG = "NavigationSceneManager";
    public static final String TRACE_EXECUTE_OPERATION_TAG = "NavigationSceneManager#executeOperation";
    private static final String TRACE_EXECUTE_PENDING_OPERATION_TAG = "NavigationSceneManager#executePendingOperation";

    private final NavigationScene mNavigationScene;
    private final RecordStack mBackStackList = new RecordStack();
    private final NavigationListener mNavigationListener;
    private AsyncHandler mThrowableHandler = null;

    /**
     * If it is paused, currently operations and subsequent operations will put into this queue.
     */
    private final ArrayDeque<Operation> mPendingActionList = new ArrayDeque<>();
    private long mLastPendingActionListItemTimestamp = -1L;

    private final CancellationSignalManager mCancellationSignalManager = new CancellationSignalManager();
    private final List<NonNullPair<LifecycleOwner, OnBackPressedListener>> mOnBackPressedListenerList = new ArrayList<>();

    private final boolean mOnlyRestoreVisibleScene;

    private final boolean mIsSeparateCreateFromCreateView;

    private boolean mAnySceneStateChanged = false;

    private NavigationMessageQueue mSceneMessageQueue = null;
    private boolean mReduceColdStartCallStack = false;

    private Configuration mActivityConfiguration = null;

    private WindowFocusChangedPendingTask mPendingWindowFocusChangedPendingTask = null;
    private boolean mRestoreStateInLifecycle = false;
    private int mConfigurationChangesAllowList = 0;
    private Scene mCurrentSyncingStateScene = null;

    NavigationSceneManager(NavigationScene scene) {
        this.mNavigationScene = scene;
        this.mNavigationListener = scene;
        this.mOnlyRestoreVisibleScene = scene.mNavigationSceneOptions.onlyRestoreVisibleScene();
        this.mIsSeparateCreateFromCreateView = scene.isSeparateCreateFromCreateView();
        this.mReduceColdStartCallStack = this.mNavigationScene.getNavigationSceneOptions().getReduceColdStartCallStack();
        if (this.mReduceColdStartCallStack) {
            mSceneMessageQueue = null;
        } else {
            mSceneMessageQueue = new NavigationMessageQueue();
        }
        this.mRestoreStateInLifecycle = this.mNavigationScene.isRestoreStateInLifecycle();
        this.mConfigurationChangesAllowList = this.mNavigationScene.getConfigurationChangesAllowList();
    }

    @NonNull
    private NavigationMessageQueue requireMessageQueue() {
        if (this.mSceneMessageQueue == null) {
            this.mSceneMessageQueue = new NavigationMessageQueue();
        }
        return this.mSceneMessageQueue;
    }

    @Nullable
    private NavigationMessageQueue getMessageQueue(){
        return this.mSceneMessageQueue;
    }

    public void saveToBundle(Bundle bundle) {
        Record currentRecord = getCurrentRecord();
        if (currentRecord != null) {
            currentRecord.saveActivityStatus();
        }

        this.mBackStackList.saveToBundle(bundle);

        ArrayList<Bundle> bundleList = new ArrayList<>();
        List<Record> recordList = this.mBackStackList.getCurrentRecordList();
        for (Record record : recordList) {
            Scene scene = record.mScene;
            if (mOnlyRestoreVisibleScene) {
                //so some Scene are not created after restore, reuse previous saved state
                if (record.mPreviousSavedState != null && scene.getState() == State.NONE) {
                    bundleList.add(record.mPreviousSavedState);
                    continue;
                }
            }
            if (scene.isSceneRestoreEnabled()) {
                Bundle sceneBundle = new Bundle();
                scene.dispatchSaveInstanceState(sceneBundle);
                bundleList.add(sceneBundle);
            } else {
                //skip because Scene disable restore
            }
        }
        bundle.putParcelableArrayList(ParcelConstants.KEY_NAVIGATION_SCENE_MANAGER_TAG, bundleList);
    }

    public void restoreFromBundle(Context context, Bundle bundle, SceneComponentFactory rootSceneComponentFactory, State targetState) {
        this.mBackStackList.restoreFromBundle(context, bundle, rootSceneComponentFactory);
        ArrayList<Bundle> bundleList = bundle.getParcelableArrayList(ParcelConstants.KEY_NAVIGATION_SCENE_MANAGER_TAG);

        List<Record> recordList = this.mBackStackList.getCurrentRecordList();

        if (this.mOnlyRestoreVisibleScene) {
            LoggerManager.getInstance().i(TAG, "restoreFromBundle restore visible Scenes");
            int nonTranslucentIndex = 0;
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (!record.mIsTranslucent) {
                    nonTranslucentIndex = i;
                    break;
                }
            }

            int restoredSceneCount = 0;
            for (int i = nonTranslucentIndex; i <= recordList.size() - 1; i++) {
                Record record = recordList.get(i);
                Bundle sceneBundle = bundleList.get(i);
                // TODO should moveState to mNavigationScene.getState() ?
                moveState(this.mNavigationScene, record.mScene, targetState, sceneBundle, false, null);

                if (i == recordList.size() - 1) {
                    restoreActivityStatus(record.mActivityStatusRecord);
                }
                restoredSceneCount++;
            }

            LoggerManager.getInstance().i(TAG, "restoreFromBundle has restored " + restoredSceneCount + " Scenes, total " + recordList.size() + " Scenes");

            for (int i = 0; i < nonTranslucentIndex; i++) {
                Record record = recordList.get(i);
                //temporarily saved previous state
                record.mPreviousSavedState = bundleList.get(i);
            }
        } else {
            LoggerManager.getInstance().i(TAG, "restoreFromBundle restore all Scenes");
            for (int i = 0; i <= recordList.size() - 1; i++) {
                Record record = recordList.get(i);
                Bundle sceneBundle = bundleList.get(i);
                // TODO should moveState to mNavigationScene.getState() ?
                moveState(this.mNavigationScene, record.mScene, targetState, sceneBundle, false, null);
            }
        }
    }

    @Override
    public void restoreChildrenSceneState(Bundle bundle, State targetState, boolean causeByActivityLifecycle) {
        ArrayList<Bundle> bundleList = bundle.getParcelableArrayList(ParcelConstants.KEY_NAVIGATION_SCENE_MANAGER_TAG);

        List<Record> recordList = this.mBackStackList.getCurrentRecordList();

        int index = 0;
        if (this.mOnlyRestoreVisibleScene) {
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (!record.mIsTranslucent) {
                    index = i;
                    break;
                }
            }
        }
        for (int i = index; i <= recordList.size() - 1; i++) {
            Record record = recordList.get(i);
            Bundle sceneBundle = bundleList.get(i);
            moveState(this.mNavigationScene, record.mScene, targetState, sceneBundle, causeByActivityLifecycle, null);
        }
    }

    public String getStackHistory() {
        return mBackStackList.getStackHistory();
    }

    /**
     * Under normal circumstances, Push/Pop is executed directly,
     * unless it is triggered in the Scene lifecycle callback,
     * then we must take the Post process.
     */
    private Set<String> mIsNavigationStateChangeInProgress = new HashSet<>();
    private int mSuppressStackOperationId = 0;
    private int mCurrentScheduledStackOperationCount = 0;

    /**
     * TODO:
     * The defect of Fragment's post() is that commit will crash after onSave(),
     * If there are no such restrictions, the post should be fine.
     * The only thing is that there may be some problems with Dialog.
     */
    private void scheduleToNextUIThreadLoop(@NonNull final Operation operation) {
        this.scheduleToNextUIThreadLoop(operation, false);
    }

    private void scheduleToNextUIThreadLoop(@NonNull final Operation operation, boolean async) {
        if (canExecuteNavigationStackOperation()) {
            /**
             * when current Handler Message is executing a NavigationScene navigation stack operation or GroupScene operation,
             * all the following navigation stack operations need to post at next Handler Message by Handler.post
             *
             * when there is a navigation stack operation waiting to be executed by Handler.post, all the following stack operations
             * must be scheduled with Handler.post too to make sure navigation order is correct
             */
            if (mIsNavigationStateChangeInProgress.size() > 0 || mCurrentScheduledStackOperationCount > 0) {
                NavigationRunnable task = new NavigationRunnable() {
                    @Override
                    public void run() {
                        mCurrentScheduledStackOperationCount--;
                        if (mIsNavigationStateChangeInProgress.size() > 0) {
                            String exceptionInfo = TextUtils.join(",", mIsNavigationStateChangeInProgress);
                            throw new SceneInternalException("miss endSuppressStackOperation(), mIsNavigationStateChangeInProgress content " + exceptionInfo);
                        }
                        if (canExecuteNavigationStackOperation()) {
                            SceneTrace.beginSection(TRACE_EXECUTE_OPERATION_TAG);
                            String suppressTag = beginSuppressStackOperation("NavigationManager execute operation by Handler.post()");
                            executeOperationSafely(operation, EMPTY_RUNNABLE);
                            endSuppressStackOperation(suppressTag);
                            mAnySceneStateChanged = true;
                            SceneTrace.endSection();
                        } else {
                            mPendingActionList.addLast(operation);
                            mLastPendingActionListItemTimestamp = System.currentTimeMillis();
                        }
                    }
                };
                mCurrentScheduledStackOperationCount++;
                LoggerManager.getInstance().i(TAG, "post " + operation + " async to message queue because previous tasks are not finished");
                requireMessageQueue().postAsync(task);
            } else {
                NavigationRunnable task = new NavigationRunnable() {
                    @Override
                    public void run() {
                        SceneTrace.beginSection(TRACE_EXECUTE_OPERATION_TAG);
                        String suppressTag = beginSuppressStackOperation("NavigationManager execute operation directly");
                        executeOperationSafely(operation, EMPTY_RUNNABLE);
                        endSuppressStackOperation(suppressTag);
                        mAnySceneStateChanged = true;
                        SceneTrace.endSection();
                    }
                };

                if (async) {
                    LoggerManager.getInstance().i(TAG, "post " + operation + " async to message queue because of async argument");
                    requireMessageQueue().postAsync(task);
                } else {
                    LoggerManager.getInstance().i(TAG, "post " + operation + " sync to message queue start");
                    requireMessageQueue().postSync(task);
                    LoggerManager.getInstance().i(TAG, "post " + operation + " sync to message queue finish");
                }
            }
        } else {
            /**
             * navigation stack operation can't be executed before NavigationScene's state is State.ACTIVITY_CREATED, otherwise
             * animation can't be execute without view
             */
            LoggerManager.getInstance().i(TAG, "add " + operation + " to pending list because of NavigationScene state is not ready");
            mPendingActionList.addLast(operation);
            mLastPendingActionListItemTimestamp = System.currentTimeMillis();
        }
    }

    @NonNull
    public String beginSuppressStackOperation(@NonNull String tagPrefix) {
        String value = tagPrefix + "_" + mSuppressStackOperationId++;
        if (!mIsNavigationStateChangeInProgress.add(value)) {
            throw new SceneInternalException("suppressTag already exists");
        }
        return value;
    }

    public void endSuppressStackOperation(@NonNull String suppressTag) {
        if (!mIsNavigationStateChangeInProgress.remove(suppressTag)) {
            throw new SceneInternalException("suppressTag not found");
        }
        if (mIsNavigationStateChangeInProgress.size() == 0) {
            //reset to zero
            mSuppressStackOperationId = 0;
        }
    }

    public void dispatchCurrentChildState(final State state) {
        if (this.mReduceColdStartCallStack && getMessageQueue() == null) {
            String suppressTag = beginSuppressStackOperation("NavigationManager dispatchCurrentChildState");
            syncCurrentSceneStateOperationInternal(state, null);
            endSuppressStackOperation(suppressTag);
            notifySceneStateChanged();
        } else {
            this.requireMessageQueue().postSync(new Runnable() {
                @Override
                public void run() {
                    String suppressTag = beginSuppressStackOperation("NavigationManager dispatchCurrentChildState");
                    executeOperationSafely(new SyncCurrentSceneStateOperation(state), EMPTY_RUNNABLE);
                    endSuppressStackOperation(suppressTag);
                    notifySceneStateChanged();
                }
            });
        }
    }

    @Override
    public void dispatchChildrenState(@NonNull final State state, @Nullable final State nextStageStateHint, final boolean reverseOrder, final boolean causeByActivityLifecycle) {
        if (this.mReduceColdStartCallStack && getMessageQueue() == null) {
            String suppressTag = beginSuppressStackOperation("NavigationManager dispatchChildrenState");
            syncAllSceneStateOperationInternal(state, nextStageStateHint, reverseOrder, causeByActivityLifecycle, null);
            endSuppressStackOperation(suppressTag);
        } else {
            this.requireMessageQueue().postSync(new Runnable() {
                @Override
                public void run() {
                    String suppressTag = beginSuppressStackOperation("NavigationManager dispatchChildrenState");
                    executeOperationSafely(new SyncAllSceneStateOperation(state, nextStageStateHint, reverseOrder, causeByActivityLifecycle), EMPTY_RUNNABLE);
                    endSuppressStackOperation(suppressTag);
                }
            });
        }
    }

    public void setResult(Scene scene, Object result) {
        Record record = mBackStackList.getRecordByScene(scene);
        if (record == null) {
            throw new IllegalArgumentException("Scene is not found in stack");
        }
        record.mPushResult = result;
    }

    private static final Runnable EMPTY_RUNNABLE = new Runnable() {
        @Override
        public void run() {

        }
    };

    public void remove(@NonNull Scene scene) {
        LoggerManager.getInstance().i(TAG, "remove " + scene);
        scheduleToNextUIThreadLoop(new RemoveOperation(scene));
    }

    public void pop() {
        LoggerManager.getInstance().i(TAG, "pop");
        scheduleToNextUIThreadLoop(new PopOperation(null));
    }

    public void pop(PopOptions popOptions) {
        LoggerManager.getInstance().i(TAG, "pop with PopOptions");
        if (popOptions.isUsePost()) {
            scheduleToNextUIThreadLoop(new CoordinatePopOptionOperation(this, requireMessageQueue(), popOptions), popOptions.isUsePostWhenPause());
        } else {
            scheduleToNextUIThreadLoop(new PopOptionOperation(popOptions));
        }
    }

    public void popTo(Class<? extends Scene> clazz, NavigationAnimationExecutor animationFactory) {
        LoggerManager.getInstance().i(TAG, "popTo " + clazz);
        scheduleToNextUIThreadLoop(new PopToOperation(clazz, animationFactory));
    }

    public void popToRoot(NavigationAnimationExecutor animationFactory) {
        LoggerManager.getInstance().i(TAG, "popToRoot");
        scheduleToNextUIThreadLoop(new PopToRootOperation(animationFactory));
    }

    @Override
    public void pushRoot(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("rootScene can't be null");
        }

        LoggerManager.getInstance().i(TAG, "pushRoot " + scene);
        if (this.mReduceColdStartCallStack && getMessageQueue() == null) {
            SceneTrace.beginSection(TRACE_EXECUTE_OPERATION_TAG);
            String suppressTag = beginSuppressStackOperation("NavigationManager execute push root operation immediately");
            pushRootOperationInternal(scene, scene instanceof SceneTranslucent, null);
            endSuppressStackOperation(suppressTag);
            SceneTrace.endSection();
        } else {
            executePushRootOperationImmediately(new PushRootOperation(scene));
        }
    }

    private void executePushRootOperationImmediately(@NonNull final Operation operation) {
        NavigationRunnable task = new NavigationRunnable() {
            @Override
            public void run() {
                SceneTrace.beginSection(TRACE_EXECUTE_OPERATION_TAG);
                String suppressTag = beginSuppressStackOperation("NavigationManager execute push root operation immediately");
                executeOperationSafely(operation, EMPTY_RUNNABLE);
                endSuppressStackOperation(suppressTag);
                SceneTrace.endSection();
            }
        };
        requireMessageQueue().postSync(task);
    }

    public void push(@NonNull final Scene scene, @NonNull PushOptions pushOptions) {
        if (scene == null) {
            throw new NullPointerException("scene can't be null");
        }
        if (pushOptions.isUsePost()) {
            LoggerManager.getInstance().i(TAG, "push " + scene.toString() + " by post");
            scheduleToNextUIThreadLoop(new CoordinatePushOptionOperation(this, requireMessageQueue(), scene, pushOptions), pushOptions.isUsePostWhenPause());
        } else {
            LoggerManager.getInstance().i(TAG, "push " + scene.toString());
            scheduleToNextUIThreadLoop(new PushOptionOperation(scene, pushOptions));
        }
    }

    @Override
    public void recreate(@NonNull Scene scene) {
        if (scene == null) {
            throw new NullPointerException("scene can't be null");
        }
        LoggerManager.getInstance().i(TAG, "recreate " + scene.toString());
        scheduleToNextUIThreadLoop(new RecreateOperation(scene));
    }

    public void changeTranslucent(@NonNull final Scene scene, boolean translucent) {
        if (scene == null) {
            throw new NullPointerException("scene can't be null");
        }
        LoggerManager.getInstance().i(TAG, "changeTranslucent " + scene.toString());
        scheduleToNextUIThreadLoop(new TranslucentOperation(scene, translucent));
    }

    private boolean mDisableNavigationAnimation = false;

    public void executePendingOperation() {
        if (this.mPendingActionList.size() == 0 || !canExecuteNavigationStackOperation()) {
            return;
        }
        LoggerManager.getInstance().i(TAG, "executePendingOperation start");

        this.requireMessageQueue().postSync(new Runnable() {
            @Override
            public void run() {
                SceneTrace.beginSection(TRACE_EXECUTE_PENDING_OPERATION_TAG);
                /*
                 * Only the last one need to do the transition animation, the previous doesn't.
                 * If not, it is easy to see that the jump animation of SchemaActivity not be executed,
                 * as SchemaActivity is usually a translucent Activity cover over the other Activity.
                 *
                 * If it is over 800ms, it will not be animated also.
                 */
                boolean animationTimeout = System.currentTimeMillis() - mLastPendingActionListItemTimestamp > 800;
                List<Operation> copy = new ArrayList<>(NavigationSceneManager.this.mPendingActionList);
                for (int i = 0; i < copy.size(); i++) {
                    Operation currentOperation = copy.get(i);
                    NavigationSceneManager.this.mDisableNavigationAnimation = animationTimeout | (i < copy.size() - 1);
                    String suppressTag = beginSuppressStackOperation("NavigationManager executePendingOperation");
                    executeOperationSafely(currentOperation, EMPTY_RUNNABLE);
                    endSuppressStackOperation(suppressTag);
                    NavigationSceneManager.this.mDisableNavigationAnimation = false;
                }
                NavigationSceneManager.this.mPendingActionList.removeAll(copy);
                if (NavigationSceneManager.this.mPendingActionList.size() > 0) {
                    throw new IllegalStateException("why mPendingActionList still have item?");
                }
                NavigationSceneManager.this.mLastPendingActionListItemTimestamp = -1L;
                NavigationSceneManager.this.mAnySceneStateChanged = true;
                SceneTrace.endSection();
            }
        });
        LoggerManager.getInstance().i(TAG, "executePendingOperation finish");
    }

    public boolean canPop() {
        return this.mBackStackList.canPop();
    }

    public void restoreActivityStatus(@Nullable ActivityStatusRecord statusRecord) {
        if (statusRecord == null) {
            return;
        }
        Activity activity = mNavigationScene.getActivity();
        statusRecord.restore(activity);
    }

    @Override
    public void restoreActivityStatusBarNavigationBarStatus(@Nullable ActivityStatusRecord activityStatusRecord) {
        if (activityStatusRecord == null) {
            return;
        }
        Activity activity = mNavigationScene.getActivity();
        activityStatusRecord.restoreStatusBarNavigationBar(activity);
    }

    @Override
    public Scene getCurrentScene() {
        Record record = mBackStackList.getCurrentRecord();
        if (record != null) {
            return record.mScene;
        } else {
            return null;
        }
    }

    @Override
    public List<Scene> getCurrentSceneList() {
        List<Record> recordList = mBackStackList.getCurrentRecordList();
        List<Scene> sceneList = new ArrayList<>();
        for (Record record : recordList) {
            sceneList.add(record.mScene);
        }
        return sceneList;
    }

    public Record findRecordByScene(Scene scene) {
        return mBackStackList.getRecordByScene(scene);
    }

    public Record getCurrentRecord() {
        return mBackStackList.getCurrentRecord();
    }

    public void addOnBackPressedListener(@NonNull LifecycleOwner lifecycleOwner, @NonNull OnBackPressedListener onBackPressedListener) {
        mOnBackPressedListenerList.add(NonNullPair.create(lifecycleOwner, onBackPressedListener));
    }

    public void removeOnBackPressedListener(@NonNull OnBackPressedListener onBackPressedListener) {
        NonNullPair<LifecycleOwner, OnBackPressedListener> target = null;
        for (int i = mOnBackPressedListenerList.size() - 1; i >= 0; i--) {
            NonNullPair<LifecycleOwner, OnBackPressedListener> pair = mOnBackPressedListenerList.get(i);
            if (pair.second == onBackPressedListener) {
                target = pair;
                break;
            }
        }
        mOnBackPressedListenerList.remove(target);
    }

    public boolean interceptOnBackPressed() {
        List<NonNullPair<LifecycleOwner, OnBackPressedListener>> copy = new ArrayList<>(mOnBackPressedListenerList);
        for (int i = copy.size() - 1; i >= 0; i--) {
            NonNullPair<LifecycleOwner, OnBackPressedListener> pair = copy.get(i);
            if (pair.first.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)
                    && pair.second.onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures a reused scene's view is properly attached to the view hierarchy
     * and calls onPrepare only when the view needs to be re-attached.
     *
     * @param navigationScene The parent NavigationScene
     * @param scene The Scene being reused
     * @param bundle The saved state bundle, if any
     */
    static void doAttachWhenReuse(@NonNull NavigationScene navigationScene, @NonNull Scene scene, @Nullable Bundle bundle) {
        View sceneView = scene.getView();
        boolean isAttached = sceneView.isAttachedToWindow();
        boolean hasParent = sceneView.getParent() != null;
        ViewGroup container = navigationScene.getSceneContainer();

        if (!isAttached && !hasParent && (container != null)) {
            //The view is removed and needs to be added back to the container
            if (bundle != null) {
                int viewIndex = NavigationSceneViewUtility.targetViewIndexOfScene(navigationScene, navigationScene.mNavigationSceneOptions, scene);
                container.addView(sceneView, viewIndex);
            } else {
                container.addView(sceneView);
            }

            if (!(scene instanceof IReuseScene)) {
                throw new SceneInternalException("This Scene should implement IReuseScene");
            }
            ((IReuseScene) scene).onPrepare(bundle);
        }
    }

    public static void moveStateNonSeparation(@NonNull NavigationScene navigationScene,
                                              @NonNull Scene scene, @NonNull State to,
                                              @Nullable Bundle bundle,
                                              boolean causedByActivityLifeCycle,
                                              @Nullable Function<Scene, Void> afterOnActivityCreatedAction,
                                              @Nullable Runnable endAction) {
        State currentState = scene.getState();
        if (currentState == to) {
            if (endAction != null) {
                endAction.run();
            }
            return;
        }

        if (currentState.value < to.value) {
            switch (currentState) {
                case NONE:
                    scene.dispatchAttachActivity(navigationScene.requireActivity());
                    scene.dispatchAttachScene(navigationScene);
                    scene.dispatchCreate(bundle);
                    ViewGroup containerView = navigationScene.getSceneContainer();
                    scene.dispatchCreateView(bundle, containerView);
                    if (ActivityCompatibleInfoCollector.isTargetSceneType(scene)) {
                        Record record = navigationScene.findRecordByScene(scene);
                        navigationScene.mNavigationSceneManager.saveActivityCompatibleInfo(record);
                    }
                    if (!causedByActivityLifeCycle) {
                        if (scene.getView().getBackground() == null) {
                            Record record = navigationScene.findRecordByScene(scene);
                            if (!record.mIsTranslucent && navigationScene.mNavigationSceneOptions.fixSceneBackground()) {
                                int resId = navigationScene.mNavigationSceneOptions.getSceneBackgroundResId();
                                if (resId > 0) {
                                    scene.getView().setBackgroundDrawable(scene.requireSceneContext().getResources().getDrawable(resId));
                                } else {
                                    scene.getView().setBackgroundDrawable(Utility.getWindowBackground(scene.requireSceneContext()));
                                }
                                record.mSceneBackgroundSet = true;
                            }
                        }
                        /*
                         * TODO: What if the NavigationScene has been destroyed at this time?
                         * TODO: What to do with serialization
                         */
                        if (bundle != null) {
                            //Scene restore from save and restore path
                            int viewIndex = NavigationSceneViewUtility.targetViewIndexOfScene(navigationScene, navigationScene.mNavigationSceneOptions, scene);
                            containerView.addView(scene.getView(), viewIndex);
                        } else {
                            containerView.addView(scene.getView());
                        }
                    }
                    scene.getView().setVisibility(View.GONE);
                    moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, afterOnActivityCreatedAction, endAction);
                    break;
                case VIEW_CREATED:
                    scene.dispatchActivityCreated(bundle);
                    if (afterOnActivityCreatedAction != null) {
                        afterOnActivityCreatedAction.apply(scene);
                    }
                    moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case ACTIVITY_CREATED:
                    scene.getView().setVisibility(View.VISIBLE);
                    if (navigationScene.isReusing(scene)) {
                        // The view may have been removed by NavigationAnimationExecutor in the reuse process,
                        // so we need to re-attach it to the view tree and prepare it for reuse if necessary.
                        doAttachWhenReuse(navigationScene, scene, bundle);
                    }
                    scene.dispatchStart();
                    moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case STARTED:
                    scene.dispatchResume();
                    ((NavigationSceneManager)navigationScene.mNavigationSceneManager).onSceneResumedWindowFocusChanged(scene);
                    moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        } else {
            switch (currentState) {
                case RESUMED:
                    scene.dispatchPause();
                    ((NavigationSceneManager)navigationScene.mNavigationSceneManager).onScenePausedWindowFocusChanged(scene);
                    moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case STARTED:
                    scene.dispatchStop();
                    if (!causedByActivityLifeCycle) {
                        scene.getView().setVisibility(View.GONE);
                    }
                    moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case ACTIVITY_CREATED:
                    if (to == State.VIEW_CREATED) {
                        throw new IllegalArgumentException("cant switch state ACTIVITY_CREATED to VIEW_CREATED");
                    }
                    //continue
                case VIEW_CREATED:
                    ActivityCompatibleInfoCollector.clearHolder(scene);
                    View view = scene.getView();
                    scene.dispatchDestroyView();
                    if (!causedByActivityLifeCycle) {
                        Utility.removeFromParentView(view);
                    }
                    scene.dispatchDestroy();
                    scene.dispatchDetachScene();
                    scene.dispatchDetachActivity();
                    moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null,endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        }
    }

    public static void moveStateWithSeparation(@NonNull NavigationScene navigationScene,
                                  @NonNull Scene scene, @NonNull State to,
                                  @Nullable Bundle bundle,
                                  boolean causedByActivityLifeCycle,
                                  @Nullable Function<Scene, Void> afterOnActivityCreatedAction,
                                  @Nullable Runnable endAction) {
        State currentState = scene.getState();
        if (currentState == to) {
            if (endAction != null) {
                endAction.run();
            }
            return;
        }

        if (currentState.value < to.value) {
            switch (currentState) {
                case NONE:
                    scene.dispatchAttachActivity(navigationScene.requireActivity());
                    scene.dispatchAttachScene(navigationScene);
                    scene.dispatchCreate(bundle);
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, afterOnActivityCreatedAction, endAction);
                    break;
                case CREATED:
                    ViewGroup containerView = navigationScene.getSceneContainer();
                    scene.dispatchCreateView(bundle, containerView);
                    if (ActivityCompatibleInfoCollector.isTargetSceneType(scene)) {
                        Record record = navigationScene.findRecordByScene(scene);
                        navigationScene.mNavigationSceneManager.saveActivityCompatibleInfo(record);
                    }
                    if (!causedByActivityLifeCycle) {
                        if (scene.getView().getBackground() == null) {
                            Record record = navigationScene.findRecordByScene(scene);
                            if (!record.mIsTranslucent && navigationScene.mNavigationSceneOptions.fixSceneBackground()) {
                                int resId = navigationScene.mNavigationSceneOptions.getSceneBackgroundResId();
                                if (resId > 0) {
                                    scene.getView().setBackgroundDrawable(scene.requireSceneContext().getResources().getDrawable(resId));
                                } else {
                                    scene.getView().setBackgroundDrawable(Utility.getWindowBackground(scene.requireSceneContext()));
                                }
                                record.mSceneBackgroundSet = true;
                            }
                        }
                        /*
                         * TODO: What if the NavigationScene has been destroyed at this time?
                         * TODO: What to do with serialization
                         */
                        if (bundle != null) {
                            //Scene restore from save and restore path
                            int viewIndex = NavigationSceneViewUtility.targetViewIndexOfScene(navigationScene, navigationScene.mNavigationSceneOptions, scene);
                            containerView.addView(scene.getView(), viewIndex);
                        } else {
                            containerView.addView(scene.getView());
                        }
                    }
                    scene.getView().setVisibility(View.GONE);
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, afterOnActivityCreatedAction, endAction);
                    break;
                case VIEW_CREATED:
                    scene.dispatchActivityCreated(bundle);
                    if (afterOnActivityCreatedAction != null) {
                        afterOnActivityCreatedAction.apply(scene);
                    }
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case ACTIVITY_CREATED:
                    scene.getView().setVisibility(View.VISIBLE);
                    if (navigationScene.isReusing(scene)) {
                        // The view may have been removed by NavigationAnimationExecutor in the reuse process,
                        // so we need to re-attach it to the view tree and prepare it for reuse if necessary.
                        doAttachWhenReuse(navigationScene, scene, bundle);
                    }
                    scene.dispatchStart();
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case STARTED:
                    scene.dispatchResume();
                    ((NavigationSceneManager)navigationScene.mNavigationSceneManager).onSceneResumedWindowFocusChanged(scene);
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        } else {
            switch (currentState) {
                case RESUMED:
                    scene.dispatchPause();
                    ((NavigationSceneManager)navigationScene.mNavigationSceneManager).onScenePausedWindowFocusChanged(scene);
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case STARTED:
                    scene.dispatchStop();
                    if (!causedByActivityLifeCycle) {
                        scene.getView().setVisibility(View.GONE);
                    }
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case ACTIVITY_CREATED:
                    if (to == State.VIEW_CREATED) {
                        throw new IllegalArgumentException("cant switch state ACTIVITY_CREATED to VIEW_CREATED");
                    }
                    //continue
                case VIEW_CREATED:
                    ActivityCompatibleInfoCollector.clearHolder(scene);
                    View view = scene.getView();
                    scene.dispatchDestroyView();
                    if (!causedByActivityLifeCycle) {
                        Utility.removeFromParentView(view);
                    }
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                case CREATED:
                    scene.dispatchDestroy();
                    scene.dispatchDetachScene();
                    scene.dispatchDetachActivity();
                    moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        }
    }

    @Override
    public void moveState(@NonNull NavigationScene navigationScene, @NonNull Scene scene, @NonNull State to, @Nullable Bundle bundle, boolean causedByActivityLifeCycle, @Nullable Runnable endAction) {
        this.moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, null, endAction);
    }

    @Override
    public void moveState(@NonNull NavigationScene navigationScene,
                                 @NonNull Scene scene, @NonNull State to,
                                 @Nullable Bundle bundle,
                                 boolean causedByActivityLifeCycle,
                                 @Nullable Function<Scene, Void> afterOnActivityCreatedAction,
                                 @Nullable Runnable endAction) {
        if (bundle != null) {
            LoggerManager.getInstance().i(TAG, "Sync Scene " + scene.toString() + " Lifecycle [" + scene.getState().name + " -> " + to.name + "] with previous saved State");
        } else {
            LoggerManager.getInstance().i(TAG, "Sync Scene " + scene.toString() + " Lifecycle [" + scene.getState().name + " -> " + to.name + "] without saved State");
        }
        if (this.mCurrentSyncingStateScene != null) {
            LoggerManager.getInstance().e(TAG, "Something error, previous Scene " + this.mCurrentSyncingStateScene.toString() + " sync lifecycle is not finished, it will throw exception in the future \n" + Log.getStackTraceString(new Throwable()));
        }

        try {
            this.mCurrentSyncingStateScene = scene;
            if (mIsSeparateCreateFromCreateView) {
                moveStateWithSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, afterOnActivityCreatedAction, endAction);
            } else {
                moveStateNonSeparation(navigationScene, scene, to, bundle, causedByActivityLifeCycle, afterOnActivityCreatedAction, endAction);
            }
        } finally {
            this.mCurrentSyncingStateScene = null;
        }
    }

    //avoid exceptions being caught externally
    public void executeOperationSafely(final Operation operation, final Runnable operationEndAction) {
        try {
            operation.execute(operationEndAction);
        } catch (final Throwable throwable) {
            if (this.mThrowableHandler == null) {
                this.mThrowableHandler = new AsyncHandler(Looper.getMainLooper());
            }
            this.mThrowableHandler.post(new Runnable() {
                @Override
                public void run() {
                    throw throwable;
                }
            });
            throw throwable;
        }
    }

    private class PopOptionOperation implements Operation {
        private final PopOptions mPopOptions;

        private PopOptionOperation(PopOptions popOptions) {
            this.mPopOptions = popOptions;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            List<Record> recordList = mBackStackList.getCurrentRecordList();

            Predicate<Scene> popUtilPredicate = this.mPopOptions.getPopUtilPredicate();
            int count = 0;
            if (popUtilPredicate != null) {
                for (int i = recordList.size() - 1; i >= 0; i--) {
                    Record record = recordList.get(i);
                    if (popUtilPredicate.apply(record.mScene)) {
                        break;
                    }
                    count++;
                }
                new PopCountOperation(mPopOptions.getNavigationAnimationExecutor(), count).execute(operationEndAction);
            } else {
                new PopOperation(mPopOptions.getNavigationAnimationExecutor()).execute(operationEndAction);
            }
        }
    }

    /**
     *  A -> B, then B return to A
     *  A onPause -> A onStop -> A onDestroyView -> B onStart -> B onResume
     */
    private class PopCountOperation implements Operation {
        private final NavigationAnimationExecutor animationFactory;
        private final int popCount;
        @Nullable
        private final Function<Scene, Void> afterOnActivityCreatedAction;

        private PopCountOperation(NavigationAnimationExecutor animationFactory, int popCount) {
            this(animationFactory, popCount, null);
        }

        private PopCountOperation(NavigationAnimationExecutor animationFactory, int popCount, Function<Scene, Void> afterOnActivityCreatedAction) {
            this.animationFactory = animationFactory;
            this.popCount = popCount;
            this.afterOnActivityCreatedAction = afterOnActivityCreatedAction;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            cancelCurrentRunningAnimation();

            if (!canExecuteNavigationStackOperation()) {
                throw new IllegalArgumentException("Can't pop, current NavigationScene state " + mNavigationScene.getState().name);
            }

            List<Record> recordList = mBackStackList.getCurrentRecordList();
            if (this.popCount <= 0) {
                throw new IllegalArgumentException("popCount can not be " + this.popCount + " stackSize is " + recordList.size());
            }
            if (this.popCount >= recordList.size()) {
                /*
                 * Need to pop all that can pop.
                 * Extreme case: there are 2 Scenes, pop two times and push one new,
                 * the new one will push failed because the Activity has been destroyed.
                 */
                if (recordList.size() > 1) {
                    new PopCountOperation(animationFactory, recordList.size() - 1).execute(EMPTY_RUNNABLE);
                }
                mNavigationScene.finishCurrentActivity();
                operationEndAction.run();
                return;
            }

            final List<Record> destroyRecordList = new ArrayList<>();
            for (int i = 0; i <= this.popCount - 1; i++) {
                Record record = recordList.get(recordList.size() - 1 - i);
                destroyRecordList.add(record);
            }

            final Record returnRecord = recordList.get(recordList.size() - this.popCount - 1);
            final Record currentRecord = mBackStackList.getCurrentRecord();
            final Scene currentScene = currentRecord.mScene;
            final View currentSceneView = currentScene.getView();

            /*
             * The practice here should be to remove those Scenes in the middle,
             * then animate the two Scenes.
             */
            for (final Record record : destroyRecordList) {
                destroyByRecord(record, currentRecord);
            }

            Scene dstScene = returnRecord.mScene;
            //When Scene has created View, compare its cached Configuration to the latest Configuration, if it is changed, recreate it
            boolean recreated = dispatchOnConfigurationChangedToRecord(returnRecord, dstScene);
            if (recreated) {
                //new scene instance is created
                dstScene = returnRecord.mScene;
            }

            final boolean isNavigationSceneInAnimationState = mNavigationScene.getState().value >= State.STARTED.value;
            final State dstState = mNavigationScene.getState();

            if (afterOnActivityCreatedAction != null) {
                if (mOnlyRestoreVisibleScene && dstScene.getState().value < State.ACTIVITY_CREATED.value) {
                    //Scene is destroyed, so schedule onNewIntent after onActivityCreated
                    Bundle dstScenePreviousDstSavedState = returnRecord.mPreviousSavedState;
                    returnRecord.mPreviousSavedState = null;
                    moveState(mNavigationScene, dstScene, dstState, dstScenePreviousDstSavedState, false, afterOnActivityCreatedAction, null);
                } else {
                    afterOnActivityCreatedAction.apply(dstScene);
                    moveState(mNavigationScene, dstScene, dstState, null, false, null);
                }
            } else {
                if (mOnlyRestoreVisibleScene) {
                    Bundle dstScenePreviousDstSavedState = returnRecord.mPreviousSavedState;
                    returnRecord.mPreviousSavedState = null;
                    moveState(mNavigationScene, dstScene, dstState, dstScenePreviousDstSavedState, false, null);
                } else {
                    moveState(mNavigationScene, dstScene, dstState, null, false, null);
                }
            }
            // Ensure that the requesting Scene is correct
            if (currentRecord.mPushResultCallback != null) {
                currentRecord.mPushResultCallback.onResult(currentRecord.mPushResult);
            }

            /*
             * In case of multiple translucent overlays of an opaque Scene,
             * after returning, it is necessary to set the previous translucent Scene to STARTED
             */
            //TODO other visible scenes should also deal with onConfigurationChanged
            if (returnRecord.mIsTranslucent) {
                final List<Record> currentRecordList = mBackStackList.getCurrentRecordList();
                if (currentRecordList.size() > 1) {
                    for (int i = currentRecordList.size() - 2; i >= 0; i--) {
                        Record record = currentRecordList.get(i);
                        if (mOnlyRestoreVisibleScene) {
                            moveState(mNavigationScene, record.mScene, findMinState(mNavigationScene.getState(), State.STARTED), record.mPreviousSavedState, false, null);
                            record.mPreviousSavedState = null;
                        } else {
                            moveState(mNavigationScene, record.mScene, findMinState(mNavigationScene.getState(), State.STARTED), null, false, null);
                        }
                        if (!record.mIsTranslucent) {
                            break;
                        }
                    }
                }
            }

            restoreActivityStatus(returnRecord.mActivityStatusRecord);
            mNavigationListener.navigationChange(currentRecord.mScene, returnRecord.mScene, false);

            NavigationAnimationExecutor navigationAnimationExecutor = null;
            // If Pop has a specified animation, the animation specified by Pop is preferred.
            if (animationFactory != null && animationFactory.isSupport(currentRecord.mScene.getClass(), returnRecord.mScene.getClass())) {
                navigationAnimationExecutor = animationFactory;
            }

            if (navigationAnimationExecutor == null && currentRecord.mNavigationAnimationExecutor != null && currentRecord.mNavigationAnimationExecutor.isSupport(currentRecord.mScene.getClass(), returnRecord.mScene.getClass())) {
                navigationAnimationExecutor = currentRecord.mNavigationAnimationExecutor;
            }

            if (navigationAnimationExecutor == null) {
                navigationAnimationExecutor = mNavigationScene.getDefaultNavigationAnimationExecutor();
            }

            if (!mDisableNavigationAnimation && isNavigationSceneInAnimationState && navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(currentRecord.mScene.getClass(), returnRecord.mScene.getClass())) {
                ViewGroup animationContainer = mNavigationScene.getAnimationContainer();
                // Ensure that the Z-axis is correct
                AnimatorUtility.bringAnimationViewToFrontIfNeeded(mNavigationScene);
                navigationAnimationExecutor.setAnimationViewGroup(animationContainer);

                final CancellationSignalList cancellationSignalList = new CancellationSignalList();
                final Runnable endAction = new Runnable() {
                    @Override
                    public void run() {
                        mCancellationSignalManager.remove(cancellationSignalList);
                        mNavigationScene.addToReuseCache(currentRecord.mScene);
                        notifyNavigationAnimationEnd(currentScene, returnRecord.mScene, false);
                        operationEndAction.run();
                    }
                };

                final AnimationInfo fromInfo = new AnimationInfo(currentScene, currentSceneView, currentScene.getState(), currentRecord.mIsTranslucent);
                final AnimationInfo toInfo = new AnimationInfo(returnRecord.mScene, returnRecord.mScene.getView(), returnRecord.mScene.getState(), returnRecord.mIsTranslucent);

                mCancellationSignalManager.add(cancellationSignalList);
                /*
                 * In the extreme case of Pop immediately after Push,
                 * We are likely to executed pop() before the layout() of the View being pushing.
                 * At this time, both height and width are 0, and it has no parent.
                 * As the animation cannot be executed, so we need to correct this case.
                 */
                navigationAnimationExecutor.executePopChange(mNavigationScene,
                        mNavigationScene.getView().getRootView(),
                        fromInfo, toInfo, cancellationSignalList, endAction);
            } else {
                mNavigationScene.addToReuseCache(currentRecord.mScene);
                operationEndAction.run();
            }
        }
    }

    private Drawable getDefaultSceneBackgroundDrawable(Scene scene) {
        int resId = mNavigationScene.mNavigationSceneOptions.getSceneBackgroundResId();
        if (resId > 0) {
            return scene.requireSceneContext().getResources().getDrawable(resId);
        } else {
            return Utility.getWindowBackground(scene.requireSceneContext());
        }
    }

    private class TranslucentOperation implements Operation {
        private final Scene scene;
        private final boolean translucent;

        private TranslucentOperation(Scene scene, boolean translucent) {
            this.scene = scene;
            this.translucent = translucent;
        }

        @Override
        public void execute(Runnable operationEndAction) {
            cancelCurrentRunningAnimation();

            if (!canExecuteNavigationStackOperation()) {
                throw new IllegalArgumentException("Can't change translucent, current NavigationScene state " + mNavigationScene.getState().name);
            }

            Record curRecord = findRecordByScene(scene);
            if (curRecord == null) {
                operationEndAction.run();
                return;
            }
            if (curRecord.mIsTranslucent == translucent) {
                operationEndAction.run();
                return;
            }
            curRecord.mIsTranslucent = translucent;
            //Because Scene's background is changed by ours, so we should restore it to default null
            if (curRecord.mSceneBackgroundSet) {
                scene.getView().setBackgroundDrawable(translucent ? null : getDefaultSceneBackgroundDrawable(scene));
            }

            final List<Record> currentRecordList = mBackStackList.getCurrentRecordList();
            if (currentRecordList.size() == 1) {
                //nothing need change
                operationEndAction.run();
                return;
            }

            List<Record> needChangeStateSceneRecordList = new ArrayList<>();
            boolean findCurrentScene = false;
            for (int i = currentRecordList.size() - 1; i >= 0; i--) {
                Record tmpRecord = currentRecordList.get(i);
                if (!findCurrentScene) {
                    if (tmpRecord == curRecord) {
                        findCurrentScene = true;
                    }
                } else {
                    needChangeStateSceneRecordList.add(tmpRecord);
                    if (!tmpRecord.mIsTranslucent) {
                        break;
                    }
                }
            }

            if (needChangeStateSceneRecordList.size() > 1) {
                Collections.reverse(needChangeStateSceneRecordList);
            }
            State state = curRecord.mScene.getState();
            State dstState = findMinState(state, translucent ? State.STARTED : State.ACTIVITY_CREATED);

            for (int i = 0; i < needChangeStateSceneRecordList.size(); i++) {
                Record tmpRecord = needChangeStateSceneRecordList.get(i);
                if (mOnlyRestoreVisibleScene && tmpRecord.mScene.getView() == null) {
                    moveState(mNavigationScene, tmpRecord.mScene, dstState, tmpRecord.mPreviousSavedState, false, null);
                    tmpRecord.mPreviousSavedState = null;
                } else {
                    moveState(mNavigationScene, tmpRecord.mScene, dstState, null, false, null);
                }
            }

            operationEndAction.run();
        }
    }

    public void cancelCurrentRunningAnimation() {
        mCancellationSignalManager.cancelAllRunningAnimationExecutor();
        InteractionNavigationPopAnimationFactory.cancelAllRunningInteractionAnimation();
    }

    public static class CancellationSignalManager {
        private final List<CancellationSignalList> cancelableList = new ArrayList<>();

        private void cancelAllRunningAnimationExecutor() {
            if (cancelableList.size() == 0) {
                return;
            }

            List<CancellationSignalList> copy = new ArrayList<>(cancelableList);
            Iterator<CancellationSignalList> iterator = copy.iterator();
            while (iterator.hasNext()) {
                CancellationSignalList cancellationSignal = iterator.next();
                iterator.remove();
                cancellationSignal.cancel();
            }
            cancelableList.removeAll(copy);
        }

        public void add(CancellationSignalList cancellationSignalList) {
            this.cancelableList.add(cancellationSignalList);
        }

        public void remove(CancellationSignalList cancellationSignalList) {
            this.cancelableList.remove(cancellationSignalList);
        }
    }

    /**
     *  1. If it is the top layer, then it is Pop.
     *  2. After removal, the following Scene will need to update the status if needed
     *  3. What if the removal is root?
     */
    private class RemoveOperation implements Operation {
        private final Scene mScene;

        private RemoveOperation(Scene scene) {
            this.mScene = scene;
        }

        @Override
        public void execute(Runnable operationEndAction) {
            if (getCurrentScene() == mScene) {
                new PopOperation(null).execute(operationEndAction);
                return;
            }

            List<Record> list = mBackStackList.getCurrentRecordList();
            for (int i = list.size() - 1; i >= 0; i--) {
                Record record = list.get(i);
                if (record.mScene == mScene) {
                    // It is possible to be animating, so reset the animation
                    if (i == list.size() - 2) {
                        cancelCurrentRunningAnimation();
                    }

                    State sceneState = mScene.getState();
                    moveState(mNavigationScene, mScene, State.NONE, null, false, null);
                    mBackStackList.remove(record);

                    // Deal with the transparent
                    if (i > 0) {
                        Record belowRecord = list.get(i - 1);
                        if (mOnlyRestoreVisibleScene) {
                            //only recreate below scene when the removed scene is visible
                            if (sceneState == State.STARTED || sceneState == State.RESUMED) {
                                moveState(mNavigationScene, belowRecord.mScene, sceneState, belowRecord.mPreviousSavedState, false, null);
                                belowRecord.mPreviousSavedState = null;
                            }
                        } else {
                            moveState(mNavigationScene, belowRecord.mScene, sceneState, null, false, null);
                        }
                    }
                    break;
                }
            }
            operationEndAction.run();
        }
    }

    private class PopOperation implements Operation {
        private final NavigationAnimationExecutor animationFactory;

        private PopOperation(NavigationAnimationExecutor animationFactory) {
            this.animationFactory = animationFactory;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            new PopCountOperation(animationFactory, 1).execute(operationEndAction);
        }
    }

    private class PopToRootOperation implements Operation {
        private final NavigationAnimationExecutor animationFactory;

        private PopToRootOperation(NavigationAnimationExecutor animationFactory) {
            this.animationFactory = animationFactory;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            List<Record> recordList = mBackStackList.getCurrentRecordList();

            int count = recordList.size() - 1;
            if (count == 0) {
                operationEndAction.run();
                return;
            }
            new PopCountOperation(animationFactory, count).execute(operationEndAction);
        }
    }

    private class PopToOperation implements Operation {
        private final Class<? extends Scene> clazz;
        private final NavigationAnimationExecutor animationFactory;

        private PopToOperation(Class<? extends Scene> clazz, NavigationAnimationExecutor animationFactory) {
            this.clazz = clazz;
            this.animationFactory = animationFactory;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            List<Record> recordList = mBackStackList.getCurrentRecordList();
            Record returnRecord = null;
            int popCount = 0;
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (record.mScene.getClass() == clazz) {
                    returnRecord = record;
                    break;
                }
                popCount++;
            }

            // There is nothing at all
            if (returnRecord == null) {
                throw new IllegalArgumentException("Cant find " + clazz.getSimpleName() + " in backStack");
            }

            if (popCount == 0) {
                operationEndAction.run();
                return;
            }
            new PopCountOperation(animationFactory, popCount).execute(operationEndAction);
        }
    }

    public static State findMinState(State left, State right) {
        if (left.value > right.value) {
            return right;
        } else {
            return left;
        }
    }

    public boolean canExecuteNavigationStackOperation() {
        return mNavigationScene.getState().value >= State.ACTIVITY_CREATED.value;
    }

    private class PushRootOperation implements Operation {
        private final Scene scene;
        private final boolean isSceneTranslucent;

        private PushRootOperation(Scene scene) {
            this.scene = scene;
            this.isSceneTranslucent = scene instanceof SceneTranslucent;
        }

        @Override
        public void execute(@Nullable final Runnable operationEndAction) {
            pushRootOperationInternal(this.scene, isSceneTranslucent, operationEndAction);
        }
    }

    private void pushRootOperationInternal(Scene scene, boolean isSceneTranslucent, @Nullable final Runnable operationEndAction) {
        final Record currentRecord = mBackStackList.getCurrentRecord();

        /*
         * It is possible to repeatedly push the same Scene object multiple times in multiple NavigationScene
         * But as the Push operation is not necessarily executed immediately,
         * the abnormal judgment in the Push method does not necessarily work.
         * So we need to check this case here to throw an exception.
         */
        if (scene.getParentScene() != null) {
            if (scene.getParentScene() == mNavigationScene) {
                if (operationEndAction != null) {
                    operationEndAction.run();
                }
                return;
            }
            throw new IllegalArgumentException("Scene already has a parent, parent " + scene.getParentScene());
        }

        final Record record = Record.newInstance(scene, isSceneTranslucent, null);
        mBackStackList.push(record);

        moveState(mNavigationScene, scene, mNavigationScene.getState(), null, false, null);

        mNavigationListener.navigationChange(currentRecord != null ? currentRecord.mScene : null, scene, true);
        if (operationEndAction != null) {
            operationEndAction.run();
        }
    }

    private class PushOptionOperation implements Operation {
        private final Scene scene;
        private final PushOptions pushOptions;
        private final boolean isSceneTranslucent;
        private final LaunchModeBehavior mLaunchModeBehavior;

        private PushOptionOperation(Scene scene, PushOptions pushOptions) {
            this.scene = scene;
            this.pushOptions = pushOptions;
            this.isSceneTranslucent = pushOptions.isIsTranslucent() || scene instanceof SceneTranslucent;
            this.mLaunchModeBehavior = pushOptions.provideLaunchModeBehavior(scene.getClass());
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            cancelCurrentRunningAnimation();
            if (!canExecuteNavigationStackOperation()) {
                throw new IllegalArgumentException("Can't push, current NavigationScene state " + mNavigationScene.getState().name);
            }

            final Record currentRecord = mBackStackList.getCurrentRecord();
            final View currentView = currentRecord != null ? currentRecord.mScene.getView() : null;

            /*
             * It is possible to repeatedly push the same Scene object multiple times in multiple NavigationScene
             * But as the Push operation is not necessarily executed immediately,
             * the abnormal judgment in the Push method does not necessarily work.
             * So we need to check this case here to throw an exception.
             */
            boolean isFromReuse = mNavigationScene.isReusing(this.scene);
            if (!isFromReuse && this.scene.getParentScene() != null) {
                if (this.scene.getParentScene() == mNavigationScene) {
                    operationEndAction.run();
                    return;
                }
                throw new IllegalArgumentException("Scene already has a parent, parent " + scene.getParentScene());
            }

            if (this.mLaunchModeBehavior != null) {
                final List<Pair<Scene, Bundle>> previousSceneList = getCurrentSceneAndArgumentsList();
                boolean isIntercepted = this.mLaunchModeBehavior.onInterceptPushOperation(previousSceneList);
                if (isIntercepted) {
                    int popSceneCount = this.mLaunchModeBehavior.getPopSceneCount();
                    final Bundle newArguments = scene.getArguments();
                    Function<Scene, Void> onNewIntentAction = new Function<Scene, Void>() {
                        @Override
                        public Void apply(@NonNull Scene scene) {
                            mLaunchModeBehavior.sceneOnNewIntent(scene, newArguments);
                            return null;
                        }
                    };
                    if (popSceneCount > 0) {
                        //fallback to pop
                        if (popSceneCount >= previousSceneList.size()) {
                            throw new IllegalArgumentException("LaunchModeBehavior getPopSceneCount count > current Scene count, LaunchModeBehavior type " + this.mLaunchModeBehavior.getClass());
                        }
                        new PopCountOperation(this.pushOptions.getNavigationAnimationFactory(), popSceneCount, onNewIntentAction).execute(operationEndAction);
                    } else {
                        Scene dstScene = getCurrentScene();
                        if (dstScene.getState() == State.RESUMED) {
                            //onPause -> onNewIntent -> onResume
                            moveState(mNavigationScene, dstScene, State.STARTED, null, false, null);
                            onNewIntentAction.apply(dstScene);
                            moveState(mNavigationScene, dstScene, State.RESUMED, null, false, null);
                        } else {
                            //onPause -> onNewIntent
                            //onStop -> onNewIntent
                            onNewIntentAction.apply(dstScene);
                        }
                        operationEndAction.run();
                    }
                    return;
                }
            }

            Predicate<Scene> removePredicate = pushOptions.getRemovePredicate();
            boolean isTaskRootReplaced = false;
            Record newTaskRoot = null;
            if (removePredicate != null) {
                final List<Record> previousRecordList = mBackStackList.getCurrentRecordList();
                for (int i = previousRecordList.size() - 1; i >= 0; i--) {
                    Record oldRecord = previousRecordList.get(i);
                    Scene oldScene = oldRecord.mScene;
                    if (!removePredicate.apply(oldScene)) {
                        newTaskRoot = oldRecord;
                        continue;
                    }
                    moveState(mNavigationScene, oldScene, State.NONE, null, false, null);
                    mBackStackList.remove(oldRecord);

                    if (i == 0) {
                        isTaskRootReplaced = true;
                    }
                }
            }

            if (currentRecord != null && mBackStackList.getCurrentRecordList().contains(currentRecord)) {
                currentRecord.saveActivityStatus();
                Scene currentScene = currentRecord.mScene;
                State dstState = isSceneTranslucent ? State.STARTED : State.ACTIVITY_CREATED;
                dstState = findMinState(dstState, mNavigationScene.getState());
                moveState(mNavigationScene, currentScene, dstState, null, false, null);

                /*
                 * In case of multiple translucent overlays of an opaque Scene,
                 * it is necessary to set the previous translucent Scene to ACTIVITY_CREATED
                 */
                final List<Record> currentRecordList = mBackStackList.getCurrentRecordList();
                if (currentRecordList.size() > 1 && !isSceneTranslucent && currentRecord.mIsTranslucent) {
                    for (int i = currentRecordList.size() - 2; i >= 0; i--) {
                        Record record = currentRecordList.get(i);
                        moveState(mNavigationScene, record.mScene, findMinState(State.ACTIVITY_CREATED, mNavigationScene.getState()), null, false, null);
                        if (!record.mIsTranslucent) {
                            break;
                        }
                    }
                }
            }

            final NavigationAnimationExecutor animationFactory = pushOptions.getNavigationAnimationFactory();
            final Record record = Record.newInstance(scene, isSceneTranslucent, animationFactory);
            record.mPushResultCallback = pushOptions.getPushResultCallback();
            mBackStackList.push(record);

            if (isTaskRootReplaced) {

            }

            /*
             * TODO: In fact, it is need to support that moveState to the specified state.
             *       Because of the destruction restore, it is impossible to go directly to RESUMED
             */
            moveState(mNavigationScene, scene, mNavigationScene.getState(), null, false, null);
            mNavigationListener.navigationChange(currentRecord != null ? currentRecord.mScene : null, scene, true);

            //Navigation animation only execute when NavigationScene is visible, otherwise skip
            final boolean isNavigationSceneInAnimationState = mNavigationScene.getState().value >= State.STARTED.value;
            if (!mDisableNavigationAnimation && isNavigationSceneInAnimationState && currentRecord != null) {
                NavigationAnimationExecutor navigationAnimationExecutor = null;
                //Scene can override mNavigationAnimationExecutor in moveState method by NavigationScene.overrideNavigationAnimationExecutor
                NavigationAnimationExecutor recordAnimationExecutor = record.mNavigationAnimationExecutor;
                if (recordAnimationExecutor != null && recordAnimationExecutor.isSupport(currentRecord.mScene.getClass(), scene.getClass())) {
                    navigationAnimationExecutor = recordAnimationExecutor;
                }
                if (navigationAnimationExecutor == null && animationFactory != null && animationFactory.isSupport(currentRecord.mScene.getClass(), scene.getClass())) {
                    navigationAnimationExecutor = animationFactory;
                }
                if (navigationAnimationExecutor == null) {
                    navigationAnimationExecutor = mNavigationScene.getDefaultNavigationAnimationExecutor();
                }

                if (navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(currentRecord.mScene.getClass(), scene.getClass())) {
                    final Scene finalCurrentScene = currentRecord.mScene;

                    AnimatorUtility.bringSceneViewToFrontIfNeeded(mNavigationScene);//保证Z轴正确
                    navigationAnimationExecutor.setAnimationViewGroup(mNavigationScene.getAnimationContainer());

                    AnimationInfo fromInfo = new AnimationInfo(finalCurrentScene, currentView, finalCurrentScene.getState(), currentRecord.mIsTranslucent);
                    AnimationInfo toInfo = new AnimationInfo(scene, scene.getView(), scene.getState(), record.mIsTranslucent);

                    final CancellationSignalList cancellationSignalList = new CancellationSignalList();
                    mCancellationSignalManager.add(cancellationSignalList);

                    navigationAnimationExecutor.executePushChange(mNavigationScene,
                            mNavigationScene.getView().getRootView(),
                            fromInfo, toInfo, cancellationSignalList, new Runnable() {
                                @Override
                                public void run() {
                                    mCancellationSignalManager.remove(cancellationSignalList);
                                    notifyNavigationAnimationEnd(finalCurrentScene, scene, true);
                                    operationEndAction.run();
                                }
                            });
                } else {
                    operationEndAction.run();
                }
            } else {
                operationEndAction.run();
            }
        }
    }

    private class RecreateOperation implements Operation {
        private final Scene scene;

        private RecreateOperation(@NonNull Scene scene) {
            this.scene = scene;
        }

        @Override
        public void execute(Runnable operationEndAction) {
            if (this.scene.getState() == State.NONE) {
                //Target scene is destroyed, skip
                if (operationEndAction != null) {
                    operationEndAction.run();
                }
                return;
            }

            if (!scene.isSceneRestoreEnabled()) {
                throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " don't support restore, so it can't use recreate");
            }
            if (!SceneInstanceUtility.isConstructorMethodSupportRestore(scene)) {
                throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " must be a public class or public static class, " +
                        "and have only one parameterless constructor to be properly recreated.");
            }

            Record record = mBackStackList.getRecordByScene(this.scene);
            State targetState = this.scene.getState();

            LoggerManager.getInstance().i(TAG, "RecreateOperation current Scene save latest data, current Scene instance " + scene.toString());

            Bundle savedInstanceState = new Bundle();
            this.scene.dispatchSaveInstanceState(savedInstanceState);

            LoggerManager.getInstance().i(TAG, "RecreateOperation current Scene destroy itself, current Scene instance " + scene.toString());
            moveState(mNavigationScene, this.scene, State.NONE, null, false, null);

            Scene newSceneInstance = null;
            if (mBackStackList.isRootScene(this.scene) && mNavigationScene.mRootSceneComponentFactory != null) {
                Scene sceneInstance = mNavigationScene.mRootSceneComponentFactory.instantiateScene(mNavigationScene.requireActivity().getClassLoader(), record.mSceneClassName, null);
                if (sceneInstance != null && sceneInstance.getParentScene() != null) {
                    throw new IllegalArgumentException("SceneComponentFactory instantiateScene return Scene already has a parent");
                }
                if (sceneInstance != null) {
                    LoggerManager.getInstance().i(TAG, "RecreateOperation create new Scene by SceneComponentFactory");
                }
                newSceneInstance = sceneInstance;
            }
            if (newSceneInstance == null) {
                LoggerManager.getInstance().i(TAG, "RecreateOperation create new Scene directly");
                Class<?> sceneClass = scene.getClass();
                newSceneInstance = SceneInstanceUtility.getInstanceFromClass(sceneClass, null);
            }
            record.mScene = newSceneInstance;

            LoggerManager.getInstance().i(TAG, "RecreateOperation new created Scene restore from previous data, new Scene instance " + newSceneInstance.toString());
            moveState(mNavigationScene, newSceneInstance, targetState, savedInstanceState, false, null);

            if (operationEndAction != null) {
                operationEndAction.run();
            }
        }
    }

    private class SyncCurrentSceneStateOperation implements Operation {
        private final State state;

        private SyncCurrentSceneStateOperation(State state) {
            this.state = state;
        }

        @Override
        public void execute(@Nullable Runnable operationEndAction) {
            syncCurrentSceneStateOperationInternal(this.state, operationEndAction);
        }
    }

    private void syncCurrentSceneStateOperationInternal(State state, Runnable operationEndAction) {
        if (getCurrentRecord() == null) {
            if (operationEndAction != null) {
                operationEndAction.run();
            }
            return;
        }

        // Translucent processing ensures that the correct method can be executed
        List<Record> recordList = mBackStackList.getCurrentRecordList();
        State targetState = state;
        for (int i = recordList.size() - 1; i >= 0; i--) {
            Record record = recordList.get(i);
            Bundle previousSavedState = null;
            if (mRestoreStateInLifecycle) {
                previousSavedState = record.consumeSavedInstanceState();
            }
            boolean modifyViewHierarchy = previousSavedState != null;
            if (i == recordList.size() - 1) {
                moveState(mNavigationScene, record.mScene, targetState, previousSavedState, !modifyViewHierarchy, operationEndAction);
                // If the current one is opaque, there is no need to traverse it again.
                if (!record.mIsTranslucent) {
                    break;
                }
            } else {
                State fixDstState = null;
                if (targetState == State.RESUMED) {
                    fixDstState = State.STARTED;
                } else if (targetState == State.STARTED) {
                    fixDstState = State.STARTED;
                } else if (targetState == State.ACTIVITY_CREATED) {
                    fixDstState = State.ACTIVITY_CREATED;
                } else if (targetState == State.VIEW_CREATED) {
                    fixDstState = State.VIEW_CREATED;
                }

                moveState(mNavigationScene, record.mScene, fixDstState, previousSavedState, !modifyViewHierarchy, operationEndAction);
                if (!record.mIsTranslucent) {
                    break;
                }
            }
        }

        if (operationEndAction != null) {
            operationEndAction.run();
        }
    }

    private class SyncAllSceneStateOperation implements Operation {
        private final State state;
        private final State nextStageStateHint;
        private final boolean reverseOrder;
        private final boolean causeByActivityLifecycle;

        private SyncAllSceneStateOperation(State state, State nextStageStateHint, boolean reverseOrder, boolean causeByActivityLifecycle) {
            this.state = state;
            this.nextStageStateHint = nextStageStateHint;
            this.reverseOrder = reverseOrder;
            this.causeByActivityLifecycle = causeByActivityLifecycle;
        }

        @Override
        public void execute(@Nullable Runnable operationEndAction) {
            syncAllSceneStateOperationInternal(this.state, this.nextStageStateHint, this.reverseOrder, this.causeByActivityLifecycle, operationEndAction);
        }
    }

    private void syncAllSceneStateOperationInternal(@NonNull State state, @Nullable State nextStageStateHint, boolean reverseOrder, boolean causeByActivityLifecycle, Runnable operationEndAction) {
        if (getCurrentRecord() == null) {
            if (operationEndAction != null) {
                operationEndAction.run();
            }
            return;
        }

        List<Record> recordList = mBackStackList.getCurrentRecordList();
        if (reverseOrder) {
            recordList = new ArrayList<>(recordList);
            Collections.reverse(recordList);
        }

        for (int i = 0; i < recordList.size(); i++) {
            Record record = recordList.get(i);
            Scene scene = record.mScene;
            Bundle previousSavedState = null;
            if (mRestoreStateInLifecycle) {
                if (nextStageStateHint == State.NONE && state != nextStageStateHint && scene.getState() == State.NONE) {
                    /*
                      current state is none, latest state is not none, but the following state is none, so just skip,
                      normally this happen when scene is already recycled and NavigationScene start to destroy because of user exit,
                      at this time, recreate scene is useless
                     */
                    //todo maybe we should invoke record.consumeSavedInstanceState() to clear previous saved state too
                    LoggerManager.getInstance().i(TAG, "Sync Scene Lifecycle skip because target Scene is already destroyed");
                    continue;
                }
                previousSavedState = record.consumeSavedInstanceState();
            }
            boolean modifyViewHierarchy = !causeByActivityLifecycle;
            if (previousSavedState != null) {
                //force modify view hierarchy
                modifyViewHierarchy = true;
            }
            moveState(mNavigationScene, scene, state, previousSavedState, !modifyViewHierarchy, null);
        }
        if (operationEndAction != null) {
            operationEndAction.run();
        }
    }

    public boolean pop(InteractionNavigationPopAnimationFactory animationFactory) {
        InteractionNavigationPopAnimationFactory.cancelAllRunningInteractionAnimation();

        Record record = getCurrentRecord();
        if (record.mIsTranslucent) {
            throw new IllegalArgumentException("InteractionNavigationPopAnimationFactory can't support translucent Scene");
        }
        Scene current = getCurrentScene();
        Record previousRecord = mBackStackList.getPreviousScene();
        if (previousRecord == null) {
            return false;
        }
        Scene previous = previousRecord.mScene;
        if (animationFactory.isSupport(current, previous)) {
            animationFactory.begin(mNavigationScene, current, previous);
            return true;
        } else {
            return false;
        }
    }

    public boolean isInteractionNavigationPopSupport(InteractionNavigationPopAnimationFactory animationFactory) {
        if (!canPop()) {
            return false;
        }

        Record record = getCurrentRecord();
        if (record.mIsTranslucent) {
            return false;
        }
        Scene current = getCurrentScene();
        Record previousRecord = mBackStackList.getPreviousScene();
        if (previousRecord == null) {
            return false;
        }
        Scene previous = previousRecord.mScene;
        return animationFactory.isSupport(current, previous);
    }

    @Override
    public NavigationScene getNavigationScene() {
        return this.mNavigationScene;
    }

    @Override
    public NavigationListener getNavigationListener() {
        return this.mNavigationListener;
    }

    @Override
    public boolean containsRecord(Record record) {
        return this.getCurrentRecordList().contains(record);
    }

    @Override
    public List<Record> getCurrentRecordList() {
        return this.mBackStackList.getCurrentRecordList();
    }

    public List<Pair<Scene, Bundle>> getCurrentSceneAndArgumentsList() {
        List<Pair<Scene, Bundle>> list = new ArrayList<>();
        List<Record> previousRecordList = getCurrentRecordList();
        for (int i = 0; i < previousRecordList.size(); i++) {
            Record tmpRecord = previousRecordList.get(i);
            Bundle tmpBundle = tmpRecord.mScene.getArguments();
            if (tmpBundle == null && tmpRecord.mPreviousSavedState != null) {
                tmpBundle = tmpRecord.mPreviousSavedState.getBundle(ParcelConstants.KEY_SCENE_ARGUMENT);
            }
            list.add(Pair.create(tmpRecord.mScene, tmpBundle));
        }
        return list;
    }

    @Override
    public void pushRecord(Record record) {
        this.mBackStackList.push(record);
    }

    @Override
    public void removeRecord(Record record) {
        this.mBackStackList.remove(record);
    }

    @Override
    public boolean isDisableNavigationAnimation() {
        return this.mDisableNavigationAnimation;
    }

    @Override
    public CancellationSignalManager getCancellationSignalManager() {
        return this.mCancellationSignalManager;
    }

    @Override
    public void notifySceneStateChanged() {
        this.mAnySceneStateChanged = true;
    }

    @Override
    public boolean isOnlyRestoreVisibleScene() {
        return this.mOnlyRestoreVisibleScene;
    }

    @Override
    public void forceExecutePendingNavigationOperation() {
        this.requireMessageQueue().postSync(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void recycleInvisibleScenes() {
        if (!this.mOnlyRestoreVisibleScene) {
            return;
        }
        if (!this.mAnySceneStateChanged) {
            LoggerManager.getInstance().i(TAG, "recycleInvisibleScenes skip because of mAnySceneStateChanged false");
            return;
        }
        if (!mIsNavigationStateChangeInProgress.isEmpty() || !mPendingActionList.isEmpty() || requireMessageQueue().hasPendingTasks()) {
            LoggerManager.getInstance().i(TAG, "recycleInvisibleScenes skip because of navigation operation is in progress");
            return;
        }

        this.mAnySceneStateChanged = false;
        LoggerManager.getInstance().i(TAG, "recycleInvisibleScenes start");
        List<Record> recordList = this.mBackStackList.getCurrentRecordList();
        int size = recordList.size();
        int lastIndex = size - 1;
        int firstOpaqueIndex = lastIndex;
        for (int i = lastIndex; i >= 0; i--) {
            Record record = recordList.get(i);
            if (!record.mIsTranslucent) {
                firstOpaqueIndex = i;
                break;
            }
        }

        int recycleStartIndex = firstOpaqueIndex - 1;
        if (this.mRestoreStateInLifecycle) {
            //we want to recycle top scene when it is invisible
            recycleStartIndex = lastIndex;
        }

        for (int i = recycleStartIndex; i >= 0; i--) {
            Record record = recordList.get(i);
            Scene scene = record.mScene;
            if (this.mRestoreStateInLifecycle && scene instanceof SceneMemoryRecyclePolicy && ((SceneMemoryRecyclePolicy) scene).followActivityLifecycle()) {
                LoggerManager.getInstance().i(TAG, "recycle scene skip " + scene.toString() + " because it followActivityLifecycle");
                continue;
            }
            State sceneState = scene.getState();
            if ((sceneState == State.ACTIVITY_CREATED || sceneState == State.VIEW_CREATED) && scene.isSceneRestoreEnabled()) {
                Bundle sceneBundle = new Bundle();
                scene.dispatchSaveInstanceState(sceneBundle);
                record.mPreviousSavedState = sceneBundle;
                View view = scene.getView();
                moveState(mNavigationScene, scene, State.NONE, null, true, null);
                Utility.removeFromParentView(view);
                //create a new Scene instance to replace previous one
                record.mScene = this.mBackStackList.createNewSceneInstance(mNavigationScene.requireActivity(), i, record, mNavigationScene.mRootSceneComponentFactory);
                LoggerManager.getInstance().i(TAG, "recycle scene " + scene + " from state " + sceneState.getName() + " completed");
            } else {
                //skip because Scene is visible or disable restore
                LoggerManager.getInstance().i(TAG, "recycle scene skip " + scene.toString() + " because it is visible or disable restore");
            }
        }
        LoggerManager.getInstance().i(TAG, "recycleInvisibleScenes finish");
    }

    //this is a temporary simple version, just recreate all Scenes which has view
    //TODO only recreate visible Scenes, other Scenes should only be recreated once it is visible
    //TODO dispatch to other visible Scenes
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        LoggerManager.getInstance().i(TAG, "Activity dispatch onConfigurationChanged start");
        executePendingOperation();//make sure all pending operations have finished
        this.mActivityConfiguration = new Configuration(newConfig);

        Record record = this.mBackStackList.getCurrentRecord();
        Scene scene = record.mScene;

        dispatchOnConfigurationChangedToRecordInternal(record, scene, newConfig, mConfigurationChangesAllowList, new Action1<Scene>() {
            @Override
            public void execute(Scene value) {
                if (mNavigationScene.mNavigationSceneOptions.isRecreateSceneOnNextLoopAfterConfigurationChanged()) {
                    recreate(value);
                } else {
                    executeOperationSafely(new RecreateOperation(value), null);
                }
            }
        });
        LoggerManager.getInstance().i(TAG, "Activity dispatch onConfigurationChanged finish");
    }

    @Override
    public void saveActivityCompatibleInfo(Record record) {
        Configuration newConfig = this.mActivityConfiguration;
        if (newConfig != null) {
            record.saveActivityCompatibleInfo(newConfig);
        } else {
            record.saveActivityCompatibleInfo();
        }
    }

    /**
     * Destroys scenes removed from the reuse pool
     *
     * This method handles the complete destruction of IReuseScene instances
     * that have been removed from the reuse pool, ensuring proper cleanup
     * by moving their state to NONE.
     *
     * @param reuseScenes The list of IReuseScene instances to be destroyed
     */
    @Override public void destroyReuseCache(List<IReuseScene> reuseScenes) {
        for (IReuseScene scene: reuseScenes) {
            moveState(mNavigationScene, (Scene) scene, State.NONE, null, true, null);
        }
    }

    @Override
    public boolean dispatchOnConfigurationChangedToRecord(Record record, Scene scene) {
        Configuration newConfig = this.mActivityConfiguration;
        if (newConfig == null) {
            return false;
        }
        LoggerManager.getInstance().i(TAG, "PopOperation dispatch onConfigurationChanged start");
        boolean result = dispatchOnConfigurationChangedToRecordInternal(record, scene, newConfig, mConfigurationChangesAllowList, new Action1<Scene>() {
            @Override
            public void execute(Scene value) {
                executeOperationSafely(new RecreateOperation(value), null);
            }
        });
        LoggerManager.getInstance().i(TAG, "PopOperation dispatch onConfigurationChanged finish");
        return result;
    }

    //make sure Scene has invoked onCreateView, then dispatch onConfigurationChanged
    private static boolean dispatchOnConfigurationChangedToRecordInternal(@NonNull Record record, @NonNull Scene scene, @NonNull Configuration newConfig, int configurationChangesAllowList, @NonNull Action1<Scene> recreateAction) {
        if (scene.getView() == null) {
            return false;
        }
        ActivityCompatibleInfoCollector.Holder holder = ActivityCompatibleInfoCollector.getHolder(scene);
        if (holder == null) {
            //skip, default behavior, nothing will happen
            return false;
        }
        Integer configChanges = holder.configChanges;
        if (holder.configChanges == null) {
            //skip, default behavior, nothing will happen
            return false;
        }
        if (newConfig.equals(record.mConfiguration)) {
            return false;
        }
        if (record.mConfiguration != null) {
            Configuration sceneConfiguration = record.mConfiguration;
            int diff = sceneConfiguration.diff(newConfig);
            LoggerManager.getInstance().i(TAG, "Configuration has been changed, raw diff " + diff);

            //remove private diff properties
            diff = ConfigurationUtility.removePrivateDiff(diff);
            if (configurationChangesAllowList != 0) {
                diff = (diff & configurationChangesAllowList);
                LoggerManager.getInstance().i(TAG, "clean diff not include in configurationChangesAllowList, result diff " + diff);
            }

            String diffString = ConfigurationUtility.configurationDiffToString(diff);
            LoggerManager.getInstance().i(TAG, "Configuration has been changed, diff " + diffString);

            if ((diff & configChanges) != 0) {
                LoggerManager.getInstance().i(TAG, "Configuration has been changed, Scene has suitable configChanges, so dispatch onConfigurationChanged to " + scene.toString());
                if (scene instanceof ActivityCompatibleBehavior) {
                    ((ActivityCompatibleBehavior) scene).onConfigurationChanged(newConfig);
                    record.saveActivityCompatibleInfo(newConfig);
                    return false;
                } else {
                    throw new SceneInternalException("Impossible, Scene don't implement ActivityCompatibleBehavior but have configChanges " + scene.toString());
                }
            } else {
                if (TextUtils.isEmpty(diffString)) {
                    LoggerManager.getInstance().i(TAG, "Configuration has been changed, skip because unknown diff " + diff);
                    if (scene instanceof ActivityCompatibleBehavior) {
                        ((ActivityCompatibleBehavior) scene).onConfigurationChanged(newConfig);
                        record.saveActivityCompatibleInfo(newConfig);
                        return false;
                    } else {
                        throw new SceneInternalException("Impossible, Scene don't implement ActivityCompatibleBehavior but have configChanges " + scene.toString());
                    }
                } else {
                    LoggerManager.getInstance().i(TAG, "Configuration has been changed, recreate " + scene.toString());
                }
            }
        } else {
            LoggerManager.getInstance().i(TAG, "Scene previous Configuration not found, recreate " + scene.toString());
        }
        recreateAction.execute(scene);
        return true;
    }

    private void dispatchWindowFocusToTargetScene(boolean hasFocus) {
        Scene scene = getCurrentScene();
        if (ActivityCompatibleInfoCollector.isTargetSceneType(scene)) {
            onSceneResumedWindowFocusChangedToTarget(scene, hasFocus);
        } else {
            //try to uninstall window focus listener if there are no ActivityCompatibleBehavior Scene
            uninstallUselessWindowFocusChangeListener();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!this.mNavigationScene.mNavigationSceneOptions.getUseWindowFocusChangedDispatch()) {
            return;
        }
        LoggerManager.getInstance().i(TAG, "onWindowFocusChanged " + hasFocus);

        if (!requireMessageQueue().hasPendingTasks()) {
            dispatchWindowFocusToTargetScene(hasFocus);
        } else {
            LoggerManager.getInstance().i(TAG, "sync window focus by SceneMessageQueue");

            //sync focus after all pending tasks are finished
            WindowFocusChangedPendingTask curWindowFocusChangedPendingTask = this.mPendingWindowFocusChangedPendingTask;
            if (curWindowFocusChangedPendingTask == null) {
                WindowFocusChangedPendingTask windowFocusChangedPendingTask = new WindowFocusChangedPendingTask(hasFocus);
                this.mPendingWindowFocusChangedPendingTask = windowFocusChangedPendingTask;
                requireMessageQueue().postAsync(windowFocusChangedPendingTask);
            } else {
                curWindowFocusChangedPendingTask.updateLatestHasFocus(hasFocus);
            }
        }
    }

    private final class WindowFocusChangedPendingTask extends NavigationRunnable {
        private final Queue<Boolean> hasFocusQueue = new ArrayDeque<>();

        private WindowFocusChangedPendingTask(boolean initHasFocus) {
            this.hasFocusQueue.add(initHasFocus);
        }

        private void updateLatestHasFocus(boolean latestHasFocus) {
            this.hasFocusQueue.add(latestHasFocus);
        }

        @Override
        public void run() {
            if (!requireMessageQueue().hasPendingTasks()) {
                NavigationSceneManager.this.mPendingWindowFocusChangedPendingTask = null;
                while (true) {
                    Boolean curFocus = this.hasFocusQueue.poll();
                    if (curFocus == null) {
                        break;
                    }

                    LoggerManager.getInstance().i(TAG, "WindowFocusChangedPendingTask dispatch onWindowFocusChanged " + curFocus);
                    dispatchWindowFocusToTargetScene(curFocus);
                }
            } else {
                requireMessageQueue().postAsync(this);
            }
        }
    }

    private void uninstallUselessWindowFocusChangeListener() {
        List<Scene> sceneList = new ArrayList<>(getCurrentSceneList());
        boolean found = false;
        for (int i = sceneList.size() - 1; i >= 0; i--) {
            Scene targetScene = sceneList.get(i);
            if (ActivityCompatibleInfoCollector.isTargetSceneType(targetScene)) {
                found = true;
                break;
            }
        }
        if (!found) {
            LoggerManager.getInstance().i(TAG, "uninstall useless WindowFocusChangeListener");
            mNavigationScene.uninstallWindowFocusChangeListenerIfNeeded();
        }
    }

    private void onSceneResumedWindowFocusChangedToTarget(Scene scene, boolean hasFocus) {
        if (!this.mNavigationScene.mNavigationSceneOptions.getUseWindowFocusChangedDispatch()) {
            return;
        }
        if (!ActivityCompatibleInfoCollector.isTargetSceneType(scene)) {
            return;
        }
        Record record = mNavigationScene.findRecordByScene(scene);
        if (record.mLastSceneWindowFocused == hasFocus) {
            return;
        }
        ActivityCompatibleBehavior activityCompatibleBehavior = (ActivityCompatibleBehavior) scene;
        activityCompatibleBehavior.onWindowFocusChanged(hasFocus);
        record.mLastSceneWindowFocused = hasFocus;
    }

    private void onSceneResumedWindowFocusChanged(Scene scene) {
        if (!this.mNavigationScene.mNavigationSceneOptions.getUseWindowFocusChangedDispatch()) {
            return;
        }
        if (!ActivityCompatibleInfoCollector.isTargetSceneType(scene)) {
            return;
        }
        if (scene.getState() != State.RESUMED) {
            return;
        }
        Activity activity = scene.requireActivity();
        if (!activity.hasWindowFocus()) {
            mNavigationScene.installWindowFocusChangeListenerIfNeeded();
        } else {
            onSceneResumedWindowFocusChangedToTarget(scene, true);
            mNavigationScene.installWindowFocusChangeListenerIfNeeded();
        }
    }

    private void onScenePausedWindowFocusChanged(Scene scene) {
        if (!this.mNavigationScene.mNavigationSceneOptions.getUseWindowFocusChangedDispatch()) {
            return;
        }
        if (!ActivityCompatibleInfoCollector.isTargetSceneType(scene)) {
            return;
        }
        if (scene.getState() != State.STARTED) {
            return;
        }
        onSceneResumedWindowFocusChangedToTarget(scene, false);
    }

    @Override
    public void notifyNavigationAnimationEnd(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
        mNavigationScene.notifyNavigationAnimationEnd(from, to, isPush);
    }

    /**
     * Method to properly destroy and handle scene reuse
     */
    @Override public void destroyByRecord(Record record, Record currentRecord) {
        Scene scene = record.mScene;
        State to = State.NONE;
        if (scene instanceof IReuseScene && ((IReuseScene) scene).isReusable()) {
            to = State.ACTIVITY_CREATED;
        }
        this.moveState(this.getNavigationScene(), scene, to, null, false, null);
        this.removeRecord(record);

        if (record != currentRecord) {
            this.getNavigationScene().addToReuseCache(scene);
        }
    }
}
