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
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.lifecycle.Lifecycle.State.DESTROYED;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.collection.LruCache;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.bytedance.scene.ActivityCompatibilityUtility;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.SceneParent;
import com.bytedance.scene.State;
import com.bytedance.scene.SuppressOperationAware;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.animatorexecutor.Android8DefaultSceneAnimatorExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.interfaces.PermissionResultCallback;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.queue.NavigationRunnable;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.DispatchWindowInsetsListener;
import com.bytedance.scene.utlity.Experimental;
import com.bytedance.scene.utlity.MemoryMonitor;
import com.bytedance.scene.utlity.NonNullPair;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.SoftInputUtility;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;
import com.bytedance.scene.view.AnimationContainerLayout;
import com.bytedance.scene.view.BlockGestureView;
import com.bytedance.scene.view.NavigationFrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 7/30/18.
 * <p>
 * (NavigationScene cannot be inherited)
 * <p>
 * When entering:
 * <p>
 *                                                                                         +-------------------------+
 *                                                                                         |      Child onStart      |
 *                                                                                         +-------------------------+
 *                                                                                           ^
 *                                                                                           | sync
 *                                                                                           |
 * +--------------------------+     +--------------------+     +---------------------+     +-------------------------+     +-----------------+     +----------------+
 * | Parent onActivityCreated | --> |       sync_1       | --> |   Parent onStart    | --> |         sync_2          | --> | Parent onResume | --> |     sync_3     | ---
 * +--------------------------+     +--------------------+     +---------------------+     +-------------------------+     +-----------------+     +----------------+
 *                                    |                                                                                                              |
 *                                    | sync                                                                                                         | sync
 *                                    v                                                                                                              v
 *                                  +--------------------+     +---------------------+     +-------------------------+                             +----------------+
 *                                  | Child onCreateView | --> | Child onViewCreated | --> | Child onActivityCreated |                             | Child onResume |
 *                                  +--------------------+     +---------------------+     +-------------------------+                             +----------------+
 * <p>
 *
 *
 * When exiting:
 * <p>
 * +---------------+     +----------------+     +--------------+     +---------------+     +---------------------+     +----------------------+
 * | Child onPause | --> |     sync_1     | --> | Child onStop | --> |    sync_2     | --> | Child onDestroyView | --> |        sync_3        | ---
 * +---------------+     +----------------+     +--------------+     +---------------+     +---------------------+     +----------------------+
 *                         |                                           |                                                 |
 *                         | sync                                      | sync                                            | sync
 *                         v                                           v                                                 v
 *                       +----------------+                          +---------------+                                 +----------------------+
 *                       | Parent onPause |                          | Parent onStop |                                 | Parent onDestroyView |
 *                       +----------------+                          +---------------+                                 +----------------------+
 *
 */
public final class NavigationScene extends Scene implements NavigationListener, SceneParent, SuppressOperationAware {
    private static final String TAG = "NavigationScene";
    private static final String KEY_NAVIGATION_SCENE_SUPPORT_RESTORE_ARGUMENT = "bd-scene-navigation:support_restore";

    private boolean mSupportRestore = true;//default support restore
    SceneComponentFactory mRootSceneComponentFactory;   // Use this when destroying recovery
    NavigationSceneOptions mNavigationSceneOptions;

    INavigationManager mNavigationSceneManager;
    private FrameLayout mSceneContainer;
    private FrameLayout mAnimationContainer;
    private BlockGestureView mBlockGestureView;
    private FrameLayout mOutsideView = null;
    private boolean mViewOwnedByOutside = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private NavigationAnimationExecutor mDefaultNavigationAnimationExecutor = null;
    private boolean mDefaultNavigationAnimatorSetByUser = false;
    private final List<InteractionNavigationPopAnimationFactory.InteractionCallback> mInteractionListenerList = new ArrayList<>();

    private final LruCache<Class<? extends Scene>, ReuseGroupScene> mLruCache = new LruCache<>(3);

    private final List<NavigationListener> mNavigationListenerList = new ArrayList<>();
    private final List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> mLifecycleCallbacks = new ArrayList<>();
    private final List<NavigationAnimationCallback> mNavigationAnimationCallbackList = new ArrayList<>();

    @Nullable
    private MemoryMonitor mMemoryMonitor;
    private final Runnable mRecycleInvisibleScenesJob = new Runnable() {
        @Override
        public void run() {
            recycleInvisibleScenes();
        }
    };

    private boolean mIsInitRootSceneOnCreate = false;

    private final boolean mSceneLifecycleCallbackObjectCreationOpt = SceneGlobalConfig.sceneLifecycleCallbackObjectCreationOpt;
    private boolean mWindowFocusChangedInstalled = false;
    private SceneWindowFocusChangedDispatcher mSceneWindowFocusChangedDispatcher = null;
    private boolean mLifecycleAndSavedStateRegistryEnabled = true;
    private boolean mRestoreStateInLifecycle = false;
    private int mConfigurationChangesAllowList = 0;

    @MainThread
    public void addNavigationListener(@NonNull final LifecycleOwner lifecycleOwner, @NonNull final NavigationListener listener) {
        ThreadUtility.checkUIThread();
        if (lifecycleOwner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        this.mNavigationListenerList.add(listener);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    lifecycleOwner.getLifecycle().removeObserver(this);
                    mNavigationListenerList.remove(listener);
                }
            }
        });
    }

    @MainThread
    public void removeNavigationListener(@NonNull NavigationListener listener) {
        ThreadUtility.checkUIThread();
        this.mNavigationListenerList.remove(listener);
    }

    @MainThread
    public void addOnBackPressedListener(@NonNull final LifecycleOwner lifecycleOwner, @NonNull final OnBackPressedListener onBackPressedListener) {
        ThreadUtility.checkUIThread();
        if (lifecycleOwner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        this.mNavigationSceneManager.addOnBackPressedListener(lifecycleOwner, onBackPressedListener);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    lifecycleOwner.getLifecycle().removeObserver(this);
                    mNavigationSceneManager.removeOnBackPressedListener(onBackPressedListener);
                }
            }
        });
    }

    @MainThread
    public void removeOnBackPressedListener(@NonNull OnBackPressedListener onBackPressedListener) {
        ThreadUtility.checkUIThread();
        this.mNavigationSceneManager.removeOnBackPressedListener(onBackPressedListener);
    }

    @MainThread
    public void addConfigurationChangedListener(@NonNull final LifecycleOwner lifecycleOwner, @NonNull final ConfigurationChangedListener configurationChangedListener) {
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }
        ActivityCompatibilityUtility.addConfigurationChangedListener(activity, lifecycleOwner, configurationChangedListener);
    }

    @NonNull
    public String getStackHistory() {
        return mNavigationSceneManager.getStackHistory();
    }

    @Nullable
    public Scene getCurrentScene() {
        if (this.mNavigationSceneManager == null) {
            return null;
        }
        return this.mNavigationSceneManager.getCurrentScene();
    }

    @NonNull
    @Override
    public List<Scene> getSceneList() {
        return mNavigationSceneManager.getCurrentSceneList();
    }

    public void setDefaultNavigationAnimationExecutor(@Nullable NavigationAnimationExecutor defaultNavigationAnimationExecutor) {
        this.mDefaultNavigationAnimatorSetByUser = true;
        this.mDefaultNavigationAnimationExecutor = defaultNavigationAnimationExecutor;
    }

    @Nullable
    public NavigationAnimationExecutor getDefaultNavigationAnimationExecutor() {
        if (this.mDefaultNavigationAnimationExecutor == null && !this.mDefaultNavigationAnimatorSetByUser) {
            this.mDefaultNavigationAnimationExecutor = new Android8DefaultSceneAnimatorExecutor();
        }
        return this.mDefaultNavigationAnimationExecutor;
    }

    public void setRootSceneComponentFactory(@Nullable SceneComponentFactory rootSceneComponentFactory) {
        this.mRootSceneComponentFactory = rootSceneComponentFactory;
    }

    @Override
    public void disableSupportRestore() {
        this.mSupportRestore = false;
    }

    @Override
    public boolean isSupportRestore() {
        return this.mSupportRestore;
    }

    public void setInitRootSceneOnCreate(boolean initRootSceneOnCreate) {
        this.mIsInitRootSceneOnCreate = initRootSceneOnCreate;
    }

    @RestrictTo(LIBRARY_GROUP)
    public boolean isFixOnResultTiming() {
        if (this.mNavigationSceneOptions != null) {
            float threshold = this.mNavigationSceneOptions.getAutoRecycleInvisibleSceneThreshold();
            return this.mNavigationSceneOptions.isFixOnResultTiming() || (threshold > 0 && threshold < 1);
        }
        return false;
    }

    @Override
    protected boolean isLifecycleAndSavedStateRegistryEnabled() {
        return this.mLifecycleAndSavedStateRegistryEnabled;
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void setLifecycleAndSavedStateRegistryEnabled(boolean enable) {
        this.mLifecycleAndSavedStateRegistryEnabled = enable;
    }

    private void createRootSceneIfNeeded(boolean usePushRoot) {
        String clazzName = mNavigationSceneOptions.getRootSceneClassName();
        Bundle arguments = mNavigationSceneOptions.getRootSceneArguments();

        Scene rootScene = null;
        if (this.mRootSceneComponentFactory != null) {
            ClassLoader classLoader = requireActivity().getClassLoader();
            rootScene = this.mRootSceneComponentFactory.instantiateScene(classLoader, clazzName, arguments);
            if (rootScene != null && rootScene.getParentScene() != null) {
                throw new IllegalArgumentException("SceneComponentFactory instantiateScene return Scene already has a parent");
            }
        }

        if (rootScene == null) {
            rootScene = SceneInstanceUtility.getInstanceFromClassName(requireActivity(), clazzName, arguments);
        }
        if (usePushRoot) {
            mNavigationSceneManager.pushRoot(rootScene);
        } else {
            //the root should not use post policy
            mNavigationSceneManager.push(rootScene, new PushOptions.Builder().setUsePost(false).build());
        }
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void addToReusePool(@NonNull ReuseGroupScene scene) {
        mLruCache.put(scene.getClass(), scene);
    }

    /**
     * @deprecated Use {@link #push(Scene)}.
     */
    @Deprecated
    public void push(@NonNull Class<? extends Scene> clazz) {
        push(clazz, null);
    }

    /**
     * @deprecated Use {@link #push(Scene)}.
     */
    @Deprecated
    public void push(@NonNull Class<? extends Scene> clazz, @Nullable Bundle argument) {
        push(clazz, argument, null);
    }

    /**
     * Push a new Scene.
     *
     * @see #pop()
     * @deprecated Use {@link #push(Scene, PushOptions)}.
     */
    @Deprecated
    public void push(@NonNull Class<? extends Scene> clazz, @Nullable Bundle argument, @Nullable PushOptions pushOptions) {
        Scene scene = null;
        if (ReuseGroupScene.class.isAssignableFrom(clazz)) {
            scene = mLruCache.get(clazz);
        }

        if (scene == null) {
            scene = SceneInstanceUtility.getInstanceFromClass(clazz, argument);
        } else {
            if (argument != null) {
                scene.setArguments(argument);
            }
        }

        pushInstance(scene, pushOptions);
    }

    public void push(@NonNull Scene scene) {
        pushInstance(scene, null);
    }

    /**
     * Navigate to a new Scene.
     */
    public void push(@NonNull Scene scene, @Nullable PushOptions pushOptions) {
        pushInstance(scene, pushOptions);
    }

    private void pushInstance(@NonNull Scene scene, @Nullable PushOptions pushOptions) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }

        if (scene.getParentScene() != null) {
            if (scene.getParentScene() == this) {
                throw new IllegalArgumentException("Scene is already pushed");
            }
            throw new IllegalArgumentException("Scene already has a parent, parent " + scene.getParentScene());
        }

        if (isSupportRestore() && scene.isSceneRestoreEnabled() && !SceneInstanceUtility.isConstructorMethodSupportRestore(scene)) {
            throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " must be a public class or public static class, " +
                    "and have only one parameterless constructor to be properly recreated from instance state.");
        }

        if (pushOptions == null) {
            pushOptions = new PushOptions.Builder().build();
        }

        hideSoftInputIfNeeded();
        mNavigationSceneManager.push(scene, pushOptions);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void recreate(@NonNull Scene scene) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }

        if (scene == null) {
            throw new NullPointerException("scene can't be null");
        }

        if (scene.getState() == State.NONE) {
            return;
        }

        if (scene.getParentScene() != this) {
            throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " parent is incorrect");
        }

        if (!scene.isSceneRestoreEnabled()) {
            throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " don't support restore, so it can't use recreate");
        }
        if (!SceneInstanceUtility.isConstructorMethodSupportRestore(scene)) {
            throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " must be a public class or public static class, " +
                    "and have only one parameterless constructor to be properly recreated.");
        }
        mNavigationSceneManager.recreate(scene);
    }

    private void hideSoftInputIfNeeded() {
        Scene currentScene = mNavigationSceneManager.getCurrentScene();
        if (currentScene != null) {
            SoftInputUtility.hideSoftInputFromWindow(currentScene.getView());
        }
    }

    public void setResult(@NonNull Scene scene, @Nullable Object result) {
        mNavigationSceneManager.setResult(scene, result);
    }

    public boolean onBackPressed() {
        return this.onBackPressed(null);
    }

    public boolean onBackPressed(@Nullable PopOptions popOptions) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return false;
        }
        if (mNavigationSceneManager.interceptOnBackPressed()) {
            return true;
        } else if (mNavigationSceneManager.canPop()) {
            if (popOptions != null) {
                pop(popOptions);
            } else {
                pop();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pop to previous Scene.
     *
     * @see #push(Class, Bundle, PushOptions)
     */
    public void pop() {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        mNavigationSceneManager.pop();
    }

    public void onConfigurationChanged(@NonNull Configuration configuration) {
        mNavigationSceneManager.onConfigurationChanged(configuration);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    @NonNull
    public String beginSuppressStackOperation(@NonNull String tagPrefix) {
        return mNavigationSceneManager.beginSuppressStackOperation(tagPrefix);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public void endSuppressStackOperation(@NonNull String suppressTag) {
        mNavigationSceneManager.endSuppressStackOperation(suppressTag);
    }

    /**
     * Pop() is asynchronous, it is possible that when Pop actually executes,
     * there is no Scene that can pop. Turn it out to the outside,
     * in case the Activity has intercepted onBackPressed.
     */
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void finishCurrentActivity() {
        requireActivity().onBackPressed();
    }

    public void pop(@NonNull PopOptions popOptions) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        mNavigationSceneManager.pop(popOptions);
    }

    public void popTo(@NonNull Class<? extends Scene> clazz) {
        popTo(clazz, null);
    }

    public void popTo(@NonNull Class<? extends Scene> clazz, NavigationAnimationExecutor animationFactory) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        mNavigationSceneManager.popTo(clazz, animationFactory);
    }

    public void popToRoot() {
        popToRoot(null);
    }

    public void popToRoot(NavigationAnimationExecutor animationFactory) {
        ThreadUtility.checkUIThread();
        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        mNavigationSceneManager.popToRoot(animationFactory);
    }

    public void remove(@NonNull Scene scene) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        if (mNavigationSceneManager.getCurrentScene() == scene) {
            hideSoftInputIfNeeded();
        }
        mNavigationSceneManager.remove(scene);
    }

    public void requestDisableTouchEvent(boolean disable) {
        if (this.mBlockGestureView == null && disable && this.mNavigationSceneOptions.getUseExtraViewToBlockGesture()) {
            this.mBlockGestureView = new BlockGestureView(requireSceneContext());
            this.mSceneContainer.addView(this.mBlockGestureView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        if (this.mBlockGestureView != null) {
            if (disable) {
                this.mBlockGestureView.setVisibility(View.VISIBLE);
                AnimatorUtility.adjustBlockGestureViewIndex(this.mBlockGestureView, true);
            } else {
                this.mBlockGestureView.setVisibility(View.GONE);
                AnimatorUtility.adjustBlockGestureViewIndex(this.mBlockGestureView, false);
            }
        }

        if (this.mNavigationSceneOptions.getOptimizedViewLayer()) {
            LoggerManager.getInstance().v(TAG, "optimizedViewLayer don't support requestDisableTouchEvent");
            return;
        }
        ((NavigationFrameLayout) getView()).setTouchEnabled(!disable);
    }

    public void setOutsideView(@NonNull FrameLayout outsideView) {
        if (getArguments() == null) {
            throw new IllegalStateException("NavigationScene don't have any arguments, you should invoke after setArguments");
        }
        if (!this.getNavigationSceneOptions().getReuseOutsideView()) {
            throw new IllegalStateException("You should invoke NavigationSceneOptions.setReuseOutsideView(true) at first");
        }
        if (getView() != null) {
            throw new IllegalStateException("NavigationScene have created its view, you should invoke before onCreateView");
        }
        if (outsideView == null) {
            throw new IllegalArgumentException("setOutsideView outsideView argument is null");
        }
        this.mOutsideView = outsideView;
        this.mViewOwnedByOutside = true;
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Experimental
    @Override
    public boolean isViewOwnedByOutside() {
        return this.mViewOwnedByOutside;
    }

    /**
     * Push animation priority:
     * 1.NavigationScene.overrideNavigationAnimationExecutor
     * 2.PushOptions.setAnimation
     * 3.NavigationScene.setDefaultNavigationAnimationExecutor
     * 4.default
     * <p>
     * Pop animation priority:
     * 1.PopOptions.setAnimation
     * 2.NavigationScene.overrideNavigationAnimationExecutor
     * 3.PushOptions.setAnimation
     * 4.NavigationScene.setDefaultNavigationAnimationExecutor
     * 5.default
     **/
    public void overrideNavigationAnimationExecutor(@NonNull Scene scene, @Nullable NavigationAnimationExecutor navigationAnimationExecutor) {
        ThreadUtility.checkUIThread();
        if (scene.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        Record record = mNavigationSceneManager.findRecordByScene(scene);
        if (record != null) {
            record.mNavigationAnimationExecutor = navigationAnimationExecutor;
        }
    }

    /**
     * {@link com.bytedance.scene.interfaces.PushOptions.Builder#setAnimation(NavigationAnimationExecutor)}
     * {@link #overrideNavigationAnimationExecutor(Scene, NavigationAnimationExecutor)}
     */
    @Nullable
    public NavigationAnimationExecutor getNavigationAnimationExecutor(@NonNull Scene scene) {
        Record record = mNavigationSceneManager.findRecordByScene(scene);
        if (record != null) {
            return record.mNavigationAnimationExecutor;
        } else {
            return null;
        }
    }

    /**
     * return a clone version of target Scene's cached Configuration
     * @param scene
     * @return
     */
    @Nullable
    public Configuration getConfiguration(@NonNull Scene scene) {
        Record record = mNavigationSceneManager.findRecordByScene(scene);
        if (record != null) {
            Configuration cachedConfiguration = record.mConfiguration;
            if (cachedConfiguration != null) {
                return new Configuration(cachedConfiguration);
            }
        }
        return null;
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public ViewGroup getSceneContainer() {
        return this.mSceneContainer;
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public ViewGroup getAnimationContainer() {
        if (this.mAnimationContainer == null) {
            if (mNavigationSceneOptions.getLazyLoadNavigationSceneUnnecessaryView()) {
                this.mAnimationContainer = addAnimationContainerToRootView((FrameLayout) getView(), requireSceneContext(), mNavigationSceneOptions.getMergeNavigationSceneView() || mNavigationSceneOptions.getOptimizedViewLayer(), mNavigationSceneOptions.getOnlyDispatchToTopSceneWindowInsets());
            } else {
                throw new IllegalStateException("Animation Container is null");
            }
        }
        return this.mAnimationContainer;
    }

    @NonNull
    public NavigationSceneOptions getNavigationSceneOptions(){
        if (this.mNavigationSceneOptions == null) {
            throw new IllegalStateException("NavigationSceneOptions is null, current NavigationScene state " + getState().getName());
        }
        return this.mNavigationSceneOptions;
    }

    @Override
    public void navigationChange(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
        List<NavigationListener> listenerList = new ArrayList<>(mNavigationListenerList);
        for (NavigationListener listener : listenerList) {
            listener.navigationChange(from, to, isPush);
        }
    }

    public void addNavigationAnimationCallback(@NonNull NavigationAnimationCallback callback) {
        ThreadUtility.checkUIThread();
        if (mNavigationAnimationCallbackList.contains(callback)) {
            throw new IllegalArgumentException("NavigationAnimationCallback already exists");
        }
        mNavigationAnimationCallbackList.add(callback);
    }

    public void removeNavigationAnimationCallback(@NonNull NavigationAnimationCallback callback) {
        ThreadUtility.checkUIThread();
        mNavigationAnimationCallbackList.remove(callback);
    }

    void notifyNavigationAnimationEnd(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
        List<NavigationAnimationCallback> callbacks = new ArrayList<>(mNavigationAnimationCallbackList);
        for (NavigationAnimationCallback callback : callbacks) {
            callback.onAnimationEnd(from, to, isPush);
        }
    }

    @Override
    public final void dispatchAttachScene(@Nullable Scene parentScene) {
        super.dispatchAttachScene(parentScene);
        if (parentScene == null) {
            //ignore
        } else if (parentScene instanceof SceneParent) {
            SceneParent sceneParent = (SceneParent) parentScene;
            if (!sceneParent.isSupportRestore()) {
                disableSupportRestore();
            }
        } else {
            throw new SceneInternalException("unknown parent Scene type " + parentScene.getClass());
        }
    }

    @Override
    public void onAttach() {
        super.onAttach();
    }

    @Override
    public void dispatchCreate(@Nullable Bundle savedInstanceState) {
        super.dispatchCreate(savedInstanceState);
        if (mIsInitRootSceneOnCreate) {
            if (!isSeparateCreateFromCreateView()) {
                throw new IllegalStateException("separateCreateFromCreateView must be set to true when initRootSceneOnCreate");
            }

            // dispatch children state
            if (savedInstanceState != null && isSupportRestore()) {
                this.mNavigationSceneManager.restoreFromBundle(requireActivity(), savedInstanceState, this.mRootSceneComponentFactory, getState());
            } else {
                createRootSceneIfNeeded(true);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalArgumentException("NavigationScene need NavigationSceneOptions bundle");
        }
        this.mNavigationSceneOptions = NavigationSceneOptions.fromBundle(getArguments());
        this.mNavigationSceneManager = new NavigationSceneManager(this);

        if (savedInstanceState != null) {
            boolean supportRestore = savedInstanceState.getBoolean(KEY_NAVIGATION_SCENE_SUPPORT_RESTORE_ARGUMENT, isSupportRestore());
            if (!supportRestore) {
                disableSupportRestore();
            }
        }

        float autoRecycleInvisibleSceneThreshold = this.mNavigationSceneOptions.getAutoRecycleInvisibleSceneThreshold();
        if (autoRecycleInvisibleSceneThreshold > 0F && autoRecycleInvisibleSceneThreshold < 1F) {
            mMemoryMonitor = new MemoryMonitor(autoRecycleInvisibleSceneThreshold, new Runnable() {
                @Override
                public void run() {
                    mHandler.post(mRecycleInvisibleScenesJob);
                }
            });
            mMemoryMonitor.start();
        }
    }

    @Override
    public void dispatchCreateView(@Nullable Bundle savedInstanceState, @NonNull ViewGroup container) {
        super.dispatchCreateView(savedInstanceState, container);
        if (mIsInitRootSceneOnCreate) {
            // causeByActivityLifecycle = false to ensure Scene view is added to NavigationScene container
            if (savedInstanceState != null && isSupportRestore()) {
                this.mNavigationSceneManager.restoreChildrenSceneState(savedInstanceState, State.VIEW_CREATED, false);
            } else {
                dispatchChildrenState(State.VIEW_CREATED, null, false, false);
            }
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mNavigationSceneOptions.getReuseOutsideView()) {
            //todo achieve requestDisableTouchEvent ability
            FrameLayout frameLayout = this.mOutsideView;
            if (frameLayout == null) {
                throw new IllegalArgumentException("Please invoke setOutsideView at first");
            }
            frameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener(mNavigationSceneOptions.getOnlyDispatchToTopSceneWindowInsets()));

            mSceneContainer = frameLayout;

            if (mNavigationSceneOptions.getLazyLoadNavigationSceneUnnecessaryView()) {
                //skip
            } else {
                mAnimationContainer = addAnimationContainerToRootView(frameLayout, requireSceneContext(), true, mNavigationSceneOptions.getOnlyDispatchToTopSceneWindowInsets());
            }
            if (mNavigationSceneOptions.drawWindowBackground()) {
                ViewCompat.setBackground(frameLayout, Utility.getWindowBackground(requireSceneContext()));
            }
            return frameLayout;
        } else if (this.mNavigationSceneOptions.getOptimizedViewLayer()) {
            //todo achieve requestDisableTouchEvent ability
            FrameLayout frameLayout = new FrameLayout(requireSceneContext());
            frameLayout.setSaveFromParentEnabled(false);
            frameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener(mNavigationSceneOptions.getOnlyDispatchToTopSceneWindowInsets()));
            frameLayout.setId(R.id.navigation_scene_content);

            mSceneContainer = frameLayout;

            if (mNavigationSceneOptions.getLazyLoadNavigationSceneUnnecessaryView()) {
                //skip
            } else {
                mAnimationContainer = addAnimationContainerToRootView(frameLayout, requireSceneContext(), true, mNavigationSceneOptions.getOnlyDispatchToTopSceneWindowInsets());
            }
            if (mNavigationSceneOptions.drawWindowBackground()) {
                ViewCompat.setBackground(frameLayout, Utility.getWindowBackground(requireSceneContext()));
            }
            return frameLayout;
        } else {
            NavigationFrameLayout frameLayout = new NavigationFrameLayout(requireSceneContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                frameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
            }
            frameLayout.setId(R.id.navigation_scene_content);

            if (mNavigationSceneOptions.getMergeNavigationSceneView()) {
                mSceneContainer = frameLayout;
                frameLayout.setChildrenDrawingOrderEnabled(true);
            } else {
                mSceneContainer = new FrameLayout(requireSceneContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mSceneContainer.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener(mNavigationSceneOptions.getOnlyDispatchToTopSceneWindowInsets()));
                }
                frameLayout.addView(mSceneContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            if (mNavigationSceneOptions.getLazyLoadNavigationSceneUnnecessaryView()) {
                //skip
            } else {
                mAnimationContainer = addAnimationContainerToRootView(frameLayout, requireSceneContext(), mNavigationSceneOptions.getMergeNavigationSceneView(), mNavigationSceneOptions.getOnlyDispatchToTopSceneWindowInsets());
            }
            if (mNavigationSceneOptions.drawWindowBackground()) {
                ViewCompat.setBackground(frameLayout, Utility.getWindowBackground(requireSceneContext()));
            }
            return frameLayout;
        }
    }

    private static FrameLayout addAnimationContainerToRootView(FrameLayout frameLayout, Context context, boolean addToBack, boolean markAnimationView) {
        AnimationContainerLayout animationContainerLayout = new AnimationContainerLayout(context);
        if (markAnimationView) {
            DispatchWindowInsetsListener.markAnimationView(animationContainerLayout);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animationContainerLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        int index = -1;
        if (addToBack) {
            index = 0;
        }
        frameLayout.addView(animationContainerLayout, index, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return animationContainerLayout;
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public void dispatchActivityCreated(@Nullable Bundle savedInstanceState) {
        super.dispatchActivityCreated(savedInstanceState);
        if (mIsInitRootSceneOnCreate) {
            if (savedInstanceState != null && isSupportRestore()) {
                this.mNavigationSceneManager.restoreChildrenSceneState(savedInstanceState, State.ACTIVITY_CREATED, true);
            } else {
                dispatchChildrenState(State.ACTIVITY_CREATED, null, false, true);
            }
        }
        this.mNavigationSceneManager.executePendingOperation();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mIsInitRootSceneOnCreate) {
            // child scene state was synced to ACTIVITY_CREATED here
            if (savedInstanceState != null && isSupportRestore()) {
                this.mNavigationSceneManager.restoreFromBundle(requireActivity(), savedInstanceState, this.mRootSceneComponentFactory, State.ACTIVITY_CREATED);
            } else {
                createRootSceneIfNeeded(false);
            }
        }

        if (getParentScene() != null) {
            NavigationScene parentSceneNavigation = NavigationSceneGetter.getNavigationScene(this);
            if (parentSceneNavigation != null) {
                parentSceneNavigation.addOnBackPressedListener(this, new OnBackPressedListener() {
                    @Override
                    public boolean onBackPressed() {
                        return NavigationScene.this.onBackPressed();
                    }
                });
            }
        }
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public void dispatchStart() {
        super.dispatchStart();
        dispatchCurrentChildState(State.STARTED);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public void dispatchResume() {
        super.dispatchResume();
        dispatchCurrentChildState(State.RESUMED);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public void dispatchPause() {
        dispatchCurrentChildState(State.STARTED);
        super.dispatchPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState.containsKey(KEY_NAVIGATION_SCENE_SUPPORT_RESTORE_ARGUMENT)) {
            throw new IllegalArgumentException("outState already contains key " + KEY_NAVIGATION_SCENE_SUPPORT_RESTORE_ARGUMENT);
        } else {
            outState.putBoolean(KEY_NAVIGATION_SCENE_SUPPORT_RESTORE_ARGUMENT, isSupportRestore());
            if (isSupportRestore()) {
                this.mNavigationSceneManager.saveToBundle(outState);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        /*
         * Terminate the animation to avoid the possibility of going after the onDestroyView
         * Otherwise, if the animation is still executing, it will cause a crash or memory leak.
         */
        this.mNavigationSceneManager.cancelCurrentRunningAnimation();
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public void dispatchStop() {
        dispatchCurrentChildState(State.ACTIVITY_CREATED);
        super.dispatchStop();
    }

    @RestrictTo(LIBRARY)
    @Override
    public void dispatchDestroyView() {
        if (isSeparateCreateFromCreateView()) {
            dispatchChildrenState(State.CREATED, State.NONE, true, true);
        }
        super.dispatchDestroyView();
    }

    @Override
    public void onDestroyView() {
        if (mMemoryMonitor != null) {
            mMemoryMonitor.stop();
            mHandler.removeCallbacks(mRecycleInvisibleScenesJob);
        }
        // TODO should be move to dispatchDestroyView
        if (!isSeparateCreateFromCreateView()) {
            dispatchChildrenState(State.NONE, null, true, true);
        }
        this.mOutsideView = null;
        this.mViewOwnedByOutside = false;
        super.onDestroyView();
    }

    @RestrictTo(LIBRARY)
    @Override
    public void dispatchDestroy() {
        if (isSeparateCreateFromCreateView()) {
            dispatchChildrenState(State.NONE, null, true, true);
        }
        super.dispatchDestroy();
    }

    private void dispatchCurrentChildState(@NonNull State state) {
        if (getState().value < State.VIEW_CREATED.value) {
            throw new IllegalArgumentException("dispatchCurrentChildState can only call when state is VIEW_CREATED, ACTIVITY_CREATED, STARTED, RESUMED");
        }
        mNavigationSceneManager.dispatchCurrentChildState(state);
    }

    /**
     * Create or Destroy operation need to synchronize all children
     *
     * @param causeByActivityLifecycle if set to false, child Scene view will be attached to NavigationScene when onCreateView,
     *                                 gone when onStop and removed from NavigationScene when onDestroyView
     */
    private void dispatchChildrenState(@NonNull State state, @Nullable State nextStageStateHint, boolean reverseOrder, boolean causeByActivityLifecycle) {
        mNavigationSceneManager.dispatchChildrenState(state, nextStageStateHint, reverseOrder, causeByActivityLifecycle);
    }

    Record findRecordByScene(Scene scene) {
        return mNavigationSceneManager.findRecordByScene(scene);
    }

    public enum TranslucentOption {
        /**
         * Scene will be translucent
         */
        TO_TRANSLUCENT,
        /**
         * Scene will be not translucent
         */
        FROM_TRANSLUCENT
    }

    /**
     * If a visible Scene is translucent, the Scene who under it will be visible too
     * @param scene
     * @param option
     */
    public void changeSceneTranslucent(@NonNull Scene scene, @NonNull TranslucentOption option) {
        ThreadUtility.checkUIThread();
        Record record = mNavigationSceneManager.findRecordByScene(scene);
        if (record == null) {
            throw new IllegalArgumentException("Scene(" + scene + ") not found");
        }
        mNavigationSceneManager.changeTranslucent(scene, option == TranslucentOption.TO_TRANSLUCENT);
    }

    public boolean isTranslucent(@NonNull Scene scene) {
        Record record = mNavigationSceneManager.findRecordByScene(scene);
        if (record == null) {
            throw new IllegalArgumentException("Scene(" + scene + ") not found");
        }
        return record.mIsTranslucent;
    }

    public void convertBackgroundToDefault() {
        if (mNavigationSceneOptions.drawWindowBackground()) {
            ViewCompat.setBackground(getView(), Utility.getWindowBackground(requireSceneContext()));
        }
    }

    public void startActivityForResult(@NonNull Intent intent, int requestCode, ActivityResultCallback resultCallback) {
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }
        ActivityCompatibilityUtility.startActivityForResult(activity, this, intent, requestCode, resultCallback);
    }

    public void startActivity(@NonNull Intent intent) {
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(@NonNull String[] permissions, int requestCode, @NonNull PermissionResultCallback resultCallback) {
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }
        ActivityCompatibilityUtility.requestPermissions(activity, this, permissions, requestCode, resultCallback);
    }

    public boolean pop(@NonNull final InteractionNavigationPopAnimationFactory animationFactory) {
        ThreadUtility.checkUIThread();

        animationFactory.setCallback(mInteractionCallback);
        boolean result = mNavigationSceneManager.pop(animationFactory);
        if (!result) {
            //clear callback
            animationFactory.setCallback(null);
        }
        return result;
    }

    public void forceExecutePendingNavigationOperation() {
        ThreadUtility.checkUIThread();
        mNavigationSceneManager.forceExecutePendingNavigationOperation();
    }

    /**
     * recycle invisible Scenes to save memory, they will be restored when user return to them via back action
     * If Scene doesn't support restore({@link Scene#isSceneRestoreEnabled == false}), it will not reappear
     */
    public void recycleInvisibleScenes() {
        ThreadUtility.checkUIThread();
        mNavigationSceneManager.recycleInvisibleScenes();
    }

    @Override
    protected LayoutInflater onGetLayoutInflater() {
        if (mNavigationSceneOptions.getUseActivityContextAndLayoutInflater()) {
            return requireActivity().getLayoutInflater();
        }
        return super.onGetLayoutInflater();
    }

    @Override
    protected Context onGetSceneContext() {
        if (mNavigationSceneOptions.getUseActivityContextAndLayoutInflater()) {
            return requireActivity();
        }
        return super.onGetSceneContext();
    }

    private InteractionNavigationPopAnimationFactory.InteractionCallback mInteractionCallback = new InteractionNavigationPopAnimationFactory.InteractionCallback() {
        @Override
        public void onStart() {
            List<InteractionNavigationPopAnimationFactory.InteractionCallback> copy = new ArrayList<>(mInteractionListenerList);
            for (InteractionNavigationPopAnimationFactory.InteractionCallback callback : copy) {
                callback.onStart();
            }
        }

        @Override
        public void onProgress(float progress) {
            List<InteractionNavigationPopAnimationFactory.InteractionCallback> copy = new ArrayList<>(mInteractionListenerList);
            for (InteractionNavigationPopAnimationFactory.InteractionCallback callback : copy) {
                callback.onProgress(progress);
            }
        }

        @Override
        public void onFinish() {
            List<InteractionNavigationPopAnimationFactory.InteractionCallback> copy = new ArrayList<>(mInteractionListenerList);
            for (InteractionNavigationPopAnimationFactory.InteractionCallback callback : copy) {
                callback.onFinish();
            }
        }
    };

    public boolean isInteractionNavigationPopSupport(@NonNull InteractionNavigationPopAnimationFactory animationFactory) {
        return mNavigationSceneManager.isInteractionNavigationPopSupport(animationFactory);
    }

    public void registerInteractionCallback(@NonNull InteractionNavigationPopAnimationFactory.InteractionCallback callback) {
        ThreadUtility.checkUIThread();
        this.mInteractionListenerList.add(callback);
    }

    public void unregisterInteractionCallback(@NonNull InteractionNavigationPopAnimationFactory.InteractionCallback callback) {
        ThreadUtility.checkUIThread();
        this.mInteractionListenerList.remove(callback);
    }

    public void registerChildSceneLifecycleCallbacks(@NonNull ChildSceneLifecycleCallbacks cb, boolean recursive) {
        ThreadUtility.checkUIThread();
        this.mLifecycleCallbacks.add(NonNullPair.create(cb, recursive));
    }

    public void unregisterChildSceneLifecycleCallbacks(@NonNull ChildSceneLifecycleCallbacks cb) {
        ThreadUtility.checkUIThread();
        NonNullPair<ChildSceneLifecycleCallbacks, Boolean> target = null;
        for (int i = 0, N = this.mLifecycleCallbacks.size(); i < N; i++) {
            if (this.mLifecycleCallbacks.get(i).first == cb) {
                target = this.mLifecycleCallbacks.get(i);
                break;
            }
        }
        if (target != null) {
            this.mLifecycleCallbacks.remove(target);
        }
    }

    public static void preloadClasses() {
        LoggerManager.getInstance();
        Object clazz = NavigationSceneOptions.class;
        clazz = SceneInstanceUtility.class;
        clazz = SceneComponentFactory.class;
        clazz = NavigationFrameLayout.class;
        clazz = DispatchWindowInsetsListener.class;
        clazz = Record.class;
        clazz = RecordStack.class;
        clazz = NavigationMessageQueue.class;
        clazz = NavigationRunnable.class;
        clazz = INavigationManager.class;
        clazz = NavigationManagerAbility.class;
        clazz = NavigationSceneManager.class;
        clazz = Operation.class;
        clazz = NavigationListener.class;
        clazz = ActivityCompatibleInfoCollector.class;
        clazz = ActivityCompatibleInfoCollector.Holder.class;
        clazz = AnimationContainerLayout.class;
        clazz = NavigationSceneGetter.class;
        clazz = ActivityCompatibleBehavior.class;
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void installWindowFocusChangeListenerIfNeeded() {
        if (!this.mNavigationSceneOptions.getUseWindowFocusChangedDispatch()) {
            return;
        }
        if (!this.mWindowFocusChangedInstalled && this.getView() != null) {
            this.mWindowFocusChangedInstalled = true;
            this.mSceneWindowFocusChangedDispatcher = new SceneWindowFocusChangedDispatcher();
            this.getView().getViewTreeObserver().addOnWindowFocusChangeListener(this.mSceneWindowFocusChangedDispatcher);
        }
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void uninstallWindowFocusChangeListenerIfNeeded() {
        if (!this.mNavigationSceneOptions.getUseWindowFocusChangedDispatch()) {
            return;
        }
        if (this.mWindowFocusChangedInstalled && this.getView() != null) {
            this.mWindowFocusChangedInstalled = false;
            this.getView().getViewTreeObserver().removeOnWindowFocusChangeListener(this.mSceneWindowFocusChangedDispatcher);
            this.mSceneWindowFocusChangedDispatcher = null;
        }
    }

    private class SceneWindowFocusChangedDispatcher implements ViewTreeObserver.OnWindowFocusChangeListener {
        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            mNavigationSceneManager.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    @Nullable
    public String getSceneDebugInfo(@NonNull Scene scene) {
        if (scene.getParentScene() == null) {
            return null;
        }
        if (scene.getParentScene() != this) {
            throw new IllegalArgumentException("Scene(" + scene + ") parent is incorrect");
        }
        Lifecycle.State state = scene.getLifecycle().getCurrentState();
        String status = null;
        if (state == Lifecycle.State.RESUMED) {
            status = "resumed";
        } else if (state == Lifecycle.State.STARTED) {
            status = "paused";
        } else if (state == Lifecycle.State.CREATED) {
            status = "stopped";
        }
        return "status: " + status + " ";
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneCreated(scene, savedInstanceState);
                    }
                }
            }
        }
        super.dispatchOnPreSceneCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneViewCreated(scene, savedInstanceState);
                    }
                }
            }
        }
        super.dispatchOnPreSceneViewCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneActivityCreated(scene, savedInstanceState);
                    }
                }
            }
        }
        super.dispatchOnPreSceneActivityCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneStarted(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneStarted(scene);
                    }
                }
            }
        }
        super.dispatchOnPreSceneStarted(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneResumed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneResumed(scene);
                    }
                }
            }
        }
        super.dispatchOnPreSceneResumed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreScenePaused(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreScenePaused(scene);
                    }
                }
            }
        }
        super.dispatchOnPreScenePaused(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneStopped(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneStopped(scene);
                    }
                }
            }
        }
        super.dispatchOnPreSceneStopped(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneViewDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneViewDestroyed(scene);
                    }
                }
            }
        }
        super.dispatchOnPreSceneViewDestroyed(scene, directChild);
    }

    /**
     * @hide
     */
    @Override
    public void dispatchOnPreSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneDestroyed(scene);
                    }
                }
            }
        }
        super.dispatchOnPreSceneDestroyed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnPreSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onPreSceneSaveInstanceState(scene, outState);
                    }
                }
            }
        }
        super.dispatchOnPreSceneSaveInstanceState(scene, outState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneCreated(scene, savedInstanceState);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneViewCreated(scene, savedInstanceState);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneViewCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneActivityCreated(scene, savedInstanceState);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneActivityCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneStarted(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneStarted(scene);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneStarted(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneResumed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneResumed(scene);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneResumed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperScenePaused(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperScenePaused(scene);
                    }
                }
            }
        }
        super.dispatchOnSuperScenePaused(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneStopped(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneStopped(scene);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneStopped(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneViewDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneViewDestroyed(scene);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneViewDestroyed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneDestroyed(scene);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneDestroyed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSuperSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSuperSceneSaveInstanceState(scene, outState);
                    }
                }
            }
        }
        super.dispatchOnSuperSceneSaveInstanceState(scene, outState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneCreated(scene, savedInstanceState);
                    }
                }
            }
        }

        super.dispatchOnSceneCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneViewCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneViewCreated(scene, savedInstanceState);
                    }
                }
            }
        }

        super.dispatchOnSceneViewCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneActivityCreated(scene, savedInstanceState);
                    }
                }
            }
        }
        super.dispatchOnSceneActivityCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneStarted(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneStarted(scene);
                    }
                }
            }
        }

        super.dispatchOnSceneStarted(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneResumed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneResumed(scene);
                    }
                }
            }
        }

        super.dispatchOnSceneResumed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneStopped(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneStopped(scene);
                    }
                }
            }
        }
        super.dispatchOnSceneStopped(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnScenePaused(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onScenePaused(scene);
                    }
                }
            }
        }

        super.dispatchOnScenePaused(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneSaveInstanceState(scene, outState);
                    }
                }
            }
        }

        super.dispatchOnSceneSaveInstanceState(scene, outState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneViewDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneViewDestroyed(scene);
                    }
                }
            }
        }

        super.dispatchOnSceneViewDestroyed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            if (mSceneLifecycleCallbackObjectCreationOpt && mLifecycleCallbacks.size() == 0) {
                //skip
            } else {
                List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
                for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                    if (directChild || pair.second) {
                        pair.first.onSceneDestroyed(scene);
                    }
                }
            }
        }

        super.dispatchOnSceneDestroyed(scene, directChild);
    }

    public boolean isRestoreStateInLifecycle() {
        return this.mRestoreStateInLifecycle;
    }

    public void setRestoreStateInLifecycle(boolean restoreStateInLifecycle) {
        if (this.mRestoreStateInLifecycle) {
            throw new IllegalStateException("setRestoreStateInLifecycle has already setup");
        }
        this.mRestoreStateInLifecycle = restoreStateInLifecycle;
    }

    /**
     * {@link com.bytedance.scene.utlity.ConfigurationUtility#getConfigChanges(Activity)}
     * @param allowList
     */
    public void setConfigurationChangesAllowList(int allowList) {
        this.mConfigurationChangesAllowList = allowList;
    }

    public int getConfigurationChangesAllowList() {
        return this.mConfigurationChangesAllowList;
    }

    /**
     * Maybe current Scene is already recycled because of low memory when Activity is in onStop state,
     * and user need use its ability before it is restored by NavigationScene sync onStart lifecycle operation, for example, in onActivityResult callback
     */
    public void restoreCurrentSceneIfNeeded() {
        if (!isRestoreStateInLifecycle()) {
            return;
        }
        Scene currentScene = this.getCurrentScene();
        if (currentScene == null) {
            return;
        }
        State currentSceneState = currentScene.getState();
        if (currentSceneState != State.NONE) {
            return;
        }
        State curNavigationSceneState = this.getState();
        if (curNavigationSceneState == State.NONE) {
            return;
        }
        dispatchCurrentChildState(curNavigationSceneState);
    }
}
