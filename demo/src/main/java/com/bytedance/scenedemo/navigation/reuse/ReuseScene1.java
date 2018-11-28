package com.bytedance.scenedemo.navigation.reuse;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.group.ReuseGroupScene;

/**
 * Created by JiangQi on 8/13/18.
 */
public class ReuseScene1 extends ReuseGroupScene {
    @NonNull
    @Override
    protected ViewGroup onCreateNewView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SystemClock.sleep(1000);
        ViewGroup view = new FrameLayout(getActivity());
        view.setBackgroundColor(Color.YELLOW);
        return view;
    }
}
