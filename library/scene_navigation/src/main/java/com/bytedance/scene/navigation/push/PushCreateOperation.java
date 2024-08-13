package com.bytedance.scene.navigation.push;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationManagerAbility;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneManager;
import com.bytedance.scene.navigation.Operation;
import com.bytedance.scene.navigation.Record;
import com.bytedance.scene.navigation.SceneTranslucent;

public class PushCreateOperation implements Operation {
    private final NavigationManagerAbility mManagerAbility;
    private final Scene mScene;
    private final PushOptions mPushOptions;
    private final boolean mIsSceneTranslucent;
    private final NavigationScene mNavigationScene;

    public PushCreateOperation(NavigationManagerAbility managerAbility, Scene scene, PushOptions pushOptions) {
        this.mManagerAbility = managerAbility;
        this.mNavigationScene = managerAbility.getNavigationScene();
        this.mScene = scene;
        this.mPushOptions = pushOptions;
        this.mIsSceneTranslucent = pushOptions.isIsTranslucent() || scene instanceof SceneTranslucent;
    }

    @Override
    public void execute(Runnable operationEndAction) {
        //add new scene to record list
        NavigationAnimationExecutor animationFactory = this.mPushOptions.getNavigationAnimationFactory();
        Record record = Record.newInstance(this.mScene, this.mIsSceneTranslucent, animationFactory);
        record.mPushResultCallback = this.mPushOptions.getPushResultCallback();

        this.mManagerAbility.pushRecord(record);

        /*
         * TODO: In fact, it is need to support that moveState to the specified state.
         *       Because of the destruction restore, it is impossible to go directly to RESUMED
         */

        mManagerAbility.moveState(mNavigationScene, mScene, mNavigationScene.getState(), null, false, null);
        operationEndAction.run();
    }
}