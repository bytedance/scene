package com.bytedance.scenedemo.livedata;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

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
        scene.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            public void a1() {
                Log.e("Lifecycle", "ON_CREATE");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void a2() {
                Log.e("Lifecycle", "ON_START");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void a3() {
                Log.e("Lifecycle", "ON_RESUME");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void a4() {
                Log.e("Lifecycle", "ON_PAUSE");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void a5() {
                Log.e("Lifecycle", "ON_STOP");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void a6() {
                Log.e("Lifecycle", "ON_DESTROY");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            public void a7() {
                Log.e("Lifecycle", "ON_ANY");
            }
        });
        Log.e("Lifecycle", "add");
        add(frameLayout.getId(), scene, "TestShowScene");
        Log.e("Lifecycle", "remove");
        remove(scene);
        Log.e("Lifecycle", "add");
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
