package com.bytedance.scene;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.ViewModelStore;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.ViewIdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 7/30/18.
 * 没有BackStack管理
 */
public abstract class Scene implements LifecycleOwner, ViewModelStoreOwner {
    public static final String SCENE_SERVICE = "scene";

    private Activity mActivity;
    private android.support.v7.view.ContextThemeWrapper mSceneContext;
    private LayoutInflater mLayoutInflater;
    private View mView;

    private Scene mParentScene;
    private Scope.RootScopeFactory mRootScopeFactory = Scope.DEFAULT_ROOT_SCOPE_FACTORY;
    private Scope mScope;
    private NavigationScene mNavigationScene;
    private State mState = State.NONE;
    private StringBuilder mStateHistoryBuilder = new StringBuilder(mState.name);
    private Bundle mArguments;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private List<Runnable> mPendingActionList = new ArrayList<>();
    private boolean mCalled = false;
    private boolean mVisibleDispatched = false;

    private int mThemeResource;

    public State getState() {
        return this.mState;
    }

    private void setState(State state) {
        this.mState = state;
        this.mStateHistoryBuilder.append(" - " + state.name);
    }

    public void setRootScopeFactory(Scope.RootScopeFactory rootScopeFactory) {
        this.mRootScopeFactory = rootScopeFactory;
    }

    public void setArguments(Bundle arguments) {
        this.mArguments = arguments;
    }

    public Bundle getArguments() {
        return this.mArguments;
    }

    public void dispatchAttachActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void dispatchAttachScene(@Nullable Scene parentScene) {
        if (parentScene != null) {
            this.mParentScene = parentScene;
            if (this.mParentScene instanceof NavigationScene) {
                this.mNavigationScene = (NavigationScene) this.mParentScene;
            } else {
                this.mNavigationScene = this.mParentScene.getNavigationScene();
            }
        }
        mCalled = false;
        onAttach();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onAttach()");
        }
    }

    public void dispatchCreate(@Nullable Bundle savedInstanceState) {
        if (mParentScene == null) {
            mScope = mRootScopeFactory.getRootScope();
        } else {
            mScope = mParentScene.getScope().buildScope(this, savedInstanceState);
        }
        if (savedInstanceState != null) {
            boolean hasArgument = savedInstanceState.getBoolean(ParcelConstants.KEY_SCENE_HAS_ARGUMENT);
            if (hasArgument) {
                Bundle bundle = savedInstanceState.getBundle(ParcelConstants.KEY_SCENE_ARGUMENT);
                if (bundle == null) {
                    throw new IllegalStateException("can't get Scene arguments from savedInstanceState");
                }
                bundle.setClassLoader(getClass().getClassLoader());
                setArguments(bundle);
            }
        }

        mCalled = false;
        onCreate(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onCreate()");
        }
        dispatchOnSceneCreated(this, savedInstanceState, false);
    }

    @NonNull
    protected final LayoutInflater getLayoutInflater() {
        if (this.mLayoutInflater == null) {
            this.mLayoutInflater = onGetLayoutInflater();
        }
        return mLayoutInflater;
    }

    protected LayoutInflater onGetLayoutInflater() {
        if (this.mActivity == null) {
            throw new IllegalStateException("onGetLayoutInflater() cannot be executed until the "
                    + "Scene is attached to the Activity.");
        }

        return this.mActivity.getLayoutInflater().cloneInContext(requireSceneContext());
    }

    public void dispatchCreateView(@Nullable Bundle savedInstanceState, @NonNull ViewGroup container) {
        if (this.mView != null) {
            throw new IllegalArgumentException("Scene already call onCreateView");
        }

        final View view = onCreateView(getLayoutInflater(), container, savedInstanceState);
        if (view == null) {
            throw new IllegalArgumentException("onCreateView cant return null");
        }
        if (view.getId() == View.NO_ID) {
            view.setId(ViewIdGenerator.generateViewId());
        }

        mView = view;
        mCalled = false;
        onViewCreated(mView, savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onViewCreated()");
        }
    }

    public void dispatchActivityCreated(Bundle savedInstanceState) {
        setState(State.STOPPED);
        mCalled = false;
        onActivityCreated(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onActivityCreated()");
        }
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    public void dispatchStart() {
        setState(State.STARTED);
        mCalled = false;
        onStart();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onStart()");
        }
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
        dispatchOnSceneStarted(this, false);
    }

    public void dispatchViewStateRestored(@Nullable Bundle savedInstanceState) {
        mCalled = false;
        onViewStateRestored(savedInstanceState);
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onViewStateRestored()");
        }
    }

    public void dispatchResume() {
        setState(State.RESUMED);
        mCalled = false;
        onResume();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onResume()");
        }
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        dispatchOnSceneResumed(this, false);
    }

    public void dispatchPause() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        setState(State.STARTED);
        mCalled = false;
        onPause();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onPause()");
        }
        dispatchOnScenePaused(this, false);
    }

    public void dispatchSaveInstanceState(Bundle outState) {
        onSaveInstanceState(outState);
    }

    public void dispatchStop() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        setState(State.STOPPED);
        mCalled = false;
        onStop();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onStop()");
        }
        dispatchOnSceneStopped(this, false);
    }

    public void dispatchDestroyView() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        setState(State.NONE);
        mCalled = false;
        onDestroyView();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onDestroyView()");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.mView.cancelPendingInputEvents();
        }
        this.mView = null;
        this.mLayoutInflater = null;
    }

    public void dispatchDestroy() {
        mCalled = false;
        onDestroy();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onDestroy()");
        }
        dispatchOnSceneDestroyed(this, false);
    }

    public void dispatchDetachScene() {
        this.mParentScene = null;
    }

    public void dispatchDetachActivity() {
        Activity activity = this.mActivity;
        this.mActivity = null;
        this.mSceneContext = null;
        mCalled = false;
        onDetach();
        if (!mCalled) {
            throw new SuperNotCalledException("Scene " + this
                    + " did not call through to super.onDetach()");
        }

        if (!activity.isChangingConfigurations()) {
            this.mScope.destroy();
        }
        this.mScope = null;
        mPendingActionList.clear();//必须最后才做，万一有人在onDestroy/onDetach里面操作add会难办，虽然上面executeNowOrScheduleAtNextResume可以改逻辑到CREATED之后就不管了
    }

    @CallSuper
    public void onAttach() {
        mCalled = true;
    }

    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mCalled = true;
    }

    @CallSuper
    public void onDetach() {
        mCalled = true;
    }

    @NonNull
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                                      @Nullable Bundle savedInstanceState);

    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCalled = true;
    }

    @CallSuper
    public void onDestroyView() {
        mCalled = true;
    }

    @CallSuper
    public void onDestroy() {
        mCalled = true;
    }

    public View getView() {
        return this.mView;
    }

    @Nullable
    public Activity getActivity() {
        return this.mActivity;
    }

    @Nullable
    public Context getApplicationContext() {
        if (this.mActivity == null) {
            return null;
        }
        return this.mActivity.getApplicationContext();
    }

    @Nullable
    public final ContextThemeWrapper getSceneContext() {
        if (this.mActivity == null) {
            return null;
        }
        if (this.mSceneContext == null) {
            this.mSceneContext = onGetSceneContext();
        }
        return this.mSceneContext;
    }

    @NonNull
    public final ContextThemeWrapper requireSceneContext() {
        ContextThemeWrapper sceneContext = getSceneContext();
        if (sceneContext == null) {
            throw new IllegalStateException("Scene " + this + " not attached to an activity.");
        }
        return sceneContext;
    }

    protected ContextThemeWrapper onGetSceneContext() {
        if (this.mActivity == null) {
            throw new IllegalStateException("onGetContextThemeWrapper() cannot be executed until the "
                    + "Scene is attached to the Activity.");
        }

        if (this.mThemeResource > 0) {
            return new android.support.v7.view.ContextThemeWrapper(this.mActivity, this.mThemeResource) {
                @Override
                public Object getSystemService(@NonNull String name) {
                    if (SCENE_SERVICE.equals(name)) {
                        return Scene.this;
                    } else if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
                        //oppo某些版本会在View.onDetachedFromWindow的时候去生成新View，所以这里做下判断
                        //发现Activity是null转给super处理
                        if (getActivity() != null) {
                            return getLayoutInflater();
                        }
                    }
                    return super.getSystemService(name);
                }
            };
        } else {
            return new android.support.v7.view.ContextThemeWrapper(this.mActivity, this.mActivity.getTheme()) {
                @Override
                public Object getSystemService(@NonNull String name) {
                    if (SCENE_SERVICE.equals(name)) {
                        return Scene.this;
                    } else if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
                        //oppo某些版本会在View.onDetachedFromWindow的时候去生成新View，所以这里做下判断
                        //发现Activity是null转给super处理
                        if (getActivity() != null) {
                            return getLayoutInflater();
                        }
                    }
                    return super.getSystemService(name);
                }
            };
        }
    }

    @NonNull
    public final Activity requireActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("Scene " + this + " not attached to an activity.");
        }
        return activity;
    }

    @NonNull
    public final Context requireApplicationContext() {
        Context context = getApplicationContext();
        if (context == null) {
            throw new IllegalStateException("Scene " + this + " not attached to a context.");
        }
        return context;
    }

    public Resources getResources() {
        return this.mActivity.getResources();
    }

    public final CharSequence getText(@StringRes int resId) {
        return getResources().getText(resId);
    }

    @NonNull
    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    @NonNull
    public final String getString(@StringRes int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    public Scene getParentScene() {
        return this.mParentScene;
    }

    public NavigationScene getNavigationScene() {
        return this.mNavigationScene;
    }

    @CallSuper
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Activity activity = requireActivity();
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int visibility = decorView.getSystemUiVisibility();
        if ((visibility & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0) {
            ViewCompat.requestApplyInsets(decorView);
        } else if ((visibility & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
            ViewCompat.requestApplyInsets(decorView);
        }

        mCalled = true;
    }

    @CallSuper
    public void onStart() {
        mCalled = true;
        dispatchVisibleChanged();
    }

    @CallSuper
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        mCalled = true;
    }

    @CallSuper
    public void onResume() {
        mCalled = true;
        executePendingActions();
    }

    @CallSuper
    public void onPause() {
        mCalled = true;
    }

    @CallSuper
    public void onSaveInstanceState(Bundle outState) {
        if (getArguments() != null) {
            outState.putBoolean(ParcelConstants.KEY_SCENE_HAS_ARGUMENT, true);
            outState.putBundle(ParcelConstants.KEY_SCENE_ARGUMENT, getArguments());
        } else {
            outState.putBoolean(ParcelConstants.KEY_SCENE_HAS_ARGUMENT, false);
        }
        mScope.saveInstance(outState);
        dispatchOnSceneSaveInstanceState(this, outState, false);
    }

    @CallSuper
    public void onStop() {
        mCalled = true;
        dispatchVisibleChanged();
    }

    public <T extends View> T findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }

    public String getStateHistory() {
        return this.mStateHistoryBuilder.toString();
    }

    public void executeNowOrScheduleAtNextResume(Runnable runnable) {
        if (getState() == State.RESUMED) {
            runnable.run();
        } else {
            mPendingActionList.add(runnable);
        }
    }

    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    @NonNull
    @Override
    public final Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    @NonNull
    @Override
    public final ViewModelStore getViewModelStore() {
        Scope scope = getScope();
        if (scope.hasServiceInMyScope(ViewModelStoreHolder.class)) {
            return ((ViewModelStoreHolder) scope.getService(ViewModelStoreHolder.class)).get();
        } else {
            ViewModelStore viewModelStore = new ViewModelStore();
            scope.register(ViewModelStoreHolder.class, new ViewModelStoreHolder(viewModelStore));
            return viewModelStore;
        }
    }

    private static class ViewModelStoreHolder implements Scope.Scoped {
        private ViewModelStore mViewModelStore;

        private ViewModelStoreHolder(@NonNull ViewModelStore viewModelStore) {
            this.mViewModelStore = viewModelStore;
        }

        @NonNull
        private ViewModelStore get() {
            return this.mViewModelStore;
        }

        @Override
        public void onUnRegister() {
            this.mViewModelStore.clear();
        }
    }

    public final void setTheme(@StyleRes int resid) {
        if (this.mThemeResource != resid) {
            this.mThemeResource = resid;
            if (this.mSceneContext != null) {
                this.mSceneContext.setTheme(this.mThemeResource);
            }
        }
    }

    public final int getTheme() {
        return this.mThemeResource;
    }

    public boolean isVisible() {
        return getState().value >= State.STARTED.value;
    }

    protected void dispatchVisibleChanged() {
        boolean visible = isVisible();
        if (visible == mVisibleDispatched) {
            return;
        }
        mVisibleDispatched = visible;
        onVisibleChanged(mVisibleDispatched);
    }

    private void executePendingActions() {
        if (this.mPendingActionList.size() > 0) {
            List<Runnable> copy = new ArrayList<>(this.mPendingActionList);
            for (final Runnable runnable : copy) {
                //万一PendingActionList里面的操作是操作生命周期怎么办，所以必须重新走executeNowOrScheduleAtNextResume，而且得
                //包裹一层Runnable
                executeNowOrScheduleAtNextResume(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                });
            }
            this.mPendingActionList.removeAll(copy);
        }
    }

    protected void onVisibleChanged(boolean visible) {

    }

    public Scope getScope() {
        if (this.mScope == null) {
            throw new IllegalStateException("Scope is not created, you can't call before onCreate");
        }
        return this.mScope;
    }

    //通知Parent自己的生命周期触发，响应的那些回调应该只判断Child变化而非自身变化
    public void dispatchOnSceneCreated(@NonNull Scene scene, Bundle savedInstanceState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneCreated(scene, savedInstanceState, scene == this);
        }
    }

    public void dispatchOnSceneStarted(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneStarted(scene, scene == this);
        }
    }

    public void dispatchOnSceneResumed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneResumed(scene, scene == this);
        }
    }

    public void dispatchOnSceneStopped(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneStopped(scene, scene == this);
        }
    }

    public void dispatchOnScenePaused(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnScenePaused(scene, scene == this);
        }
    }

    public void dispatchOnSceneSaveInstanceState(@NonNull Scene scene, Bundle outState, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneSaveInstanceState(scene, outState, scene == this);
        }
    }

    public void dispatchOnSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        Scene parentScene = getParentScene();
        if (parentScene != null) {
            parentScene.dispatchOnSceneDestroyed(scene, scene == this);
        }
    }
}