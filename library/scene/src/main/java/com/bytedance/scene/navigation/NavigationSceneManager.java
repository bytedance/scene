package com.bytedance.scene.navigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignalList;
import com.bytedance.scene.utlity.NonNullPair;
import com.bytedance.scene.utlity.Predicate;
import com.bytedance.scene.utlity.Utility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 优先保证生命周期正确，后保证动画连贯
 * <p>
 * Push/Pop 要求NavigationScene必须是onResume状态，如果不是，缓存等后续执行
 * 后续的Scene生命周期是下个UI循环的事情
 */

class AsyncHandler extends Handler {
    private boolean async = true;

    @SuppressLint("NewApi")
    AsyncHandler(Looper looper) {
        super(looper);
        if (Build.VERSION.SDK_INT < 16) {
            async = false;
        } else if (async && Build.VERSION.SDK_INT < 22) {
            // Confirm that the method is available on this API level despite being @hide.
            Message message = Message.obtain();
            try {
                message.setAsynchronous(true);
            } catch (NoSuchMethodError e) {
                async = false;
            }
            message.recycle();
        }
    }

    @SuppressLint("NewApi")
    public void postAsyncIfNeeded(Runnable runnable) {
        Message message = Message.obtain(this, runnable);
        if (async) {
            message.setAsynchronous(true);
        }
        sendMessage(message);
    }
}

public class NavigationSceneManager {
    private NavigationScene mNavigationScene;
    private final RecordStack mBackStackList = new RecordStack();
    private NavigationListener mNavigationListener;
    private final AsyncHandler mHandler = new AsyncHandler(Looper.getMainLooper());

    //如果暂停了，后续的操作都放这个队列，或者当前有正在执行的，也先放入这个队列
    private final ArrayDeque<Operation> mPendingActionList = new ArrayDeque<>();
    private long mLastPendingActionListItemTimestamp = -1L;

    private final CancellationSignalManager mCancellationSignalManager = new CancellationSignalManager();

    public NavigationSceneManager(NavigationScene scene) {
        this.mNavigationScene = scene;
        this.mNavigationListener = scene;
    }

    public void saveToBundle(Bundle bundle) {
        this.mBackStackList.saveToBundle(bundle);

        ArrayList<Bundle> bundleList = new ArrayList<>();
        List<Record> recordList = this.mBackStackList.getCurrentRecordList();
        for (Record record : recordList) {
            Bundle sceneBundle = new Bundle();
            record.mScene.onSaveInstanceState(sceneBundle);
            bundleList.add(sceneBundle);
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
            moveState(this.mNavigationScene, record.mScene, State.STOPPED, sceneBundle, false, null);
        }
    }

    public String getStackHistory() {
        return mBackStackList.getStackHistory();
    }

    private boolean mIsNavigationStateChangeInProgress = false;//正常情况下Push/Pop都是直接执行的，除非是Scene生命周期回调里面又触发的，那么必须走Post流程

    //todo Fragment post缺陷在于onSave后commit会崩溃，如果没有这些限制，post应该还好
    //我唯一想到的是Dialog的问题
    private void scheduleToNextUIThreadLoop(final Operation operation) {
        if (mNavigationScene.getState() == State.RESUMED) {
            if (mIsNavigationStateChangeInProgress) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        executeNowOrScheduleOperation(operation);
                    }
                };
                mHandler.postAsyncIfNeeded(task);
            } else {
                mIsNavigationStateChangeInProgress = true;
                operation.execute(EMPTY_RUNNABLE);
                mIsNavigationStateChangeInProgress = false;
            }
        } else {
            mPendingActionList.addLast(operation);
            mLastPendingActionListItemTimestamp = System.currentTimeMillis();
        }
    }

    private void executeNowOrScheduleOperation(Operation operation) {
        if (mNavigationScene.getState() == State.RESUMED) {
            mIsNavigationStateChangeInProgress = true;
            operation.execute(EMPTY_RUNNABLE);
            mIsNavigationStateChangeInProgress = false;
        } else {
            mPendingActionList.addLast(operation);
            mLastPendingActionListItemTimestamp = System.currentTimeMillis();
        }
    }

    public void dispatchCurrentChildState(State state) {
        new SyncCurrentSceneStateOperation(state).execute(EMPTY_RUNNABLE);
    }

    public void dispatchChildrenState(State state) {
        new SyncAllSceneStateOperation(state).execute(EMPTY_RUNNABLE);
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
        scheduleToNextUIThreadLoop(new RemoveOperation(scene));
    }

    public void pop() {
        scheduleToNextUIThreadLoop(new PopOperation(null));
    }

    public void pop(PopOptions popOptions) {
        scheduleToNextUIThreadLoop(new PopOptionOperation(popOptions));
    }

    public void popTo(Class<? extends Scene> clazz, NavigationAnimationExecutor animationFactory) {
        scheduleToNextUIThreadLoop(new PopToOperation(clazz, animationFactory));
    }

    public void popToRoot(NavigationAnimationExecutor animationFactory) {
        scheduleToNextUIThreadLoop(new PopToRootOperation(animationFactory));
    }

    public void push(@NonNull final Scene scene, @NonNull PushOptions pushOptions) {
        if (scene == null) {
            throw new NullPointerException("scene can't be null");
        }
        scheduleToNextUIThreadLoop(new PushOptionOperation(scene, pushOptions));
    }

    private boolean mDisableNavigationAnimation = false;

    public void executePendingOperation() {
        if (this.mPendingActionList.size() == 0 || mNavigationScene.getState() != State.RESUMED) {
            return;
        }

        //只有最后一个做过渡动画，之前的不需要做，如果最后一个也不做动画，很容易出现SchemaActivity跳转
        //动画无法执行，因为SchemaActivity通常是个半透明的盖住主Activity上面
        //如果时间超过800ms也不做动画
        boolean animationTimeout = System.currentTimeMillis() - mLastPendingActionListItemTimestamp > 800;
        List<Operation> copy = new ArrayList<>(this.mPendingActionList);
        for (int i = 0; i < copy.size(); i++) {
            Operation currentOperation = copy.get(i);
            this.mDisableNavigationAnimation = animationTimeout | (i < copy.size() - 1);
            this.mIsNavigationStateChangeInProgress = true;
            currentOperation.execute(EMPTY_RUNNABLE);
            this.mIsNavigationStateChangeInProgress = false;
            this.mDisableNavigationAnimation = false;
        }
        this.mPendingActionList.removeAll(copy);
        if (this.mPendingActionList.size() > 0) {
            throw new IllegalStateException("why mPendingActionList still have item?");
        }
        this.mLastPendingActionListItemTimestamp = -1L;
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

    public Record findRecordByScene(Scene scene) {
        return mBackStackList.getRecordByScene(scene);
    }

    public Record getCurrentRecord() {
        return mBackStackList.getCurrentRecord();
    }

    private List<NonNullPair<Scene, PopListener>> mPopListenerList = new ArrayList<>();

    public void addPopListener(Scene scene, PopListener popListener) {
        mPopListenerList.add(NonNullPair.create(scene, popListener));
    }

    public void removePopListenerList(PopListener popListenerList) {
        NonNullPair<Scene, PopListener> target = null;
        for (int i = mPopListenerList.size() - 1; i >= 0; i--) {
            NonNullPair<Scene, PopListener> pair = mPopListenerList.get(i);
            if (pair.second == popListenerList) {
                target = pair;
                break;
            }
        }
        mPopListenerList.remove(target);
    }

    public boolean interceptPop() {
        List<NonNullPair<Scene, PopListener>> copy = new ArrayList<>(mPopListenerList);
        for (int i = copy.size() - 1; i >= 0; i--) {
            NonNullPair<Scene, PopListener> pair = copy.get(i);
            if (pair.first.getState() == State.RESUMED) {
                if (pair.second.onPop()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ConfigurationChangedListener> mConfigurationChangedListenerList = new ArrayList<>();

    public void addConfigurationChangedListener(@NonNull Scene scene, @NonNull ConfigurationChangedListener configurationChangedListener) {
        mConfigurationChangedListenerList.add(configurationChangedListener);
    }

    public void removeConfigurationChangedListener(@NonNull ConfigurationChangedListener configurationChangedListener) {
        mConfigurationChangedListenerList.remove(configurationChangedListener);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        for (int i = mConfigurationChangedListenerList.size() - 1; i >= 0; i--) {
            ConfigurationChangedListener listener = mConfigurationChangedListenerList.get(i);
            if (listener != null) {
                listener.onConfigurationChanged(newConfig);
            }
        }
    }

    private static void moveState(NavigationScene navigationScene,
                                  Scene scene, State to,
                                  Bundle bundle,
                                  boolean causedByActivityLifeCycle,
                                  Runnable endAction) {
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
                    scene.dispatchAttachActivity(navigationScene.getActivity());
                    scene.dispatchAttachScene(navigationScene);
                    scene.dispatchCreate(bundle);
                    ViewGroup containerView = navigationScene.getPageContainer();
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
                            }
                        }
                        //todo 万一这个时候NavigationScene已经销毁了怎么办？
                        //todo 序列化怎么办
                        containerView.addView(scene.getView());
                    }
                    scene.getView().setVisibility(View.GONE);
                    scene.dispatchActivityCreated(bundle);

                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                case STOPPED:
                    scene.getView().setVisibility(View.VISIBLE);
                    scene.dispatchStart();
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
                case STARTED:
                    scene.dispatchResume();
                    moveState(navigationScene, scene, to, bundle, causedByActivityLifeCycle, endAction);
                    break;
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
                case STOPPED:
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
            }
        }
    }

    private interface Operation {
        void execute(Runnable operationEndAction);
    }

    private class PopOptionOperation implements Operation {
        private PopOptions mPopOptions;

        public PopOptionOperation(PopOptions popOptions) {
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

    private class PopCountOperation implements Operation {
        private NavigationAnimationExecutor animationFactory;
        private int popCount;

        public PopCountOperation(NavigationAnimationExecutor animationFactory, int popCount) {
            this.animationFactory = animationFactory;
            this.popCount = popCount;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            cancelCurrentRunningAnimation();

            if (mNavigationScene.getState().value < State.RESUMED.value) {
                throw new IllegalArgumentException("Can't push after NavigationScene is pause");
            }

            List<Record> recordList = mBackStackList.getCurrentRecordList();
            if (this.popCount <= 0) {
                throw new IllegalArgumentException("popCount can not be " + this.popCount + " stackSize is " + recordList.size());
            }
            if (this.popCount >= recordList.size()) {
                //得把可以Pop的都给Pop了
                //极端Case，有2个Scene，Pop 2 个后 Push 1 个新的，那么新的是不可能出来的因为Activity已经结束了
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

            //这里的做法应该是先移除中间的那些Scene，然后拿前后这2个Scene做动画
            for (final Record record : destroyRecordList) {
                Scene scene = record.mScene;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    scene.getView().cancelPendingInputEvents();
                }
                moveState(mNavigationScene, scene, State.NONE, null, false, null);
                mBackStackList.remove(record);
                //如果是个可复用的Scene，那么存起来
                if (record != currentRecord && scene instanceof ReuseGroupScene) {
                    mNavigationScene.addToReusePool((ReuseGroupScene) scene);
                }
            }

            final Scene dstScene = returnRecord.mScene;

            moveState(mNavigationScene, dstScene, State.RESUMED, null, false, null);
            //保证请求的Scene是正确的
            if (returnRecord.mPushResultCallback != null) {
                if (returnRecord.mPushForResultTargetHashCode == currentRecord.mScene.hashCode()) {
                    returnRecord.mPushResultCallback.onResult(currentRecord.mPushResult);
                } else {
                    returnRecord.mPushResultCallback.onResult(null);
                }
                returnRecord.mPushResultCallback = null;
            }

            //多个半透明的叠加一个不透明的Scene，返回后，必然需要把之前的半透明Scene都切到RESUMED
            if (returnRecord.mIsTranslucent) {
                final List<Record> currentRecordList = mBackStackList.getCurrentRecordList();
                if (currentRecordList.size() > 1) {
                    for (int i = currentRecordList.size() - 2; i >= 0; i--) {
                        Record record = currentRecordList.get(i);
                        moveState(mNavigationScene, record.mScene, State.RESUMED, null, false, null);
                        if (!record.mIsTranslucent) {
                            break;
                        }
                    }
                }
            }

            restoreActivityStatus(returnRecord.mActivityStatusRecord);
            mNavigationListener.navigationChange(currentRecord.mScene, returnRecord.mScene, false);

            NavigationAnimationExecutor navigationAnimationExecutor = null;
            //如果Pop有指定动画，优先Pop指定的动画
            if (animationFactory != null && animationFactory.isSupport(currentRecord.mScene.getClass(), returnRecord.mScene.getClass())) {
                navigationAnimationExecutor = animationFactory;
            }

            if (navigationAnimationExecutor == null && currentRecord.mNavigationAnimationExecutor != null && currentRecord.mNavigationAnimationExecutor.isSupport(currentRecord.mScene.getClass(), returnRecord.mScene.getClass())) {
                navigationAnimationExecutor = currentRecord.mNavigationAnimationExecutor;
            }

            if (navigationAnimationExecutor == null) {
                navigationAnimationExecutor = mNavigationScene.getDefaultNavigationAnimationExecutor();
            }

            if (!mDisableNavigationAnimation && navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(currentRecord.mScene.getClass(), returnRecord.mScene.getClass())) {
                ViewGroup animationContainer = mNavigationScene.getAnimationContainer();
                AnimatorUtility.bringToFrontIfNeeded(animationContainer);//保证Z轴正确
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

                final AnimationInfo fromInfo = new AnimationInfo(currentScene, currentSceneView, currentScene.getState(), false);
                final AnimationInfo toInfo = new AnimationInfo(returnRecord.mScene, returnRecord.mScene.getView(), returnRecord.mScene.getState(), false);

                mCancellationSignalManager.add(cancellationSignalList);
                //Push后立刻Pop的极端Case，我们有可能抢在Push的View在layout之前执行到Pop，这个时候高宽都是0，也没Parent，动画没法执行，需要修正
                navigationAnimationExecutor.executePopChange(mNavigationScene,
                        mNavigationScene.getView().getRootView(),
                        fromInfo, toInfo, cancellationSignalList, endAction);
            } else {
                operationEndAction.run();
            }
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
     * 1，如果是顶层，那么就是Pop
     * 2，移除后，下面的Scene如果需要要更新状态
     * 3，移除的是根怎么办
     */
    private class RemoveOperation implements Operation {
        private Scene mScene;

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
                    //有可能正在做动画，所以要重置掉动画
                    if (i == list.size() - 2) {
                        cancelCurrentRunningAnimation();
                    }

                    State sceneState = mScene.getState();
                    moveState(mNavigationScene, mScene, State.NONE, null, false, null);
                    mBackStackList.remove(record);

                    //透明的处理
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
        private NavigationAnimationExecutor animationFactory;

        public PopOperation(NavigationAnimationExecutor animationFactory) {
            this.animationFactory = animationFactory;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            new PopCountOperation(animationFactory, 1).execute(operationEndAction);
        }
    }

    private class PopToRootOperation implements Operation {
        private NavigationAnimationExecutor animationFactory;

        public PopToRootOperation(NavigationAnimationExecutor animationFactory) {
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
        private Class<? extends Scene> clazz;
        private NavigationAnimationExecutor animationFactory;

        private PopToOperation(Class<? extends Scene> clazz, NavigationAnimationExecutor animationFactory) {
            this.clazz = clazz;
            this.animationFactory = animationFactory;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            List<Record> recordList = mBackStackList.getCurrentRecordList();
            Record returnRecord = null;
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (record.mScene.getClass() == clazz) {
                    returnRecord = record;
                    break;
                }
            }

            //说明根本没有
            if (returnRecord == null) {
                throw new IllegalArgumentException("Cant find " + clazz.getSimpleName() + " in backStack");
            }

            int count = recordList.size() - 1;
            if (count == 0) {
                operationEndAction.run();
                return;
            }
            new PopCountOperation(animationFactory, count).execute(operationEndAction);
        }
    }

    private class PushOptionOperation implements Operation {
        private Scene scene;
        private PushOptions pushOptions;

        private PushOptionOperation(final Scene scene, PushOptions pushOptions) {
            this.scene = scene;
            this.pushOptions = pushOptions;
        }

        @Override
        public void execute(final Runnable operationEndAction) {
            cancelCurrentRunningAnimation();
            if (mNavigationScene.getState().value < State.RESUMED.value) {
                throw new IllegalArgumentException("Can't push after NavigationScene is pause");
            }

            final Record currentRecord = mBackStackList.getCurrentRecord();
            final View currentView = currentRecord != null ? currentRecord.mScene.getView() : null;

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
                currentRecord.mPushResultCallback = pushOptions.getPushResultCallback();
                //todo 销毁恢复过来肯定不行了，类似Activity
                currentRecord.mPushForResultTargetHashCode = scene.hashCode();
                Scene currentScene = currentRecord.mScene;
                State dstState = pushOptions.isIsTranslucent() ? State.STARTED : State.STOPPED;
                moveState(mNavigationScene, currentScene, dstState, null, false, null);

                //多个半透明的叠加一个不透明的Scene，必然需要把之前的半透明Scene都切到STOPPED
                final List<Record> currentRecordList = mBackStackList.getCurrentRecordList();
                if (currentRecordList.size() > 1 && !pushOptions.isIsTranslucent() && currentRecord.mIsTranslucent) {
                    for (int i = currentRecordList.size() - 2; i >= 0; i--) {
                        Record record = currentRecordList.get(i);
                        moveState(mNavigationScene, record.mScene, State.STOPPED, null, false, null);
                        if (!record.mIsTranslucent) {
                            break;
                        }
                    }
                }
            }

            final NavigationAnimationExecutor animationFactory = pushOptions.getNavigationAnimationFactory();
            final Record record = Record.newInstance(scene, pushOptions.isIsTranslucent(), animationFactory);
            mBackStackList.push(record);

            if (isTaskRootReplaced) {

            }

            //todo，其实moveState到指定状态，就是需要支持的，因为销毁恢复，必然不可能直接到RESUMED
            moveState(mNavigationScene, scene, State.RESUMED, null, false, null);

            mNavigationListener.navigationChange(currentRecord != null ? currentRecord.mScene : null, scene, true);

            if (!mDisableNavigationAnimation && currentRecord != null) {
                NavigationAnimationExecutor navigationAnimationExecutor = null;
                if (animationFactory != null && animationFactory.isSupport(currentRecord.mScene.getClass(), scene.getClass())) {
                    navigationAnimationExecutor = animationFactory;
                }

                if (navigationAnimationExecutor == null) {
                    navigationAnimationExecutor = mNavigationScene.getDefaultNavigationAnimationExecutor();
                }

                if (navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(currentRecord.mScene.getClass(), scene.getClass())) {
                    final Scene finalCurrentScene = currentRecord.mScene;

                    AnimatorUtility.bringToFrontIfNeeded(mNavigationScene.getPageContainer());//保证Z轴正确
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
        private State state;

        private SyncCurrentSceneStateOperation(State state) {
            this.state = state;
        }

        @Override
        public void execute(Runnable operationEndAction) {
            if (getCurrentRecord() == null) {
                operationEndAction.run();
                return;
            }

            //处理半透明，保证能正确执行到相应的方法
            List<Record> recordList = mBackStackList.getCurrentRecordList();
            State targetState = this.state;
            for (int i = recordList.size() - 1; i >= 0; i--) {
                Record record = recordList.get(i);
                if (i == recordList.size() - 1) {
                    NavigationSceneManager.moveState(mNavigationScene, record.mScene, targetState, null, true, operationEndAction);
                    //如果当前的是不透明，那么没必须再遍历了
                    if (!record.mIsTranslucent) {
                        break;
                    }
                } else {
                    State fixDstState = null;
                    if (targetState == State.RESUMED) {
                        fixDstState = State.STARTED;
                    } else if (targetState == State.STARTED) {
                        fixDstState = State.STARTED;
                    } else if (targetState == State.STOPPED) {
                        fixDstState = State.STOPPED;
                    }

                    NavigationSceneManager.moveState(mNavigationScene, record.mScene, fixDstState, null, true, operationEndAction);
                    if (!record.mIsTranslucent) {
                        break;
                    }
                }
            }

            operationEndAction.run();
        }
    }

    private class SyncAllSceneStateOperation implements Operation {
        private State state;

        private SyncAllSceneStateOperation(State state) {
            this.state = state;
        }

        @Override
        public void execute(Runnable operationEndAction) {
            if (getCurrentRecord() == null) {
                operationEndAction.run();
                return;
            }

            List<Record> recordList = mBackStackList.getCurrentRecordList();
            for (final Record record : recordList) {
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
}
