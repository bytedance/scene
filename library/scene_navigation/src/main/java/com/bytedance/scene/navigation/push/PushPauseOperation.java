package com.bytedance.scene.navigation.push;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.navigation.SceneTranslucent;
import com.bytedance.scene.utlity.Predicate;

import java.util.List;

public class PushPauseOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final Scene mScene;
    private final PushOptions mPushOptions;
    private final boolean mIsSceneTranslucent;
    private final NavigationScene mNavigationScene;

    public PushPauseOperation(NavigationManagerAbility managerAbility, Scene scene, PushOptions pushOptions) {
        this.mManagerAbility = managerAbility;
        this.mNavigationScene = managerAbility.getNavigationScene();
        this.mScene = scene;
        this.mPushOptions = pushOptions;
        this.mIsSceneTranslucent = pushOptions.isIsTranslucent() || scene instanceof SceneTranslucent;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        Predicate<Scene> removePredicate = mPushOptions.getRemovePredicate();
        if (removePredicate != null) {
            final List<Record> previousRecordList = mManagerAbility.getCurrentRecordList();
            for (int i = previousRecordList.size() - 1; i >= 0; i--) {
                Record oldRecord = previousRecordList.get(i);
                Scene oldScene = oldRecord.mScene;
                if (!removePredicate.apply(oldScene)) {
                    continue;
                }
                NavigationSceneManager.moveState(mNavigationScene, oldScene, State.NONE, null, false, null);
                mManagerAbility.removeRecord(oldRecord);
            }
        }

        final Record currentRecord = mManagerAbility.getCurrentRecord();

        //move current Scene to paused status
        if (currentRecord != null && mManagerAbility.getCurrentRecordList().contains(currentRecord)) {
            currentRecord.saveActivityStatus();
            final Scene currentScene = currentRecord.mScene;
            State dstState = mIsSceneTranslucent ? State.STARTED : State.ACTIVITY_CREATED;
            dstState = NavigationSceneManager.findMinState(dstState, mNavigationScene.getState());

            if (dstState == State.STARTED || dstState == State.ACTIVITY_CREATED) {
                NavigationSceneManager.moveState(mNavigationScene, currentScene, State.STARTED, null, false, null);
            }
        }

        operationEndAction.run();
    }
}