package com.bytedance.scenedemo.animation;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.animation.SharedElementSceneTransitionExecutor;
import com.bytedance.scene.animation.interaction.scenetransition.AutoSceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.SceneTransition;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/23/18.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class TransitionDemoNew extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.layout_transition_0, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ArrayMap<String, SceneTransition> map = new ArrayMap<>();
        map.put("wo", new AutoSceneTransition());
        map.put("imageview", new AutoSceneTransition());

        getView().findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                getNavigationScene().push(TransitionDemo2.class, null, new PushOptions.Builder().setAnimation(new SharedElementSceneTransitionExecutor(map, null)).build());
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));
    }

    public static class TransitionDemo2 extends GroupScene {

        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return (ViewGroup) inflater.inflate(R.layout.layout_transition_1, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));
        }
    }
}