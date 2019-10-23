package com.bytedance.scenedemo.lifecycle;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.group.EmptyScene;

/**
 * Created by JiangQi on 8/28/18.
 */
public class LifeCycleDemoScene extends GroupScene {
    FrameLayout frameLayout;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        frameLayout = new FrameLayout(getActivity());
        frameLayout.setId(View.generateViewId());

        return frameLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (findSceneByTag("wo") == null) {
            add(frameLayout.getId(), EmptyScene.newInstance(0), "wo");
        }

        if (findSceneByTag("wo") == null) {
            add(frameLayout.getId(), EmptyScene.newInstance(0), "wo");
        }

//        beginTransaction();
//        if (findSceneByTag("wo") == null) {
//            add(frameLayout.getId(), EmptyScene.newInstance(0), "wo");
//        }
//
//        if (findSceneByTag("wo") == null) {
//            add(frameLayout.getId(), EmptyScene.newInstance(0), "wo");
//        }
//        commitTransaction();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
