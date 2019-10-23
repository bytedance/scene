package com.bytedance.scenedemo.restore;

import androidx.annotation.NonNull;
import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.SceneActivity;

public class SupportRestoreActivity extends SceneActivity {
    @NonNull
    @Override
    protected Class<? extends Scene> getHomeSceneClass() {
        return SupportRestoreRootScene.class;
    }

    @Override
    protected boolean supportRestore() {
        return true;
    }
}
