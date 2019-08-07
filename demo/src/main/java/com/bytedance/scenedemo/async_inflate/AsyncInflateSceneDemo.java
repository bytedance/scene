package com.bytedance.scenedemo.async_inflate;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bytedance.scene.group.AsyncLayoutGroupScene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 9/19/18.
 */
public class AsyncInflateSceneDemo extends GroupScene {

    private int id;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        id = View.generateViewId();

        FrameLayout frameLayout = new FrameLayout(requireActivity());
        frameLayout.setId(id);
        return frameLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TestAsyncInflateScene scene = new TestAsyncInflateScene();
        scene.setAsyncLayoutEnabled(true);

        add(id, scene, "wo");
    }


    public static class TestAsyncInflateScene extends AsyncLayoutGroupScene {
        @Override
        protected int getLayoutId() {
            return R.layout.layout_test_async_layout;
        }

        @Override
        public void onAsyncActivityCreated(Bundle savedInstanceState) {
            super.onAsyncActivityCreated(savedInstanceState);

            ((AnimatedVectorDrawable) ((ImageView) findViewById(R.id.imageview)).getDrawable()).start();
        }
    }
}
