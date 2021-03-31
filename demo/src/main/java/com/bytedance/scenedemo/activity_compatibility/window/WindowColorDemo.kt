package com.bytedance.scenedemo.activity_compatibility.window;

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
            requireActivity().getWindow().setStatusBarColor(ColorUtil.getMaterialColor(getResources(), 2));
            requireActivity().getWindow().setNavigationBarColor(ColorUtil.getMaterialColor(getResources(), 3));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(requireActivity().getWindow().getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(requireActivity().getWindow().getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }
}
