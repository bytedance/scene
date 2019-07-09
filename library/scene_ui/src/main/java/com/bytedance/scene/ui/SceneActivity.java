package com.bytedance.scene.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bytedance.scene.SceneDelegate;
import com.bytedance.scene.NavigationSceneUtility;
import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/24/18.
 */
public abstract class SceneActivity extends AppCompatActivity {
    private SceneDelegate mDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        this.mDelegate = NavigationSceneUtility.setupWithActivity(this, savedInstanceState, getHomeSceneClass(), supportRestore());
    }

    @Override
    public void onBackPressed() {
        if (!this.mDelegate.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @NonNull
    protected abstract Class<? extends Scene> getHomeSceneClass();

    protected abstract boolean supportRestore();
}
