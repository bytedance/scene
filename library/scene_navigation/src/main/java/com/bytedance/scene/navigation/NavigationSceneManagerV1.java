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

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignalList;
import com.bytedance.scene.utlity.NonNullPair;
import com.bytedance.scene.utlity.Predicate;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.Utility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 2023.11.7
 * default INavigationManager implementation
 */
class NavigationSceneManagerV1 implements INavigationManager {
    private static final String TAG = "NavigationSceneManagerV1";
    private static final String TRACE_EXECUTE_OPERATION_TAG = "NavigationSceneManager#executeOperation";
    private static final String TRACE_EXECUTE_PENDING_OPERATION_TAG = "NavigationSceneManager#executePendingOperation";

    private NavigationScene mNavigationScene;
    private final RecordStack mBackStackList = new RecordStack();
    private NavigationListener mNavigationListener;
    private final AsyncHandler mHandler = new AsyncHandler(Looper.getMainLooper());

    /**
     * If it is paused, currently operations and subsequent operations will put into this queue.
     */
    private final ArrayDeque<Operation> mPendingActionList = new ArrayDeque<>();
    private long mLastPendingActionListItemTimestamp = -1L;

    private final CancellationSignalManager mCancellationSignalManager = new CancellationSignalManager();
    private final List<NonNullPair<LifecycleOwner, OnBackPressedListener>> mOnBackPressedListenerList = new ArrayList<>();

    private final boolean mActivityCompatibleLifecycleStrategyEnabled = SceneGlobalConfig.useActivityCompatibleLifecycleStrategy;

    NavigationSceneManagerV1(NavigationScene scene) {
        this.mNavigationScene = scene;
        this.mNavigationListener = scene;
    }

    public void saveToBundle(Bundle bundle) {
        this.mBackStackList.saveToBundle(bundle);

        ArrayList<Bundle> bundleList = new ArrayList<>();
        List<Record> recordList = this.mBackStackList.getCurrentRecordList();
        for (Record record : recordList) {
            Scene scene = record.mScene;
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

    public void restoreFromBundle(Context context, Bundle bundle, SceneComponentFactory rootSceneComponentFactory) {
        this.mBackStackList.restoreFromBundle(context, bundle, rootSceneComponentFactory);
        ArrayList<Bundle> bundleList = bundle.getParcelableArrayList(ParcelConstants.KEY_NAVIGATION_SCENE_MANAGER_TAG);

        List<Record> recordList = this.mBackStackList.getCurrentRecordList();
        for (int i = 0; i <= recordList.size() - 1; i++) {
            Record record = recordList.get(i);
            Bundle sceneBundle = bundleList.get(i);
            moveState(this.mNavigationScene, record.mScene, State.ACTIVITY_CREATED, sceneBundle, false, null);
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
        if (canExecuteNavigationStackOperation()) {
            /**
             * when current Handler Message is executing a NavigationScene navigation stack operation or GroupScene operation,
             * all the following navigation stack operations need to post at next Handler Message by Handler.post
             *
             * when there is a navigation stack operation waiting to be executed by Handler.post, all the following stack operations
             * must be scheduled with Handler.post too to make sure navigation order is correct
             */
            if (mIsNavigationStateChangeInProgress.size() > 0 || mCurrentScheduledStackOperationCount > 0) {
                Runnable task = new Runnable() {
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
                            SceneTrace.endSection();
                        } else {
                            mPendingActionList.addLast(operation);
                            mLastPendingActionListItemTimestamp = System.currentTimeMillis();
                        }
                    }
                };
                mCurrentScheduledStackOperationCount++;
                mHandler.postAsyncIfNeeded(task);
            } else {
                SceneTrace.beginSection(TRACE_EXECUTE_OPERATION_TAG);
                String suppressTag = beginSuppressStackOperation("NavigationManager execute operation directly");
                executeOperationSafely(operation, EMPTY_RUNNABLE);
                endSuppressStackOperation(suppressTag);
                SceneTrace.endSection();
            }
        } else {
            /**
             * navigation stack operation can't be executed before NavigationScene's state is State.ACTIVITY_CREATED, otherwise
             * animation can't be execute without view
             */
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

    public void dispatchCurrentChildState(State state) {
        String suppressTag = beginSuppressStackOperation("NavigationManager dispatchCurrentChildState");
        executeOperationSafely(new SyncCurrentSceneStateOperation(state), EMPTY_RUNNABLE);
        endSuppressStackOperation(suppressTag);
    }

    public void dispatchChildrenState(State state, boolean reverseOrder) {
        String suppressTag = beginSuppressStackOperation("NavigationManager dispatchChildrenState");
        executeOperationSafely(new SyncAllSceneStateOperation(state, reverseOrder), EMPTY_RUNNABLE);
        endSuppressStackOperation(suppressTag);
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
        if (mActivityCompatibleLifecycleStrategyEnabled && popOptions.isUseActivityCompatibleLifecycle()) {
            scheduleToNextUIThreadLoop(new PopOptionActivityCompatibleLifecycleOperation(popOptions));
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

    public void push(@NonNull final Scene scene, @NonNull PushOptions pushOptions) {
        if (scene == null) {
            throw new NullPointerException("scene can't be null");
        }
        LoggerManager.getInstance().i(TAG, "push " + scene.toString());
        scheduleToNextUIThreadLoop(new PushOptionOperation(scene, pushOptions));
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
        LoggerManager.getInstance().i(TAG, "executePendingOperation");

        SceneTrace.beginSection(TRACE_EXECUTE_PENDING_OPERATION_TAG);
        /*
         * Only the last one need to do the transition animation, the previous doesn't.
         * If not, it is easy to see that the jump animation of SchemaActivity not be executed,
         * as SchemaActivity is usually a translucent Activity cover over the other Activity.
         *
         * If it is over 800ms, it will not be animated also.
         */
        boolean animationTimeout = System.currentTimeMillis() - mLastPendingActionListItemTimestamp > 800;
        List<Operation> copy = new ArrayList<>(this.mPendingActionList);
        for (int i = 0; i < copy.size(); i++) {
            Operation currentOperation = copy.get(i);
            this.mDisableNavigationAnimation = animationTimeout | (i < copy.size() - 1);
            String suppressTag = beginSuppressStackOperation("NavigationManager executePendingOperation");
            executeOperationSafely(currentOperation, EMPTY_RUNNABLE);
            endSuppressStackOperation(suppressTag);
            this.mDisableNavigationAnimation = false;
        }
        this.mPendingActionList.removeAll(copy);
        if (this.mPendingActionList.size() > 0) {
            throw new IllegalStateException("why mPendingActionList still have item?");
        }
        this.mLastPendingActionListItemTimestamp = -1L;
        SceneTrace.endSection();
    }

    public boolean canPop() {
        return this.mBackStackList.canPop();
    }

    private void restoreActivityStatus(ActivityStatusRecord statusRecord) {
        Activity activity = mNavigationScene.getActivity();
        statusRecord.restore(activity);
    }

    public Scene getCurrentScene() {
        Record record = mBackStackList.getCurrentRecord();
        if (record != null) {
            return record.mScene;
        } else {
            return null;
        }
    }

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

    private static void moveState(@NonNull NavigationScene navigationScene,
                                  @NonNull Scene scene, @NonNull State to,
                                  @Nullable Bundle bundle,
                                  boolean causedByActivityLifeCycle,
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
                        containerView.addView(scene.getView());
                    }
                    scene.getView().setVisibility(View.GONE);
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                case VIEW_CREATED:
                    scene.dispatchActivityCreated(bundle);
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                case ACTIVITY_CREATED:
                    scene.getView().setVisibility(View.VISIBLE);
                    scene.dispatchStart();
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                case STARTED:
                    scene.dispatchResume();
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        } else {
            switch (currentState) {
                case RESUMED:
                    scene.dispatchPause();
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                case STARTED:
                    scene.dispatchStop();
                    if (!causedByActivityLifeCycle) {
                        scene.getView().setVisibility(View.GONE);
                    }
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                case ACTIVITY_CREATED:
                    if (to == State.VIEW_CREATED) {
                        throw new IllegalArgumentException("cant switch state ACTIVITY_CREATED to VIEW_CREATED");
                    }
                    //continue
                case VIEW_CREATED:
                    View view = scene.getView();
                    scene.dispatchDestroyView();
                    if (!causedByActivityLifeCycle) {
                        Utility.removeFromParentView(view);
                    }
                    scene.dispatchDestroy();
                    scene.dispatchDetachScene();
                    scene.dispatchDetachActivity();
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                default:
                    throw new SceneInternalException("unreachable state case " + currentState.getName());
            }
        }
    }

    private interface Operation {
        void execute(Runnable operationEndAction);
    }

    //avoid exceptions being caught externally
    private void executeOperationSafely(final Operation operation, final Runnable operationEndAction) {
        try {
            operation.execute(operationEndAction);
        } catch (final Throwable throwable) {
            mHandler.post(new Runnable() {
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

    private class PopOptionActivityCompatibleLifecycleOperation implements Operation {
        private final PopOptions mPopOptions;

        private PopOptionActivityCompatibleLifecycleOperation(PopOptions popOptions) {
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
                new PopCountActivityCompatibleLifecycleOperation(mPopOptions.getNavigationAnimationExecutor(), count).execute(operationEndAction);
            } else {
                new PopActivityCompatibleLifecycleOperation(mPopOptions.getNavigationAnimationExecutor()).execute(operationEndAction);
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

        private PopCountOperation(NavigationAnimationExecutor animationFactory, int popCount) {
            this.animationFactory = animationFactory;
            this.popCount = popCount;
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
                Scene scene = record.mScene;
                moveState(mNavigationScene, scene, State.NONE, null, false, null);
                mBackStackList.remove(record);
                // If it is a reusable Scene, save it
                if (record != currentRecord && scene instanceof ReuseGroupScene) {
                    mNavigationScene.addToReusePool((ReuseGroupScene) scene);
                }
            }

            final Scene dstScene = returnRecord.mScene;
            final boolean isNavigationSceneInAnimationState = mNavigationScene.getState().value >= State.STARTED.value;
            final State dstState = mNavigationScene.getState();

            moveState(mNavigationScene, dstScene, dstState, null, false, null);
            // Ensure that the requesting Scene is correct
            if (currentRecord.mPushResultCallback != null) {
                currentRecord.mPushResultCallback.onResult(currentRecord.mPushResult);
            }

            /*
             * In case of multiple translucent overlays of an opaque Scene,
             * after returning, it is necessary to set the previous translucent Scene to STARTED
             */
            if (returnRecord.mIsTranslucent) {
                final List<Record> currentRecordList = mBackStackList.getCurrentRecordList();
                if (currentRecordList.size() > 1) {
                    for (int i = currentRecordList.size() - 2; i >= 0; i--) {
                        Record record = currentRecordList.get(i);
                        moveState(mNavigationScene, record.mScene, findMinState(mNavigationScene.getState(), State.STARTED), null, false, null);
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
                AnimatorUtility.bringToFrontIfNeeded(animationContainer);
                navigationAnimationExecutor.setAnimationViewGroup(animationContainer);

                final CancellationSignalList cancellationSignalList = new CancellationSignalList();
                final Runnable endAction = new Runnable() {
                    @Override
                    public void run() {
                        mCancellationSignalManager.remove(cancellationSignalList);
                        if (currentRecord.mScene instanceof ReuseGroupScene) {
                            mNavigationScene.addToReusePool((ReuseGroupScene) currentRecord.mScene);
                        }
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
                if (currentRecord.mScene instanceof ReuseGroupScene) {
                    mNavigationScene.addToReusePool((ReuseGroupScene) currentRecord.mScene);
                }
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

    /**
     *  A -> B, then B return to A
     *  A onPause -> B onStart -> B onResume -> A onStop -> A onDestroyView
     */
    private class PopCountActivityCompatibleLifecycleOperation implements Operation {
        private final NavigationAnimationExecutor animationFactory;
        private final int popCount;

        private PopCountActivityCompatibleLifecycleOperation(NavigationAnimationExecutor animationFactory, int popCount) {
            this.animationFactory = animationFactory;
            this.popCount = popCount;
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
                    new PopCountActivityCompatibleLifecycleOperation(animationFactory, recordList.size() - 1).execute(EMPTY_RUNNABLE);
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

            Runnable actionAfterPopped = null;

            /*
             * The practice here should be to remove those Scenes in the middle,
             * then animate the two Scenes.
             */
            for (final Record record : destroyRecordList) {
                Scene scene = record.mScene;
                moveState(mNavigationScene, scene, State.STARTED, null, false, null);
            }

            actionAfterPopped = new Runnable() {
                @Override
                public void run() {
                    for (final Record record : destroyRecordList) {
                        Scene scene = record.mScene;
                        moveState(mNavigationScene, scene, State.NONE, null, false, null);
                        mBackStackList.remove(record);
                        // If it is a reusable Scene, save it
                        if (record != currentRecord && scene instanceof ReuseGroupScene) {
                            mNavigationScene.addToReusePool((ReuseGroupScene) scene);
                        }
                    }
                }
            };

            final Scene dstScene = returnRecord.mScene;
            final boolean isNavigationSceneInAnimationState = mNavigationScene.getState().value >= State.STARTED.value;
            final State dstState = mNavigationScene.getState();

            moveState(mNavigationScene, dstScene, dstState, null, false, null);
            // Ensure that the requesting Scene is correct
            if (currentRecord.mPushResultCallback != null) {
                currentRecord.mPushResultCallback.onResult(currentRecord.mPushResult);
            }

            /*
             * In case of multiple translucent overlays of an opaque Scene,
             * after returning, it is necessary to set the previous translucent Scene to STARTED
             */
            if (returnRecord.mIsTranslucent) {
                final List<Record> currentRecordList = mBackStackList.getCurrentRecordList();
                if (currentRecordList.size() > 1) {
                    int index = currentRecordList.indexOf(returnRecord);
                    if (index > 0) {
                        for (int i = index - 1; i >= 0; i--) {
                            Record record = currentRecordList.get(i);
                            moveState(mNavigationScene, record.mScene, findMinState(mNavigationScene.getState(), State.STARTED), null, false, null);
                            if (!record.mIsTranslucent) {
                                break;
                            }
                        }
                    }
                }
            }

            if (actionAfterPopped != null) {
                actionAfterPopped.run();
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
                AnimatorUtility.bringToFrontIfNeeded(animationContainer);
                navigationAnimationExecutor.setAnimationViewGroup(animationContainer);

                final CancellationSignalList cancellationSignalList = new CancellationSignalList();
                final Runnable endAction = new Runnable() {
                    @Override
                    public void run() {
                        mCancellationSignalManager.remove(cancellationSignalList);
                        if (currentRecord.mScene instanceof ReuseGroupScene) {
                            mNavigationScene.addToReusePool((ReuseGroupScene) currentRecord.mScene);
                        }
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
                if (currentRecord.mScene instanceof ReuseGroupScene) {
                    mNavigationScene.addToReusePool((ReuseGroupScene) currentRecord.mScene);
                }
                operationEndAction.run();
            }
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
                moveState(mNavigationScene, tmpRecord.mScene, dstState, null, false, null);
            }

            operationEndAction.run();
        }
    }

    public void cancelCurrentRunningAnimation() {
        mCancellationSignalManager.cancelAllRunningAnimationExecutor();
        InteractionNavigationPopAnimationFactory.cancelAllRunningInteractionAnimation();
    }

    private static class CancellationSignalManager {
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

        private void add(CancellationSignalList cancellationSignalList) {
            this.cancelableList.add(cancellationSignalList);
        }

        private void remove(CancellationSignalList cancellationSignalList) {
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
                        moveState(mNavigationScene, belowRecord.mScene, sceneState, null, false, null);
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

    private class PopActivityCompatibleLifecycleOperation implements Operation {
        private final NavigationAnimationExecutor animationFactory;

        private PopActivityCompatibleLifecycleOperation(NavigationAnimationExecutor animationFactory) {
            this.animationFactory = animationFactory;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            new PopCountActivityCompatibleLifecycleOperation(animationFactory, 1).execute(operationEndAction);
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

    private static State findMinState(State left, State right) {
        if (left.value > right.value) {
            return right;
        } else {
            return left;
        }
    }

    private boolean canExecuteNavigationStackOperation() {
        return mNavigationScene.getState().value >= State.ACTIVITY_CREATED.value;
    }

    private class PushOptionOperation implements Operation {
        private final Scene scene;
        private final PushOptions pushOptions;
        private final boolean isSceneTranslucent;

        private PushOptionOperation(Scene scene, PushOptions pushOptions) {
            this.scene = scene;
            this.pushOptions = pushOptions;
            this.isSceneTranslucent = pushOptions.isIsTranslucent() || scene instanceof SceneTranslucent;
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
            if (this.scene.getParentScene() != null) {
                if (this.scene.getParentScene() == mNavigationScene) {
                    operationEndAction.run();
                    return;
                }
                throw new IllegalArgumentException("Scene already has a parent, parent " + scene.getParentScene());
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

                    AnimatorUtility.bringToFrontIfNeeded(mNavigationScene.getSceneContainer());//保证Z轴正确
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

    private class SyncCurrentSceneStateOperation implements Operation {
        private final State state;

        private SyncCurrentSceneStateOperation(State state) {
            this.state = state;
        }

        @Override
        public void execute(Runnable operationEndAction) {
            if (getCurrentRecord() == null) {
                operationEndAction.run();
                return;
            }

            // Translucent processing ensures that the correct method can be executed
            List<Record> recordList = mBackStackList.getCurrentRecordList();
            State targetState = this.state;
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (i == recordList.size() - 1) {
                    NavigationSceneManagerV1.moveState(mNavigationScene, record.mScene, targetState, null, true, operationEndAction);
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

                    NavigationSceneManagerV1.moveState(mNavigationScene, record.mScene, fixDstState, null, true, operationEndAction);
                    if (!record.mIsTranslucent) {
                        break;
                    }
                }
            }

            operationEndAction.run();
        }
    }

    private class SyncAllSceneStateOperation implements Operation {
        private final State state;
        private final boolean reverseOrder;

        private SyncAllSceneStateOperation(State state, boolean reverseOrder) {
            this.state = state;
            this.reverseOrder = reverseOrder;
        }

        @Override
        public void execute(Runnable operationEndAction) {
            if (getCurrentRecord() == null) {
                operationEndAction.run();
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
                moveState(mNavigationScene, scene, state, null, true, null);
            }
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
    public void forceExecutePendingNavigationOperation() {

    }

    public void recycleInvisibleScenes() {

    }
}
