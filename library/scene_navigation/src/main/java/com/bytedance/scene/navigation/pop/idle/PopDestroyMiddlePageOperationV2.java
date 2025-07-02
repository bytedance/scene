package com.bytedance.scene.navigation.pop.idle;

import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopDestroyMiddlePageOperationV2 implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final List<Record> mDestroyRecordList;
    private final Record mCurrentRecord;

    public PopDestroyMiddlePageOperationV2(NavigationManagerAbility navigationManagerAbility, NavigationAnimationExecutor animationFactory, List<Record> destroyRecordList, Record currentRecord, Record returnRecord, Scene currentScene, View currentSceneView) {
        this.mManagerAbility = navigationManagerAbility;
        this.mDestroyRecordList = destroyRecordList;
        this.mCurrentRecord = currentRecord;
    }

    @Override
    public void execute(final Runnable operationEndAction) {
        for (final Record record : this.mDestroyRecordList) {
            if (record == mCurrentRecord) {
                continue;
            }
            this.mManagerAbility.destroyByRecord(record, mCurrentRecord);
        }
        if (operationEndAction != null) {
            operationEndAction.run();
        }
    }
}
