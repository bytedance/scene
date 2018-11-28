package com.bytedance.scenedemo;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.SceneActivity;

public class MainListActivity extends SceneActivity {
    @Override
    protected Class<? extends Scene> getHomeSceneClass() {
        return MainListScene.class;
    }

    @Override
    protected boolean supportRestore() {
        return true;
    }
}
