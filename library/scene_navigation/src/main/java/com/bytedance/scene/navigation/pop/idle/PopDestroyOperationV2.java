package com.bytedance.scene.navigation.pop.idle;

import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;

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

    public PopDestroyOperationV2(NavigationManagerAbility navigationManagerAbility, Record currentRecord, Record returnRecord) {
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
            this.mManagerAbility.obtainNavigationResultActionHandler().deliverResultLegacy(mCurrentRecord);
        }

        this.mManagerAbility.obtainNavigationResultActionHandler().deliverResult(mReturnRecord);

        this.mManagerAbility.restoreActivityStatus(mReturnRecord.mActivityStatusRecord);
        this.mManagerAbility.getNavigationListener().navigationChange(mCurrentRecord.mScene, mReturnRecord.mScene, false);
        mNavigationScene.addToReuseCache(mCurrentRecord.mScene);
    }
}
