package com.bytedance.scenedemo.group.all;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.Creator;
import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.MainListScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/7/18.
 */
public class AllViewPagerChildScene extends UserVisibleHintGroupScene {
    public static AllViewPagerChildScene newInstance(int index) {
        AllViewPagerChildScene scene = new AllViewPagerChildScene();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        scene.setArguments(bundle);
        return scene;
    }

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.group_scene_page_block_all, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final int index = getArguments().getInt("index");

        Scene scene0 = createOrReuse("0", new Creator<Scene>() {
            @Override
            public Scene call() {
                return PageBlockScene.newInstance(index + 0);
            }
        });

        Scene scene1 = createOrReuse("1", new Creator<Scene>() {
            @Override
            public Scene call() {
                return PageBlockScene.newInstance(index + 1);
            }
        });

        Scene scene2 = createOrReuse("2", new Creator<Scene>() {
            @Override
            public Scene call() {
                return PageBlockScene.newInstance(index + 2);
            }
        });

        Scene scene3 = createOrReuse("3", new Creator<Scene>() {
            @Override
            public Scene call() {
                return PageBlockScene.newInstance(index + 3);
            }
        });

        if (!isAdded(scene0))
            add(R.id.block_0, scene0, "0");
        if (!isAdded(scene1))
            add(R.id.block_1, scene1, "1");
        if (!isAdded(scene2))
            add(R.id.block_2, scene2, "2");
        if (!isAdded(scene3))
            add(R.id.block_3, scene3, "3");
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
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNavigationScene().push(MainListScene.class);
                }
            });
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
