package com.bytedance.scenedemo.migrate;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 11/6/18.
 *
 * Demonstrate how to manually manage the life cycle of Scene
 * and host the entire Scene with normal View
 */
public class TestSceneToViewActivity extends Activity {
    private TestSceneDelegateToViewView viewView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewView = new TestSceneDelegateToViewView(this);
        setContentView(viewView);
        viewView.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewView.onDestroyView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        viewView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        NavigationScene navigationScene = viewView.getNavigationScene();
        if (navigationScene != null && navigationScene.onBackPressed()) {
            //empty
        } else {
            super.onBackPressed();
        }
    }
}
