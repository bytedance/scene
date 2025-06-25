package com.bytedance.scene.navigation.push;

import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignalList;

import java.util.List;

public class PushStopOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Record mPreviousRecord;
    private final View mPreviousSceneView;

    public PushStopOperation(NavigationManagerAbility managerAbility, Record previousRecord) {
        this.mManagerAbility = managerAbility;
        this.mNavigationScene = managerAbility.getNavigationScene();
        this.mPreviousRecord = previousRecord;
        this.mPreviousSceneView = previousRecord != null ? previousRecord.mScene.getView() : null;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        List<Record> currentRecordList = mManagerAbility.getCurrentRecordList();
        Record topRecord = currentRecordList.get(currentRecordList.size() - 1);

        if (mPreviousRecord == null) {
            mManagerAbility.getNavigationListener().navigationChange(null, topRecord.mScene, true);
            operationEndAction.run();
            return;
        }

        if (!topRecord.mIsTranslucent && currentRecordList.size() > 1) {
            State dstState = NavigationSceneManager.findMinState(State.ACTIVITY_CREATED, mNavigationScene.getState());

            for (int i = currentRecordList.size() - 2; i >= 0; i--) {
                Record record = currentRecordList.get(i);
                mManagerAbility.moveState(mNavigationScene, record.mScene, dstState, null, false, null);
                if (!record.mIsTranslucent) {
                    break;
                }
            }
        }

        mManagerAbility.getNavigationListener().navigationChange(mPreviousRecord.mScene, topRecord.mScene, true);

        //Navigation animation only execute when NavigationScene is visible, otherwise skip
        final boolean isNavigationSceneInAnimationState = mNavigationScene.getState().value >= State.STARTED.value;

        if (mManagerAbility.isDisableNavigationAnimation() || !isNavigationSceneInAnimationState || mPreviousSceneView == null) {
            operationEndAction.run();
            return;
        }

        NavigationAnimationExecutor navigationAnimationExecutor = null;
        //Scene can override mNavigationAnimationExecutor in moveState method by NavigationScene.overrideNavigationAnimationExecutor
        NavigationAnimationExecutor recordAnimationExecutor = topRecord.mNavigationAnimationExecutor;
        if (recordAnimationExecutor != null && recordAnimationExecutor.isSupport(mPreviousRecord.mScene.getClass(), topRecord.mScene.getClass())) {
            navigationAnimationExecutor = recordAnimationExecutor;
        }
        if (navigationAnimationExecutor == null) {
            navigationAnimationExecutor = mNavigationScene.getDefaultNavigationAnimationExecutor();
        }

        if (navigationAnimationExecutor != null && navigationAnimationExecutor.isSupport(mPreviousRecord.mScene.getClass(), topRecord.mScene.getClass())) {
            final Scene finalCurrentScene = mPreviousRecord.mScene;

            AnimatorUtility.bringSceneViewToFrontIfNeeded(mNavigationScene);//保证Z轴正确
            navigationAnimationExecutor.setAnimationViewGroup(mNavigationScene.getAnimationContainer());

            AnimationInfo fromInfo = new AnimationInfo(finalCurrentScene, mPreviousSceneView, finalCurrentScene.getState(), mPreviousRecord.mIsTranslucent);
            AnimationInfo toInfo = new AnimationInfo(topRecord.mScene, topRecord.mScene.getView(), topRecord.mScene.getState(), topRecord.mIsTranslucent);

            final CancellationSignalList cancellationSignalList = new CancellationSignalList();
            mManagerAbility.getCancellationSignalManager().add(cancellationSignalList);

            navigationAnimationExecutor.executePushChange(mNavigationScene,
                    mNavigationScene.getView().getRootView(),
                    fromInfo, toInfo, cancellationSignalList, new Runnable() {
                        @Override
                        public void run() {
                            mManagerAbility.getCancellationSignalManager().remove(cancellationSignalList);
                            operationEndAction.run();
                        }
                    });
        } else {
            operationEndAction.run();
        }
    }
}