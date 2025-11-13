package com.bytedance.scene.navigation.pop;

import android.os.Bundle;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.State;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.utlity.SceneInternalException;

/**
 * Created by jiangqi on 2023/11/19
 *
 * @author jiangqi@bytedance.com
 */
public class PopPauseOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Scene mCurrentScene;
    private final Record mCurrentRecord;

    public PopPauseOperation(NavigationManagerAbility navigationManagerAbility, Record currentRecord, Scene currentScene) {
        this.mManagerAbility = navigationManagerAbility;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mCurrentRecord = currentRecord;
        this.mCurrentScene = currentScene;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        /*
         * The practice here should be to remove those Scenes in the middle,
         * then animate the two Scenes.
         */
        if (mCurrentScene != null) {
            Bundle previousSavedState = null;
            if (SceneGlobalConfig.usePreviousSavedStateWhenPauseIfPossible) {
                previousSavedState = this.mCurrentRecord.consumeSavedInstanceState();
                if (previousSavedState != null && this.mCurrentScene.getState() != State.NONE) {
                    throw new SceneInternalException("Scene' previous saved state still exists when its state is " + this.mCurrentScene.getState().name);
                }
            }
            this.mManagerAbility.moveState(this.mNavigationScene, this.mCurrentScene, State.STARTED, previousSavedState, false, null);
        }

        operationEndAction.run();
    }
}
