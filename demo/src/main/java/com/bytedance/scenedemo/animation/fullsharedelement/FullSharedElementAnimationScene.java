package com.bytedance.scenedemo.animation.fullsharedelement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.bytedance.scene.animation.SharedElementSceneTransitionExecutor;
import com.bytedance.scene.animation.interaction.scenetransition.AutoSceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.BackgroundRecolor;
import com.bytedance.scene.animation.interaction.scenetransition.ChangeBounds;
import com.bytedance.scene.animation.interaction.scenetransition.ChangeImageTransform;
import com.bytedance.scene.animation.interaction.scenetransition.ChangeTransform;
import com.bytedance.scene.animation.interaction.scenetransition.SceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.SceneTransitionSet;
import com.bytedance.scene.animation.interaction.scenetransition.TextRecolor;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.Slide;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 10/19/18.
 */
public class FullSharedElementAnimationScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.full_shared_element_0, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayMap<String, SceneTransition> map = new ArrayMap<>();

        SceneTransitionSet a = new SceneTransitionSet();
        a.addSceneTransition(new ChangeTransform());
        a.addSceneTransition(new ChangeBounds());
        map.put("haha", a);
        map.put("imageView", new AutoSceneTransition().addSceneTransition(new ChangeImageTransform()));
        map.put("haha_parent", new AutoSceneTransition().addSceneTransition(new BackgroundRecolor()));
        map.put("shared_element_text_no_size_change", new AutoSceneTransition().addSceneTransition(new BackgroundRecolor()).addSceneTransition(new TextRecolor()));
//        map.put("shared_element_text_no_size_change", new AutoSceneTransition());

        final SharedElementSceneTransitionExecutor sharedElementSceneTransitionExecutor = new SharedElementSceneTransitionExecutor(map, new Slide());

        findViewById(R.id.haha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(new FullSharedElementAnimationScene1(), new PushOptions.Builder().setAnimation(sharedElementSceneTransitionExecutor).build());
            }
        });
    }
}
