package com.bytedance.scene.navigation.pop;

import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignalList;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopDestroyOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationAnimationExecutor mAnimationFactory;
    private final NavigationScene mNavigationScene;
    private final List<Record> mDestroyRecordList;
    private final Record mCurrentRecord;
    private final Record mReturnRecord;
    private final Scene mCurrentScene;
    private final View mCurrentSceneView;

    public PopDestroyOperation(NavigationManagerAbility navigationManagerAbility, NavigationAnimationExecutor animationFactory,
                               List<Record> destroyRecordList, Record currentRecord, Record returnRecord, Scene currentScene, View currentSceneView) {
        this.mManagerAbility = navigationManagerAbility;
        this.mAnimationFactory = animationFactory;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mDestroyRecordList = destroyRecordList;
        this.mCurrentRecord = currentRecord;
        this.mReturnRecord = returnRecord;
        this.mCurrentScene = currentScene;
        this.mCurrentSceneView = currentSceneView;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        for (final Record record : this.mDestroyRecordList) {
            Scene scene = record.mScene;
            this.mManagerAbility.moveState(this.mManagerAbility.getNavigationScene(), scene, State.NONE, null, false, null);
            this.mManagerAbility.removeRecord(record);
            // If it is a reusable Scene, save it
            if (record != mCurrentRecord && scene instanceof ReuseGroupScene) {
                this.mManagerAbility.getNavigationScene().addToReusePool((ReuseGroupScene) scene);
            }
        }

        // Ensure that the requesting Scene is correct
        if (mCurrentRecord.mPushResultCallback != null && mNavigationScene.isEnableAutoRecycleInvisibleScenes()) {
            mCurrentRecord.mPushResultCallback.onResult(mCurrentRecord.mPushResult);
        }

        this.mManagerAbility.restoreActivityStatus(mReturnRecord.mActivityStatusRecord);
        this.mManagerAbility.getNavigationListener().navigationChange(mCurrentRecord.mScene, mReturnRecord.mScene, false);

        NavigationAnimationExecutor navigationAnimationExecutor = null;
        // If Pop has a specified animation, the animation specified by Pop is preferred.
        if (this.mAnimationFactory != null && this.mAnimationFactory.isSupport(mCurrentRecord.mScene.getClass(), mReturnRecord.mScene.getClass())) {
            navigationAnimationExecutor = this.mAnimationFactory;
        }

        if (navigationAnimationExecutor == null && mCurrentRecord.mNavigationAnimationExecutor != null && mCurrentRecord.mNavigationAnimationExecutor.isSupport(mCurrentRecord.mScene.getClass(), mReturnRecord.mScene.getClass())) {
            navigationAnimationExecutor = mCurrentRecord.mNavigationAnimationExecutor;
        }

        if (navigationAnimationExecutor == null) {
            navigationAnimationExecutor = mNavigationScene.getDefaultNavigationAnimationExecutor();
        }

        final boolean isNavigationSceneInAnimationState = mNavigationScene.getState().value >= State.STARTED.value;

        if (!this.mManagerAbility.isDisableNavigationAnimation() && isNavigationSceneInAnimationState && navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(mCurrentRecord.mScene.getClass(), mReturnRecord.mScene.getClass())) {
            ViewGroup animationContainer = mNavigationScene.getAnimationContainer();
            // Ensure that the Z-axis is correct
            AnimatorUtility.bringToFrontIfNeeded(animationContainer);
            navigationAnimationExecutor.setAnimationViewGroup(animationContainer);

            final CancellationSignalList cancellationSignalList = new CancellationSignalList();
            final Runnable endAction = new Runnable() {
                @Override
                public void run() {
                    mManagerAbility.getCancellationSignalManager().remove(cancellationSignalList);
                    if (mCurrentRecord.mScene instanceof ReuseGroupScene) {
                        mNavigationScene.addToReusePool((ReuseGroupScene) mCurrentRecord.mScene);
                    }
                    operationEndAction.run();
                }
            };

            final AnimationInfo fromInfo = new AnimationInfo(mCurrentScene, mCurrentSceneView, mCurrentScene.getState(), mCurrentRecord.mIsTranslucent);
            final AnimationInfo toInfo = new AnimationInfo(mReturnRecord.mScene, mReturnRecord.mScene.getView(), mReturnRecord.mScene.getState(), mReturnRecord.mIsTranslucent);

            this.mManagerAbility.getCancellationSignalManager().add(cancellationSignalList);
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
            if (mCurrentRecord.mScene instanceof ReuseGroupScene) {
                mNavigationScene.addToReusePool((ReuseGroupScene) mCurrentRecord.mScene);
            }
            operationEndAction.run();
        }
    }
}
