package com.bytedance.scene.navigation.push;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneTrace;
import com.bytedance.scene.State;
import com.bytedance.scene.interfaces.Function;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.launchmode.LaunchModeBehavior;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.navigation.pop.CoordinatePopCountOperation;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.queue.NavigationRunnable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangqi on 2023/11/13
 *
 * @author jiangqi@bytedance.com
 * <p>
 * <p>
 * 封装成三个 Operation，再封装进三个 NavigationRunnable
 *
 * PushPauseOperation -> PushCreateOperation -> PushStopOperation
 */

//一个 Task，内部创建出三个 Task，然后他立刻执行第一个，后面两个经过 Post
//最后一个 task 需要第一个 task 的 view 和 record 信息

//创建三个 Task，第一个，立刻执行，后面两个，经过 post

public class CoordinatePushOptionOperation implements Operation {
    private static final String TAG = "CoordinatePushOptionOperation";

    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final NavigationMessageQueue mMessageQueue;
    private final Scene mScene;
    private final PushOptions mPushOptions;
    private final LaunchModeBehavior mLaunchModeBehavior;

    public CoordinatePushOptionOperation(NavigationManagerAbility managerAbility,
                                         NavigationMessageQueue messageQueue,
                                         Scene scene, PushOptions pushOptions) {
        this.mManagerAbility = managerAbility;
        this.mNavigationScene = managerAbility.getNavigationScene();
        this.mMessageQueue = messageQueue;
        this.mScene = scene;
        this.mPushOptions = pushOptions;
        this.mLaunchModeBehavior = pushOptions.provideLaunchModeBehavior(scene.getClass());
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        this.mManagerAbility.cancelCurrentRunningAnimation();
        if (!this.mManagerAbility.canExecuteNavigationStackOperation()) {
            throw new IllegalArgumentException("Can't push, current NavigationScene state " + mNavigationScene.getState().name);
        }

        /*
         * It is possible to repeatedly push the same Scene object multiple times in multiple NavigationScene
         * But as the Push operation is not necessarily executed immediately,
         * the abnormal judgment in the Push method does not necessarily work.
         * So we need to check this case here to throw an exception.
         */
        boolean isFromReuse = mNavigationScene.isReusing(this.mScene);
        if (!isFromReuse && (this.mScene.getParentScene() != null)) {
            if (this.mScene.getParentScene() == mManagerAbility.getNavigationScene()) {
                operationEndAction.run();
                return;
            }
            throw new IllegalArgumentException("Scene already has a parent, parent " + mScene.getParentScene());
        }

        //for launchMode
        if (this.mLaunchModeBehavior != null) {
            final List<Pair<Scene, Bundle>> previousSceneList = mManagerAbility.getCurrentSceneAndArgumentsList();
            boolean isIntercepted = this.mLaunchModeBehavior.onInterceptPushOperation(previousSceneList);
            if (isIntercepted) {
                int popSceneCount = this.mLaunchModeBehavior.getPopSceneCount();
                final Bundle newArguments = mScene.getArguments();
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
                    LoggerManager.getInstance().i(TAG, "Push Operation is converted to Pop Operation because of launchMode");
                    new CoordinatePopCountOperation(mManagerAbility, mMessageQueue, this.mPushOptions.getNavigationAnimationFactory(), popSceneCount, null, onNewIntentAction).execute(operationEndAction);
                }else {
                    Record dstRecord = mManagerAbility.getCurrentRecord();
                    Scene dstScene = dstRecord.mScene;
                    if (dstScene.getState() == State.RESUMED) {
                        //Because Activity is onResume, so Scene is onResume too, pause then invoke onNewIntent, finally resume
                        //onResume(current state) -> onPause -> onNewIntent -> onResume
                        mManagerAbility.moveState(mNavigationScene, dstScene, State.STARTED, null, false, null);
                        onNewIntentAction.apply(dstScene);
                        mManagerAbility.moveState(mNavigationScene, dstScene, State.RESUMED, null, false, null);
                    } else {
                        //Because Activity is onPause or onStop, so Scene is onPause or onStop state, invoke onNewIntent directly
                        //onPause/onStop(current state) -> onNewIntent
                        if (dstScene.getState().value < State.ACTIVITY_CREATED.value) {
                            //restore target Scene if it has been destroyed
                            LoggerManager.getInstance().d(TAG, "Target Scene has been destroyed, restore it firstly, then dispatch onNewIntent");
                            mManagerAbility.moveState(mNavigationScene, dstScene, State.ACTIVITY_CREATED, dstRecord.consumeSavedInstanceState(), false, null);
                        }
                        onNewIntentAction.apply(dstScene);
                    }
                    operationEndAction.run();
                }
                return;
            }
        }

        final Record currentRecord = mManagerAbility.getCurrentRecord();

        //pause current scene and add new record to record list
        final PushPauseOperation pauseCurrentSceneOperation = new PushPauseOperation(mManagerAbility, mScene, mPushOptions);

        //execute new scene's lifecycle
        final PushCreateOperation createAndResumeNewSceneOperation = new PushCreateOperation(mManagerAbility, mScene, mPushOptions);

        //stop previous scene and execute animation if needed
        Operation stopPreviousSceneOperation = null;
        PushAnimationOperation pushAnimationOperation = null;

        if (mPushOptions.isUseAnimationBeforePause()) {
            //stop previous scene
            stopPreviousSceneOperation = new PushStopOperation(mManagerAbility, currentRecord);

            //execute animation if needed
            pushAnimationOperation = new PushAnimationOperation(mManagerAbility, currentRecord);
        } else {
            stopPreviousSceneOperation = new LegacyPushStopOperation(mManagerAbility, currentRecord);
        }

        Operation finalStopPreviousSceneOperation = stopPreviousSceneOperation;
        final NavigationRunnable stopPreviousSceneTask = new NavigationRunnable() {
            @Override
            public void run() {
                SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute operation directly");
                mManagerAbility.executeOperationSafely(finalStopPreviousSceneOperation, operationEndAction);
                mManagerAbility.endSuppressStackOperation(suppressTag);
                mManagerAbility.notifySceneStateChanged();
                SceneTrace.endSection();
            }
        };

        if (currentRecord != null) {
            PushAnimationOperation finalPushAnimationOperation = pushAnimationOperation;
            final NavigationRunnable createAndResumeNewSceneTask = new NavigationRunnable() {
                @Override
                public void run() {
                    SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                    String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute operation directly");
                    mManagerAbility.executeOperationSafely(createAndResumeNewSceneOperation, new Runnable() {
                        @Override
                        public void run() {
                            if (finalPushAnimationOperation != null) {
                                //first execute animation, then execute stop
                                mManagerAbility.executeOperationSafely(finalPushAnimationOperation, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mPushOptions.isUseIdleWhenStop()) {
                                            mMessageQueue.executeWhenIdleOrTimeLimit(stopPreviousSceneTask, TimeUnit.SECONDS.toMillis(10));
                                        } else {
                                            mMessageQueue.postAsyncAtHead(stopPreviousSceneTask);
                                        }
                                    }
                                });
                            } else {
                                if (mPushOptions.isUseIdleWhenStop()) {
                                    mMessageQueue.executeWhenIdleOrTimeLimit(stopPreviousSceneTask, TimeUnit.SECONDS.toMillis(10));
                                } else {
                                    mMessageQueue.postAsyncAtHead(stopPreviousSceneTask);
                                }
                            }
                        }
                    });
                    mManagerAbility.endSuppressStackOperation(suppressTag);
                    SceneTrace.endSection();
                }
            };
            pauseCurrentSceneOperation.execute(new Runnable() {
                @Override
                public void run() {
                    //但是这个逃不掉啊，不然当前这个任务就得走 postSync 了
                    mMessageQueue.postAsyncAtHead(createAndResumeNewSceneTask);
                }
            });
        } else {
            pauseCurrentSceneOperation.execute(new Runnable() {
                @Override
                public void run() {
                }
            });
            createAndResumeNewSceneOperation.execute(new Runnable() {
                @Override
                public void run() {

                }
            });
            stopPreviousSceneOperation.execute(operationEndAction);
        }
    }
}