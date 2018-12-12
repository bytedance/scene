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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.UiThread;
import android.support.v4.util.LruCache;
import android.support.v4.util.Pair;
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
import com.bytedance.scene.interfaces.SceneNavigation;
import com.bytedance.scene.utlity.DispatchWindowInsetsListener;
import com.bytedance.scene.utlity.NonNullPair;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SoftInputUtility;
import com.bytedance.scene.utlity.ThreadUtility;
import com.bytedance.scene.utlity.Utility;
import com.bytedance.scene.utlity.ViewIdGenerator;
import com.bytedance.scene.view.NavigationFrameLayout;
import com.bytedance.scene.view.NoneTouchFrameLayout;

import java.util.ArrayList;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 7/30/18.
 */
public final class NavigationScene extends Scene implements SceneNavigation, NavigationListener {
    public static interface NavigationSceneHost {
        boolean isSupportRestore();

        void startActivityForResult(@NonNull Intent intent, int requestCode);

        void requestPermissions(@NonNull String[] permissions, int requestCode);
    }

    private NavigationSceneHost mNavigationSceneHost;
    private SceneComponentFactory mRootSceneComponentFactory;//销毁恢复的时候也用这个
    private NavigationSceneManager mNavigationSceneManager;

    private FrameLayout mPageContainer;
    private FrameLayout mAnimationContainer;

    private NavigationAnimationExecutor mDefaultNavigationAnimationExecutor = new Android8DefaultSceneAnimatorExecutor();

    private final List<NavigationListener> mNavigationListenerList = new ArrayList<>();
    private final List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> mLifecycleCallbacks = new ArrayList<>();
    private final List<InteractionNavigationPopAnimationFactory.InteractionCallback> mInteractionListenerList = new ArrayList<>();

    //todo 得保证顺序
    private List<Pair<Scene, PopListener>> mPopListenerList = new ArrayList<>();

    NavigationSceneOptions mNavigationSceneOptions;

    @UiThread
    public void addNavigationListener(NavigationListener listener) {
        ThreadUtility.checkUIThread();
        this.mNavigationListenerList.add(listener);
    }

    @UiThread
    public void removeNavigationListener(NavigationListener listener) {
        ThreadUtility.checkUIThread();
        this.mNavigationListenerList.remove(listener);
    }

    @UiThread
    @Override
    public void addPopListener(@NonNull Scene scene, @NonNull final PopListener popListener) {
        ThreadUtility.checkUIThread();
        if (scene.getState().value > State.NONE.value) {
            this.mNavigationSceneManager.addPopListener(scene, popListener);
            scene.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                void onDestroy() {
                    mNavigationSceneManager.removePopListenerList(popListener);
                }
            });
        }
    }

    @UiThread
    @Override
    public void removePopListener(@NonNull PopListener popListener) {
        ThreadUtility.checkUIThread();
        this.mNavigationSceneManager.removePopListenerList(popListener);
    }

    @UiThread
    @Override
    public void addConfigurationChangedListener(@NonNull Scene scene, @NonNull final ConfigurationChangedListener configurationChangedListener) {
        ThreadUtility.checkUIThread();
        if (scene.getState().value > State.NONE.value) {
            this.mNavigationSceneManager.addConfigurationChangedListener(scene, configurationChangedListener);
            scene.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                void onDestroy() {
                    mNavigationSceneManager.removeConfigurationChangedListener(configurationChangedListener);
                }
            });
        }
    }

    @NonNull
    @Override
    public String getStackHistory() {
        return mNavigationSceneManager.getStackHistory();
    }

    @Nullable
    @Override
    public Scene getCurrentScene() {
        return mNavigationSceneManager.getCurrentScene();
    }

    @NonNull
    @Override
    public List<Scene> getSceneList() {
        return mNavigationSceneManager.getCurrentSceneList();
    }

    public void setDefaultNavigationAnimationExecutor(NavigationAnimationExecutor defaultNavigationAnimationExecutor) {
        this.mDefaultNavigationAnimationExecutor = defaultNavigationAnimationExecutor;
    }

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
        }

        if (rootScene == null) {
            rootScene = SceneInstanceUtility.getInstanceFromClassName(requireActivity(), clazzName, arguments);
        }
        mNavigationSceneManager.push(rootScene, new PushOptions.Builder().build());
    }

    @Override
    public void push(@NonNull Class<? extends Scene> clazz) {
        push(clazz, null, new PushOptions.Builder().build());
    }

    @Override
    public void push(@NonNull Class<? extends Scene> clazz, Bundle argument) {
        push(clazz, argument, new PushOptions.Builder().build());
    }

    private LruCache<Class, ReuseGroupScene> mLruCache = new LruCache<>(3);

    void addToReusePool(ReuseGroupScene scene) {
        mLruCache.put(scene.getClass(), scene);
    }

    @Override
    public void push(@NonNull Class<? extends Scene> clazz, Bundle argument, PushOptions pushOptions) {
        if (getActivity() != null && !Utility.isActivityStatusValid(getActivity())) {
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

    @Override
    public void push(@NonNull Scene scene) {
        push(scene, new PushOptions.Builder().build());
    }

    @RestrictTo(LIBRARY_GROUP)
    public boolean isSupportRestore() {
        NavigationScene navigationScene = (NavigationScene) getNavigationScene();
        if (navigationScene != null) {
            return navigationScene.isSupportRestore();
        } else {
            return mNavigationSceneHost.isSupportRestore();
        }
    }

    @Override
    public void push(@NonNull Scene scene, @Nullable PushOptions pushOptions) {
        ThreadUtility.checkUIThread();

        if (getActivity() != null && !Utility.isActivityStatusValid(getActivity())) {
            return;
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

    @Override
    public void setResult(@NonNull Scene scene, Object result) {
        mNavigationSceneManager.setResult(scene, result);
    }

    @Override
    public boolean pop() {
        ThreadUtility.checkUIThread();

        if (getActivity() != null && !Utility.isActivityStatusValid(getActivity())) {
            return false;
        }
        if (mNavigationSceneManager.interceptPop()) {
            return true;
        } else if (mNavigationSceneManager.canPop()) {
            hideSoftInputIfNeeded();
            cancelPendingInputEventsIfNeeded();
            mNavigationSceneManager.pop();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pop是异步的，有可能Pop真正执行的时候发现没有可以Pop的Scene了
     * 转出来给外部，万一Activity有拦截onBackPressed也不一定
     */
    void finishCurrentActivity() {
        requireActivity().onBackPressed();
    }

    @Override
    public void pop(PopOptions popOptions) {
        ThreadUtility.checkUIThread();

        if (getActivity() != null && !Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        cancelPendingInputEventsIfNeeded();
        mNavigationSceneManager.pop(popOptions);
    }

    @Override
    public void popTo(@NonNull Class<? extends Scene> clazz) {
        popTo(clazz, null);
    }

    @Override
    public void popTo(@NonNull Class<? extends Scene> clazz, NavigationAnimationExecutor animationFactory) {
        ThreadUtility.checkUIThread();

        if (getActivity() != null && !Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        cancelPendingInputEventsIfNeeded();
        mNavigationSceneManager.popTo(clazz, animationFactory);
    }

    @Override
    public void popToRoot() {
        popToRoot(null);
    }

    @Override
    public void popToRoot(NavigationAnimationExecutor animationFactory) {
        ThreadUtility.checkUIThread();
        if (getActivity() != null && !Utility.isActivityStatusValid(getActivity())) {
            return;
        }
        hideSoftInputIfNeeded();
        cancelPendingInputEventsIfNeeded();
        mNavigationSceneManager.popToRoot(animationFactory);
    }

    @Override
    public void remove(Scene scene) {
        ThreadUtility.checkUIThread();

        if (getActivity() != null && !Utility.isActivityStatusValid(getActivity())) {
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

    public ViewGroup getPageContainer() {
        return this.mPageContainer;
    }

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NavigationFrameLayout frameLayout = new NavigationFrameLayout(requireSceneContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            frameLayout.setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
        frameLayout.setId(ViewIdGenerator.generateViewId());

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

    private static NavigationSceneOptions generateDefaultOptions() {
        NavigationSceneOptions navigationSceneOptions = new NavigationSceneOptions();
        navigationSceneOptions.setDrawWindowBackground(true);
        return navigationSceneOptions;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.mNavigationSceneManager.restoreFromBundle(requireActivity(), savedInstanceState, this.mRootSceneComponentFactory);
        } else {
            createRootSceneIfNeeded();
        }

        SceneNavigation parentSceneNavigation = getNavigationScene();
        if (parentSceneNavigation != null) {
            parentSceneNavigation.addPopListener(this, new PopListener() {
                @Override
                public boolean onPop() {
                    return pop();
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
    public void dispatchStart() {
        super.dispatchStart();
        dispatchCurrentChildState(State.STARTED);
    }

    @Override
    public void dispatchResume() {
        super.dispatchResume();
        dispatchCurrentChildState(State.RESUMED);
        this.mNavigationSceneManager.executePendingOperation();//必须在Children更新到onResume之后，保证该还原的Window属性都记录下来了
    }

    @Override
    public void dispatchPause() {
        dispatchCurrentChildState(State.STARTED);
        super.dispatchPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mNavigationSceneManager.saveToBundle(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mNavigationSceneManager.cancelCurrentRunningAnimation();//终止动画，避免有可能之后走到onDestroyView后，有动画还在执行引发的崩溃或者内存泄漏
    }

    @Override
    public void dispatchStop() {
        dispatchCurrentChildState(State.STOPPED);
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
            throw new IllegalArgumentException("dispatchCurrentChildState can only call when state is STOPPED, STARTED, RESUMED");
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

    private SparseArrayCompat<ActivityResultCallback> mResultCallbackMap = new SparseArrayCompat<>();
    private SparseArrayCompat<PermissionResultCallback> mPermissionResultCallbackMap = new SparseArrayCompat<>();

    //todo 万一Activity是空的怎么处理
    //todo 直接存对象内存泄漏怎么办
    @Override
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

        SceneNavigation parentSceneNavigation = getNavigationScene();
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

    @Override
    public void startActivity(@NonNull Intent intent) {
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void requestPermissions(@NonNull String[] permissions, int requestCode, PermissionResultCallback resultCallback) {
        ThreadUtility.checkUIThread();
        Activity activity = getActivity();
        if (!Utility.isActivityStatusValid(activity)) {
            return;
        }

        SceneNavigation parentSceneNavigation = getNavigationScene();
        if (parentSceneNavigation == null) {
            mPermissionResultCallbackMap.put(requestCode, resultCallback);
            this.mNavigationSceneHost.requestPermissions(permissions, requestCode);
        } else {
            parentSceneNavigation.requestPermissions(permissions, requestCode, resultCallback);
        }
    }

    @Override
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

    @Override
    public void registerChildSceneLifecycleCallbacks(@NonNull ChildSceneLifecycleCallbacks cb, boolean recursive) {
        ThreadUtility.checkUIThread();
        this.mLifecycleCallbacks.add(NonNullPair.create(cb, recursive));
    }

    @Override
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
