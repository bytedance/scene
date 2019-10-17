package com.bytedance.scenedemo.group.basic_usage;

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
import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/7/18.
 */
public class GroupSceneBasicUsageSample extends AppCompatScene {

    @NonNull
    @Override
    public ViewGroup onCreateContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.group_scene_page_block, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(R.string.main_part_btn_child_scene);

        BasicUsageScene scene = createOrReuse("0", new Creator<BasicUsageScene>() {
            @Override
            public BasicUsageScene call() {
                return BasicUsageScene.newInstance(0);
            }
        });
        BasicUsageScene scene1 = createOrReuse("1", new Creator<BasicUsageScene>() {
            @Override
            public BasicUsageScene call() {
                return BasicUsageScene.newInstance(1);
            }
        });
        BasicUsageScene scene2 = createOrReuse("2", new Creator<BasicUsageScene>() {
            @Override
            public BasicUsageScene call() {
                return BasicUsageScene.newInstance(2);
            }
        });
        BasicUsageScene scene3 = createOrReuse("3", new Creator<BasicUsageScene>() {
            @Override
            public BasicUsageScene call() {
                return BasicUsageScene.newInstance(3);
            }
        });

        if (!isAdded(scene))
            add(R.id.block_0, scene, "0");
        if (!isAdded(scene1))
            add(R.id.block_1, scene1, "1");
        if (!isAdded(scene2))
            add(R.id.block_2, scene2, "2");
        if (!isAdded(scene3))
            add(R.id.block_3, scene3, "3");

        final BasicUsageScene finalScene = scene;
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

    public static class BasicUsageScene extends Scene {

        public static BasicUsageScene newInstance(int index) {
            BasicUsageScene scene = new BasicUsageScene();
            Bundle bundle = new Bundle();
            bundle.putInt("index", index);
            scene.setArguments(bundle);
            return scene;
        }

        private TextView name;

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
