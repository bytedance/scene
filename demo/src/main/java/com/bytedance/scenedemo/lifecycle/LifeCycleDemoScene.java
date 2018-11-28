package com.bytedance.scenedemo.lifecycle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.group.all.EmptyScene;

/**
 * Created by JiangQi on 8/28/18.
 * <p>
 * //todo 我靠，万一ondestroy的时候操作add怎么办
 * <p>
 * <p>
 * 1，在各种生命周期里面反复find+add多次
 * 2，批量操作，里面添加重复tag，应该报错
 */
public class LifeCycleDemoScene extends AppCompatScene {
    FrameLayout frameLayout;

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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
