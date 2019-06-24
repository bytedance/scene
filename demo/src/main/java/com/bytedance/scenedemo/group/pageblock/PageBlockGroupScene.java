package com.bytedance.scenedemo.group.pageblock;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.Creator;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/7/18.
 */
public class PageBlockGroupScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.group_scene_page_block, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PageBlockScene scene = createOrReuse("0", new Creator<PageBlockScene>() {
            @Override
            public PageBlockScene call() {
                return PageBlockScene.newInstance(0);
            }
        });
        PageBlockScene scene1 = createOrReuse("1", new Creator<PageBlockScene>() {
            @Override
            public PageBlockScene call() {
                return PageBlockScene.newInstance(1);
            }
        });
        PageBlockScene scene2 = createOrReuse("2", new Creator<PageBlockScene>() {
            @Override
            public PageBlockScene call() {
                return PageBlockScene.newInstance(2);
            }
        });
        PageBlockScene scene3 = createOrReuse("3", new Creator<PageBlockScene>() {
            @Override
            public PageBlockScene call() {
                return PageBlockScene.newInstance(3);
            }
        });

//        if (savedInstanceState != null) {
//            scene = findSceneByTag("0");
//            scene1 = findSceneByTag("1");
//            scene2 = findSceneByTag("2");
//            scene3 = findSceneByTag("3");
//        } else {
//            scene = PageBlockScene.newInstance(0);
//            scene1 = PageBlockScene.newInstance(1);
//            scene2 = PageBlockScene.newInstance(2);
//            scene3 = PageBlockScene.newInstance(3);
//
//
//        }

        if (!isAdded(scene))
            add(R.id.block_0, scene, "0");
        if (!isAdded(scene1))
            add(R.id.block_1, scene1, "1");
        if (!isAdded(scene2))
            add(R.id.block_2, scene2, "2");
        if (!isAdded(scene3))
            add(R.id.block_3, scene3, "3");

        final PageBlockScene finalScene = scene;
        findViewById(R.id.btn_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow(finalScene)) {
                    hide(finalScene);
                } else {
                    show(finalScene);
                }
            }
        });

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow(finalScene)) {
                    hide(finalScene, android.R.anim.slide_out_right);
                } else {
                    show(finalScene, android.R.anim.slide_in_left);
                }
            }
        });

        findViewById(R.id.btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded(finalScene)) {
                    remove(finalScene, R.anim.slide_out_to_left);
                } else {
                    add(R.id.block_0, finalScene, "0", R.anim.slide_in_from_right);
                }
            }
        });
    }

    public static class PageBlockScene extends Scene {

        public static PageBlockScene newInstance(int index) {
            PageBlockScene scene = new PageBlockScene();
            Bundle bundle = new Bundle();
            bundle.putInt("index", index);
            scene.setArguments(bundle);
            return scene;
        }

        private TextView name;

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(getActivity());
            name = new TextView(getActivity());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            layout.addView(name, layoutParams);
            return layout;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            int index = getArguments().getInt("index", 0);
            getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));
            name.setText("Child Scene" + index);
        }
    }
}
