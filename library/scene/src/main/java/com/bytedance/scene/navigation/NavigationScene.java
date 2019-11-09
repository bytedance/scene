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
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.*;
import androidx.collection.LruCache;
import androidx.collection.SparseArrayCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.bytedance.scene.*;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.animatorexecutor.Android8DefaultSceneAnimatorExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.interfaces.PermissionResultCallback;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.utlity.*;
import com.bytedance.scene.view.NavigationFrameLayout;
import com.bytedance.scene.view.NoneTouchFrameLayout;

import java.util.ArrayList;
import java.util.List;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.lifecycle.Lifecycle.State.DESTROYED;

/**
 * Created by JiangQi on 7/30/18.
 *
 * (NavigationScene cannot be inherited)
 *
 * When entering:
 * 1.Parent: onAttach -> onCreate -> onCreateView -> onViewCreated -> onActivityCreated
 *   (All child Scenes start the life cycle process after NavigationAccCreate's onActivityCreated)
 *   (The previously pushed Scene will be cached)
 * 2.Child: onAttach -> onCreate -> onCreateView -> onViewCreated -> onActivityCreated
 * 3.Parent: onStart
 * 4.Child: onStart
 * 5.Parent: onResume
 * 6.Child: onResume
 *
 * When exiting:
 * 1.Child: onPause
 * 2.Parent: onPause
 * 3.Child: onStop
 * 4.Parent: onStop
 * 5.Child: onDestroyView -> onDestroy -> onDetach
 * 6.Parent: onDestroyView -> onDestroy -> onDetach
 */
public final class NavigationScene extends Scene implements NavigationListener, SceneParent {
    private static final String KEY_NAVIGATION_SCENE_SUPPORT_RESTORE_ARGUMENT = "bd-scene-navigation:support_restore";

    private boolean mSupportRestore = true;//default support restore
    private SceneComponentFactory mRootSceneComponentFactory;   // Use this when destroying recovery
    NavigationSceneOptions mNavigationSceneOptions;

    private NavigationSceneManager mNavigationSceneManager;
    private FrameLayout mSceneContainer;
    private FrameLayout mAnimationContainer;
    @Nullable
    private NavigationAnimationExecutor mDefaultNavigationAnimationExecutor = new Android8DefaultSceneAnimatorExecutor();
    private final List<InteractionNavigationPopAnimationFactory.InteractionCallback> mInteractionListenerList = new ArrayList<>();

    private final LruCache<Class, ReuseGroupScene> mLruCache = new LruCache<>(3);

    private final List<NavigationListener> mNavigationListenerList = new ArrayList<>();
    private final List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> mLifecycleCallbacks = new ArrayList<>();

    @MainThread
    public void addNavigationListener(@NonNull final LifecycleOwner lifecycleOwner, @NonNull final NavigationListener listener) {
        ThreadUtility.checkUIThread();
        if (lifecycleOwner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        this.mNavigationListenerList.add(listener);
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                lifecycleOwner.getLifecycle().removeObserver(this);
                mNavigationListenerList.remove(listener);
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
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                lifecycleOwner.getLifecycle().removeObserver(this);
                mNavigationSceneManager.removeOnBackPressedListener(onBackPressedListener);
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
        return mNavigationSceneManager.getCurrentScene();
    }

    @NonNull
    @Override
    public List<Scene> getSceneList() {
        return mNavigationSceneManager.getCurrentSceneList();
    }

    public void setDefaultNavigationAnimationExecutor(@Nullable NavigationAnimationExecutor defaultNavigationAnimationExecutor) {
        this.mDefaultNavigationAnimationExecutor = defaultNavigationAnimationExecutor;
    }

    @Nullable
    public NavigationAnimationExecutor getDefaultNavigationAnimationExecutor() {
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

    private void createRootSceneIfNeeded() {
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
        mNavigationSceneManager.push(rootScene, new PushOptions.Builder().build());
    }

    public void push(@NonNull Class<? extends Scene> clazz) {
        push(clazz, null, new PushOptions.Builder().build());
    }

    public void push(@NonNull Class<? extends Scene> clazz, Bundle argument) {
        push(clazz, argument, new PushOptions.Builder().build());
    }

    void addToReusePool(@NonNull ReuseGroupScene scene) {
        mLruCache.put(scene.getClass(), scene);
    }

    /**
     * Push a new Scene.
     *
     * @see #pop()
     */
    public void push(@NonNull Class<? extends Scene> clazz, @Nullable Bundle argument, @Nullable PushOptions pushOptions) {
        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }

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

        push(scene, pushOptions);
    }

    public void push(@NonNull Scene scene) {
        push(scene, new PushOptions.Builder().build());
    }

    public void push(@NonNull Scene scene, @Nullable PushOptions pushOptions) {
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

        if (isSupportRestore() && !SceneInstanceUtility.isSupportRestore(scene)) {
            throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " must be a public class or public static class, " +
                    "and have only one parameterless constructor to be properly recreated from instance state.");
        }

        hideSoftInputIfNeeded();
        cancelPendingInputEventsIfNeeded();
        mNavigationSceneManager.push(scene, pushOptions);
    }

    private void hideSoftInputIfNeeded() {
        Scene currentScene = mNavigationSceneManager.getCurrentScene();
        if (currentScene != null) {
            SoftInputUtility.hideSoftInputFromWindow(currentScene.getView());
        }
    }

    private void cancelPendingInputEventsIfNeeded() {
        Scene currentScene = mNavigationSceneManager.getCurrentScene();
        if (currentScene != null) {
            View view = currentScene.getView();
            if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                view.cancelPendingInputEvents();
            }
        }
    }

    public void setResult(@NonNull Scene scene, @Nullable Object result) {
        mNavigationSceneManager.setResult(scene, result);
    }

    public boolean onBackPressed() {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return false;
        }
        if (mNavigationSceneManager.interceptOnBackPressed()) {
            return true;
        } else if (mNavigationSceneManager.canPop()) {
            pop();
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
        cancelPendingInputEventsIfNeeded();
        mNavigationSceneManager.pop();
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    public String beginSuppressStackOperation(@NonNull String tagPrefix) {
        return mNavigationSceneManager.beginSuppressStackOperation(tagPrefix);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    public void endSuppressStackOperation(@NonNull String suppressTag) {
        mNavigationSceneManager.endSuppressStackOperation(suppressTag);
    }

    /**
     * Pop() is asynchronous, it is possible that when Pop actually executes,
     * there is no Scene that can pop. Turn it out to the outside,
     * in case the Activity has intercepted onBackPressed.
     */
    void finishCurrentActivity() {
        requireActivity().onBackPressed();
    }

    public void pop(@NonNull PopOptions popOptions) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        cancelPendingInputEventsIfNeeded();
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
        cancelPendingInputEventsIfNeeded();
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
        cancelPendingInputEventsIfNeeded();
        mNavigationSceneManager.popToRoot(animationFactory);
    }

    public void remove(@NonNull Scene scene) {
        ThreadUtility.checkUIThread();

        if (!Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        if (mNavigationSceneManager.getCurrentScene() == scene) {
            hideSoftInputIfNeeded();
            cancelPendingInputEventsIfNeeded();
        }
        mNavigationSceneManager.remove(scene);
    }

    public void requestDisableTouchEvent(boolean disable) {
        ((NavigationFrameLayout) getView()).setTouchEnabled(!disable);
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
        return this.mAnimationContainer;
    }

    @Override
    public void navigationChange(@Nullable Scene from, @NonNull Scene to, boolean isPush) {
        List<NavigationListener> listenerList = new ArrayList<>(mNavigationListenerList);
        for (NavigationListener listener : listenerList) {
            listener.navigationChange(from, to, isPush);
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNavigationSceneManager = new NavigationSceneManager(this);

        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalArgumentException("NavigationScene need NavigationSceneOptions bundle");
        }
        mNavigationSceneOptions = NavigationSceneOptions.fromBundle(getArguments());
        if (savedInstanceState != null) {
            boolean supportRestore = savedInstanceState.getBoolean(KEY_NAVIGATION_SCENE_SUPPORT_RESTORE_ARGUMENT, isSupportRestore());
            if (!supportRestore) {
                disableSupportRestore();
            }
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NavigationFrameLayout frameLayout = new NavigationFrameLayout(requireSceneContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            frameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        frameLayout.setId(R.id.navigation_scene_content);

        mSceneContainer = new FrameLayout(requireSceneContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSceneContainer.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        frameLayout.addView(mSceneContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        NoneTouchFrameLayout noneTouchFrameLayout = new NoneTouchFrameLayout(requireSceneContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            noneTouchFrameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        noneTouchFrameLayout.setTouchEnabled(false);
        mAnimationContainer = noneTouchFrameLayout;
        frameLayout.addView(mAnimationContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (mNavigationSceneOptions.drawWindowBackground()) {
            ViewCompat.setBackground(frameLayout, Utility.getWindowBackground(requireSceneContext()));
        }
        return frameLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && isSupportRestore()) {
            this.mNavigationSceneManager.restoreFromBundle(requireActivity(), savedInstanceState, this.mRootSceneComponentFactory);
        } else {
            createRootSceneIfNeeded();
        }

        NavigationScene parentSceneNavigation = getNavigationScene();
        if (parentSceneNavigation != null) {
            parentSceneNavigation.addOnBackPressedListener(this, new OnBackPressedListener() {
                @Override
                public boolean onBackPressed() {
                    if (getState().value < State.STARTED.value) {
                        return false;
                    }
                    return NavigationScene.this.onBackPressed();
                }
            });
        }
    }

    @Override
    public void dispatchActivityCreated(@Nullable Bundle savedInstanceState) {
        super.dispatchActivityCreated(savedInstanceState);
        this.mNavigationSceneManager.executePendingOperation();
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

    @Override
    public void onDestroyView() {
        dispatchChildrenState(State.NONE);
        super.onDestroyView();
    }

    private void dispatchCurrentChildState(@NonNull State state) {
        if (getState().value < State.VIEW_CREATED.value) {
            throw new IllegalArgumentException("dispatchCurrentChildState can only call when state is VIEW_CREATED, ACTIVITY_CREATED, STARTED, RESUMED");
        }
        mNavigationSceneManager.dispatchCurrentChildState(state);
    }

    /**
     * Destroy operation needs to synchronize all children
     */
    private void dispatchChildrenState(@NonNull State state) {
        mNavigationSceneManager.dispatchChildrenState(state);
    }

    Record findRecordByScene(Scene scene) {
        return mNavigationSceneManager.findRecordByScene(scene);
    }

    public void convertFromTranslucent(Scene scene) {

    }

    public boolean convertToTranslucent(Scene scene) {
        return false;
    }

    public void convertBackgroundToBlack() {
        getView().setBackgroundColor(Color.BLACK);
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

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void dispatchOnSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneCreated(scene, savedInstanceState);
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
    public void dispatchOnSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneActivityCreated(scene, savedInstanceState);
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
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneStarted(scene);
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
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneResumed(scene);
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
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneStopped(scene);
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
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onScenePaused(scene);
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
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneSaveInstanceState(scene, outState);
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
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneViewDestroyed(scene);
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
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneDestroyed(scene);
                }
            }
        }

        super.dispatchOnSceneDestroyed(scene, directChild);
    }
}
