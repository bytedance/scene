package com.bytedance.scene.navigation.pop.idle;

import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopDestroyOperationV2 implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Record mCurrentRecord;
    private final Record mReturnRecord;

    public PopDestroyOperationV2(NavigationManagerAbility navigationManagerAbility, NavigationAnimationExecutor animationFactory, List<Record> destroyRecordList, Record currentRecord, Record returnRecord, Scene currentScene, View currentSceneView) {
        this.mManagerAbility = navigationManagerAbility;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mCurrentRecord = currentRecord;
        this.mReturnRecord = returnRecord;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        mManagerAbility.destroyByRecord(mCurrentRecord, mCurrentRecord);

        // Ensure that the requesting Scene is correct
        if (mCurrentRecord.mPushResultCallback != null && mNavigationScene.isFixOnResultTiming()) {
            mCurrentRecord.mPushResultCallback.onResult(mCurrentRecord.mPushResult);
        }

        this.mManagerAbility.restoreActivityStatus(mReturnRecord.mActivityStatusRecord);
        this.mManagerAbility.getNavigationListener().navigationChange(mCurrentRecord.mScene, mReturnRecord.mScene, false);
        mNavigationScene.addToReuseCache(mCurrentRecord.mScene);
    }
}
