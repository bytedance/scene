package com.bytedance.scene.navigation.pop;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
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
public class PopPauseOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final NavigationScene mNavigationScene;
    private final Scene mCurrentScene;

    public PopPauseOperation(NavigationManagerAbility navigationManagerAbility, Scene currentScene) {
        this.mManagerAbility = navigationManagerAbility;
        this.mNavigationScene = navigationManagerAbility.getNavigationScene();
        this.mCurrentScene = currentScene;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        /*
         * The practice here should be to remove those Scenes in the middle,
         * then animate the two Scenes.
         */
        if (mCurrentScene != null) {
            this.mManagerAbility.moveState(this.mNavigationScene, mCurrentScene, State.STARTED, null, false, null);
        }

        operationEndAction.run();
    }
}
