package com.bytedance.scenedemo.livedata;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

public class LiveDataScene extends GroupScene {
    private Button button;
    private MutableLiveData<String> liveData = new MutableLiveData<>();
    private int value = 0;
    private FrameLayout frameLayout;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setPadding(0, 100, 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        button = new Button(getActivity());
        layout.addView(button);

        frameLayout = new FrameLayout(requireActivity());
        layout.addView(frameLayout, new ViewGroup.LayoutParams(500, 500));
        frameLayout.setBackgroundColor(Color.RED);
        frameLayout.setId(ViewIdGenerator.generateViewId());
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        liveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                button.setText(s);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveData.setValue(String.valueOf(value));
                value++;
            }
        });

        getScope().register("livedata", liveData);
        Scene scene = new TestShowScene();
        add(frameLayout.getId(), scene, "TestShowScene");
        remove(scene);
        add(frameLayout.getId(), scene, "TestShowScene");

        Scene hiddenScene = new TestHiddenScene();
        add(frameLayout.getId(), hiddenScene, "TestHiddenScene");
        hide(hiddenScene);
    }

    public static class TestHiddenScene extends Scene {

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            LiveData<String> liveData = getScope().getService("livedata");
            liveData.observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    Toast.makeText(requireActivity(), "TestHiddenScene " + s, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static class TestShowScene extends Scene {

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            LiveData<String> liveData = getScope().getService("livedata");
            liveData.observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    Toast.makeText(requireActivity(), "TestShowScene " + s, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
