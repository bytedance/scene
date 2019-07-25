package com.bytedance.scene.navigation;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.util.LruCache;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.animatorexecutor.Android8DefaultSceneAnimatorExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.interfaces.ActivityResultCallback;
import com.bytedance.scene.interfaces.PermissionResultCallback;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.utlity.DispatchWindowInsetsListener;
import com.bytedance.scene.utlity.NonNullPair;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SoftInputUtility;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;
import com.bytedance.scene.view.NavigationFrameLayout;
import com.bytedance.scene.view.NoneTouchFrameLayout;
import com.ixigua.scene.R;

import java.util.ArrayList;
import java.util.List;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;
import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 *
 * 流程
 *
 * NavigationScene无法被继承
 *
 * 进入的时候
 * 1.自己的onAttach onCreate onCreateView onViewCreated onActivityCreated
 * (所有子Scene都是在NavigationScene的onActivityCreated之后开始走生命周期流程，对于之前push的Scene，都会缓存起来)
 * 2.子Scene的onAttach onCreate onCreateView onViewCreated onActivityCreated
 *
 * 3.自己的onStart
 * 4.子Scene的onStart
 *
 * 5.自己的onResume
 * 6.子Scene的onResume
 *
 * 退出的时候
 * 1.子Scene的onPause
 * 2.自己的onPause
 *
 * 3.子Scene的onStop
 * 4.自己的onStop
 *
 * 5.子Scene的onDestroyView onDestroy onDetach
 * 6.自己的onDestroyView onDestroy onDetach
 *
 *
 */
public final class NavigationScene extends Scene implements NavigationListener {
    public static interface NavigationSceneHost {
        boolean isSupportRestore();

        void startActivityForResult(@NonNull Intent intent, int requestCode);

        void requestPermissions(@NonNull String[] permissions, int requestCode);
    }

    private NavigationSceneHost mNavigationSceneHost;
    private SceneComponentFactory mRootSceneComponentFactory;//销毁恢复的时候也用这个
    NavigationSceneOptions mNavigationSceneOptions;

    private NavigationSceneManager mNavigationSceneManager;
    private FrameLayout mPageContainer;
    private FrameLayout mAnimationContainer;
    @NonNull
    private NavigationAnimationExecutor mDefaultNavigationAnimationExecutor = new Android8DefaultSceneAnimatorExecutor();
    private final List<InteractionNavigationPopAnimationFactory.InteractionCallback> mInteractionListenerList = new ArrayList<>();

    private final LruCache<Class, ReuseGroupScene> mLruCache = new LruCache<>(3);

    private final List<NavigationListener> mNavigationListenerList = new ArrayList<>();
    private final List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> mLifecycleCallbacks = new ArrayList<>();
    private final SparseArrayCompat<ActivityResultCallback> mResultCallbackMap = new SparseArrayCompat<>();
    private final SparseArrayCompat<PermissionResultCallback> mPermissionResultCallbackMap = new SparseArrayCompat<>();

    @UiThread
    public void addNavigationListener(@NonNull Lifecycle lifecycle, @NonNull final NavigationListener listener) {
        ThreadUtility.checkUIThread();
        if (lifecycle.getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        this.mNavigationListenerList.add(listener);
        lifecycle.addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                mNavigationListenerList.remove(listener);
            }
        });
    }

    @UiThread
    public void removeNavigationListener(@NonNull NavigationListener listener) {
        ThreadUtility.checkUIThread();
        this.mNavigationListenerList.remove(listener);
    }

    @UiThread
    public void addOnBackPressedListener(@NonNull Scene scene, @NonNull final OnBackPressedListener onBackPressedListener) {
        ThreadUtility.checkUIThread();
        if (scene.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        this.mNavigationSceneManager.addOnBackPressedListener(scene, onBackPressedListener);
        scene.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                mNavigationSceneManager.removeOnBackPressedListener(onBackPressedListener);
            }
        });
    }

    @UiThread
    public void removeOnBackPressedListener(@NonNull OnBackPressedListener onBackPressedListener) {
        ThreadUtility.checkUIThread();
        this.mNavigationSceneManager.removeOnBackPressedListener(onBackPressedListener);
    }

    @UiThread
    public void addConfigurationChangedListener(@NonNull Scene scene, @NonNull final ConfigurationChangedListener configurationChangedListener) {
        ThreadUtility.checkUIThread();
        if (scene.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        this.mNavigationSceneManager.addConfigurationChangedListener(scene, configurationChangedListener);
        scene.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy() {
                mNavigationSceneManager.removeConfigurationChangedListener(configurationChangedListener);
            }
        });
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
    public List<Scene> getSceneList() {
        return mNavigationSceneManager.getCurrentSceneList();
    }

    public void setDefaultNavigationAnimationExecutor(@NonNull NavigationAnimationExecutor defaultNavigationAnimationExecutor) {
        this.mDefaultNavigationAnimationExecutor = defaultNavigationAnimationExecutor;
    }

    @NonNull
    public NavigationAnimationExecutor getDefaultNavigationAnimationExecutor() {
        return this.mDefaultNavigationAnimationExecutor;
    }

    public void setNavigationSceneHost(NavigationSceneHost navigationSceneHost) {
        this.mNavigationSceneHost = navigationSceneHost;
    }

    public void setRootSceneComponentFactory(SceneComponentFactory rootSceneComponentFactory) {
        this.mRootSceneComponentFactory = rootSceneComponentFactory;
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

    void addToReusePool(ReuseGroupScene scene) {
        mLruCache.put(scene.getClass(), scene);
    }

    public void push(@NonNull Class<? extends Scene> clazz, Bundle argument, PushOptions pushOptions) {
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
            scene.setArguments(argument);
        }

        push(scene, pushOptions);
    }

    public void push(@NonNull Scene scene) {
        push(scene, new PushOptions.Builder().build());
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public boolean isSupportRestore() {
        NavigationScene navigationScene = (NavigationScene) getNavigationScene();
        if (navigationScene != null) {
            return navigationScene.isSupportRestore();
        } else {
            return mNavigationSceneHost.isSupportRestore();
        }
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
            throw new IllegalArgumentException("Scene must have only empty argument constructor when support restore");
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

    public void setResult(@NonNull Scene scene, Object result) {
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
     * Pop是异步的，有可能Pop真正执行的时候发现没有可以Pop的Scene了
     * 转出来给外部，万一Activity有拦截onBackPressed也不一定
     */
    void finishCurrentActivity() {
        requireActivity().onBackPressed();
    }

    public void pop(PopOptions popOptions) {
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

    public void remove(Scene scene) {
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
    public ViewGroup getPageContainer() {
        return this.mPageContainer;
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
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NavigationFrameLayout frameLayout = new NavigationFrameLayout(requireSceneContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            frameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        frameLayout.setId(R.id.navigation_scene_content);

        mPageContainer = new FrameLayout(requireSceneContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPageContainer.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        frameLayout.addView(mPageContainer);

        NoneTouchFrameLayout noneTouchFrameLayout = new NoneTouchFrameLayout(requireSceneContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            noneTouchFrameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        noneTouchFrameLayout.setTouchEnabled(false);
        mAnimationContainer = noneTouchFrameLayout;
        frameLayout.addView(mAnimationContainer);

        if (mNavigationSceneOptions.drawWindowBackground()) {
            ViewCompat.setBackground(frameLayout, Utility.getWindowBackground(requireSceneContext()));
        }
        return frameLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
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
            parentSceneNavigation.addConfigurationChangedListener(this, new ConfigurationChangedListener() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    NavigationScene.this.onConfigurationChanged(newConfig);
                }
            });
        }
    }

    @Override
    public void dispatchActivityCreated(Bundle savedInstanceState) {
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
        this.mNavigationSceneManager.saveToBundle(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mNavigationSceneManager.cancelCurrentRunningAnimation();//终止动画，避免有可能之后走到onDestroyView后，有动画还在执行引发的崩溃或者内存泄漏
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
        mResultCallbackMap.clear();
        mPermissionResultCallbackMap.clear();
    }

    private void dispatchCurrentChildState(State state) {
        if (getState().value < State.STOPPED.value) {
            throw new IllegalArgumentException("dispatchCurrentChildState can only call when state is STOPPED, ACTIVITY_CREATED, STARTED, RESUMED");
        }
        mNavigationSceneManager.dispatchCurrentChildState(state);
    }

    //销毁需要同步所有child
    private void dispatchChildrenState(State state) {
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

    //todo 万一Activity是空的怎么处理
    //todo 直接存对象内存泄漏怎么办
    public void startActivityForResult(@NonNull Intent intent, int requestCode, ActivityResultCallback resultCallback) {
        if (requestCode < 0) {
            startActivity(intent);
            return;
        }

        ThreadUtility.checkUIThread();
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }

        NavigationScene parentSceneNavigation = getNavigationScene();
        if (parentSceneNavigation == null) {
            mResultCallbackMap.put(requestCode, resultCallback);
            this.mNavigationSceneHost.startActivityForResult(intent, requestCode);
        } else {
            parentSceneNavigation.startActivityForResult(intent, requestCode, resultCallback);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityResultCallback callback = mResultCallbackMap.get(requestCode);
        if (callback != null) {
            callback.onResult(resultCode, data);
            mResultCallbackMap.remove(requestCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionResultCallback callback = mPermissionResultCallbackMap.get(requestCode);
        if (callback != null) {
            callback.onResult(grantResults);
            mPermissionResultCallbackMap.remove(requestCode);
        }
    }

    public void startActivity(@NonNull Intent intent) {
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(@NonNull String[] permissions, int requestCode, PermissionResultCallback resultCallback) {
        ThreadUtility.checkUIThread();
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }

        NavigationScene parentSceneNavigation = getNavigationScene();
        if (parentSceneNavigation == null) {
            mPermissionResultCallbackMap.put(requestCode, resultCallback);
            this.mNavigationSceneHost.requestPermissions(permissions, requestCode);
        } else {
            parentSceneNavigation.requestPermissions(permissions, requestCode, resultCallback);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        mNavigationSceneManager.onConfigurationChanged(newConfig);
    }

    //todo 抽到SceneNavigation
    public boolean pop(final InteractionNavigationPopAnimationFactory animationFactory) {
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

    public boolean isInteractionNavigationPopSupport(InteractionNavigationPopAnimationFactory animationFactory) {
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
    public void dispatchOnSceneCreated(@NonNull Scene scene, Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
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
    public void dispatchOnSceneStarted(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
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
    public void dispatchOnSceneResumed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
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
    public void dispatchOnSceneStopped(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
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
    public void dispatchOnScenePaused(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
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
    public void dispatchOnSceneSaveInstanceState(@NonNull Scene scene, Bundle outState, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
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
    public void dispatchOnSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : mLifecycleCallbacks) {
                if (directChild || pair.second) {
                    pair.first.onSceneDestroyed(scene);
                }
            }
        }

        super.dispatchOnSceneDestroyed(scene, directChild);
    }
}
