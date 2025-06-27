package com.bytedance.scene.navigation.pop.idle;

import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.group.ReuseGroupScene;
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
            Scene scene = record.mScene;
            this.mManagerAbility.moveState(this.mManagerAbility.getNavigationScene(), scene, State.NONE, null, false, null);
            this.mManagerAbility.removeRecord(record);
            // If it is a reusable Scene, save it
            if (scene instanceof ReuseGroupScene) {
                this.mManagerAbility.getNavigationScene().addToReusePool((ReuseGroupScene) scene);
            }
        }
        if (operationEndAction != null) {
            operationEndAction.run();
        }
    }
}
