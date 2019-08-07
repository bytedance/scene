package com.bytedance.scenedemo;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.SceneActivity;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends SceneActivity {

    @NotNull
    @Override
    protected Class<? extends Scene> getHomeSceneClass() {
        return MainScene.class;
    }

    @Override
    protected boolean supportRestore() {
        return true;
    }

}
