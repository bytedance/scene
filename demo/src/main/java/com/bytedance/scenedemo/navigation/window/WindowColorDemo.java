package com.bytedance.scenedemo.navigation.window;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/21/18.
 */
public class WindowColorDemo extends Scene {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(ColorUtil.getMaterialColor(getResources(), 2));
            getActivity().getWindow().setNavigationBarColor(ColorUtil.getMaterialColor(getResources(), 3));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }
}
