package com.bytedance.scene.navigation.push;

import com.bytedance.scene.State;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;

import java.util.List;

public class PushStopOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Record mPreviousRecord;

    public PushStopOperation(NavigationManagerAbility managerAbility, Record previousRecord) {
        this.mManagerAbility = managerAbility;
        this.mNavigationScene = managerAbility.getNavigationScene();
        this.mPreviousRecord = previousRecord;
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
        operationEndAction.run();
    }
}