package com.bytedance.scene.navigation.push;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneTrace;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.queue.NavigationRunnable;

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
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final NavigationMessageQueue mMessageQueue;
    private final Scene mScene;
    private final PushOptions mPushOptions;

    public CoordinatePushOptionOperation(NavigationManagerAbility managerAbility,
                                         NavigationMessageQueue messageQueue,
                                         Scene scene, PushOptions pushOptions) {
        this.mManagerAbility = managerAbility;
        this.mNavigationScene = managerAbility.getNavigationScene();
        this.mMessageQueue = messageQueue;
        this.mScene = scene;
        this.mPushOptions = pushOptions;
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
        if (this.mScene.getParentScene() != null) {
            if (this.mScene.getParentScene() == mManagerAbility.getNavigationScene()) {
                operationEndAction.run();
                return;
            }
            throw new IllegalArgumentException("Scene already has a parent, parent " + mScene.getParentScene());
        }

        final Record currentRecord = mManagerAbility.getCurrentRecord();

        //pause current scene and add new record to record list
        final PushPauseOperation pauseCurrentSceneOperation = new PushPauseOperation(mManagerAbility, mScene, mPushOptions);

        //execute new scene's lifecycle
        final PushCreateOperation createAndResumeNewSceneOperation = new PushCreateOperation(mManagerAbility, mScene, mPushOptions);

        //stop previous scene and execute animation if needed
        final PushStopOperation stopPreviousSceneOperation = new PushStopOperation(mManagerAbility, currentRecord);

        final NavigationRunnable stopPreviousSceneTask = new NavigationRunnable() {
            @Override
            public void run() {
                SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute operation directly");
                mManagerAbility.executeOperationSafely(stopPreviousSceneOperation, operationEndAction);
                mManagerAbility.endSuppressStackOperation(suppressTag);
                mManagerAbility.notifySceneStateChanged();
                SceneTrace.endSection();
            }
        };

        final NavigationRunnable createAndResumeNewSceneTask = new NavigationRunnable() {
            @Override
            public void run() {
                SceneTrace.beginSection(NavigationSceneManager.TRACE_EXECUTE_OPERATION_TAG);
                String suppressTag = mManagerAbility.beginSuppressStackOperation("NavigationManager execute operation directly");
                mManagerAbility.executeOperationSafely(createAndResumeNewSceneOperation, new Runnable() {
                    @Override
                    public void run() {
                        mMessageQueue.postAsyncAtHead(stopPreviousSceneTask);
                    }
                });
                mManagerAbility.endSuppressStackOperation(suppressTag);
                SceneTrace.endSection();
            }
        };

        if (currentRecord != null) {
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