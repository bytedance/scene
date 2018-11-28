package com.bytedance.scenedemo.group.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by JiangQi on 11/6/18.
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
}
