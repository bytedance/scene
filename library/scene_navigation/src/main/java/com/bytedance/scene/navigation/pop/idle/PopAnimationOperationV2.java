package com.bytedance.scene.navigation.pop.idle;

import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
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
public class PopAnimationOperationV2 implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationAnimationExecutor mAnimationFactory;
    private final NavigationScene mNavigationScene;
    private final Record mCurrentRecord;
    private final Record mReturnRecord;
    private final Scene mCurrentScene;
    private final View mCurrentSceneView;

    public final CancellationSignalList cancellationSignalList = new CancellationSignalList();

    public PopAnimationOperationV2(NavigationManagerAbility navigationManagerAbility, NavigationAnimationExecutor animationFactory, List<Record> destroyRecordList, Record currentRecord, Record returnRecord, Scene currentScene, View currentSceneView) {
        this.mManagerAbility = navigationManagerAbility;
        this.mAnimationFactory = animationFactory;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mCurrentRecord = currentRecord;
        this.mReturnRecord = returnRecord;
        this.mCurrentScene = currentScene;
        this.mCurrentSceneView = currentSceneView;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
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
            this.mManagerAbility.restoreActivityStatusBarNavigationBarStatus(mReturnRecord.mActivityStatusRecord);

            ViewGroup animationContainer = mNavigationScene.getAnimationContainer();
            // Ensure that the Z-axis is correct
            AnimatorUtility.bringAnimationViewToFrontIfNeeded(mNavigationScene);
            navigationAnimationExecutor.setAnimationViewGroup(animationContainer);
            final Runnable endAction = new Runnable() {
                @Override
                public void run() {
                    mManagerAbility.getCancellationSignalManager().remove(cancellationSignalList);
                    mManagerAbility.notifyNavigationAnimationEnd(mCurrentScene, mReturnRecord.mScene, false);
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
            navigationAnimationExecutor.executePopChange(mNavigationScene, mNavigationScene.getView().getRootView(), fromInfo, toInfo, cancellationSignalList, endAction);
        } else {
            operationEndAction.run();
        }
    }
}
