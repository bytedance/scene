package com.bytedance.scenedemo.group.inherited;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.Creator;
import com.bytedance.scene.group.InheritedScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/21/18.
 */
public class InheritedDemo extends InheritedScene {

    TextView summary;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.layout_inherited, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        summary = getView().findViewById(R.id.summary);
        summary.setText(R.string.part_inherited_tip);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Child0Scene scene = createOrReuse("0", new Creator<Child0Scene>() {
            @Override
            public Child0Scene call() {
                return Child0Scene.newInstance(0);
            }
        });
        Child0Scene scene1 = createOrReuse("1", new Creator<Child0Scene>() {
            @Override
            public Child0Scene call() {
                return Child0Scene.newInstance(1);
            }
        });
        Child0Scene scene2 = createOrReuse("2", new Creator<Child0Scene>() {
            @Override
            public Child0Scene call() {
                return Child0Scene.newInstance(2);
            }
        });
        Child0Scene scene3 = createOrReuse("3", new Creator<Child0Scene>() {
            @Override
            public Child0Scene call() {
                return Child0Scene.newInstance(3);
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
    }

    public static class Child0Scene extends Scene {

        public static Child0Scene newInstance(int index) {
            Child0Scene scene = new Child0Scene();
            Bundle bundle = new Bundle();
            bundle.putInt("index", index);
            scene.setArguments(bundle);
            return scene;
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(getActivity());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final int index = getArguments().getInt("index", 0);
            getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));

            getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InheritedDemo inheritedScene = getScope().getService(InheritedDemo.class);
                    inheritedScene.summary.setText("Child Scene" + index);
                }
            });
        }
    }
}