package com.bytedance.scenedemo.lifecycle;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/6/18.
 */
public class ChildSceneLifecycleCallbacksDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText(R.string.lifecycle_callback_tip);
        layout.addView(textView);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene0.class);
            }
        });

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

        layout.setFitsSystemWindows(true);

        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNavigationScene().registerChildSceneLifecycleCallbacks(mChildSceneLifecycleCallbacks, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getNavigationScene().unregisterChildSceneLifecycleCallbacks(mChildSceneLifecycleCallbacks);
    }

    public static class EmptyScene0 extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(getActivity());
            textView.setText(getNavigationScene().getStackHistory());
            layout.addView(textView);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNavigationScene().push(EmptyScene1.class);
                }
            });

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            layout.setFitsSystemWindows(true);

            return layout;
        }
    }

    public static class EmptyScene1 extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(getActivity());
            textView.setText(getNavigationScene().getStackHistory());
            layout.addView(textView);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            layout.setFitsSystemWindows(true);

            return layout;
        }
    }

    private ChildSceneLifecycleCallbacks mChildSceneLifecycleCallbacks = new ChildSceneLifecycleCallbacks() {
        @Override
        public void onSceneCreated(Scene scene, Bundle savedInstanceState) {
            log("Scene", scene.toString() + " onSceneCreated");
        }

        @Override
        public void onSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
            log("Scene", scene.toString() + " onSceneActivityCreated");
        }

        @Override
        public void onSceneStarted(Scene scene) {
            log("Scene", scene.toString() + " onSceneStarted");
        }

        @Override
        public void onSceneResumed(Scene scene) {
            log("Scene", scene.toString() + " onSceneResumed");
        }

        @Override
        public void onSceneSaveInstanceState(Scene scene, Bundle outState) {
            log("Scene", scene.toString() + " onSceneSaveInstanceState");
        }

        @Override
        public void onScenePaused(Scene scene) {
            log("Scene", scene.toString() + " onScenePaused");
        }

        @Override
        public void onSceneStopped(Scene scene) {
            log("Scene", scene.toString() + " onSceneStopped");
        }

        @Override
        public void onSceneViewDestroyed(Scene scene) {
            log("Scene", scene.toString() + " onSceneViewDestroyed");
        }

        @Override
        public void onSceneDestroyed(@NonNull Scene scene) {
            log("Scene", scene.toString() + " onSceneDestroyed");
        }
    };

    private static void log(String name, String tag) {
        Log.e(name, tag);
    }
}
