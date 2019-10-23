package com.bytedance.scenedemo.animation.fullsharedelement;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 10/19/18.
 */
public class FullSharedElementAnimationScene1 extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.full_shared_element_1, container, false);
    }
}
