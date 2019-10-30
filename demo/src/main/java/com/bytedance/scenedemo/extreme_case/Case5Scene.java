package com.bytedance.scenedemo.extreme_case;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.ChildSceneLifecycleAdapterCallbacks;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/5/18.
 */
public class Case5Scene extends GroupScene {

    private TextView mTextView;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final FrameLayout frameLayout = new FrameLayout(requireActivity());
        frameLayout.setId(View.generateViewId());
        layout.addView(frameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));

        Button button = new Button(getActivity());
        button.setText(R.string.case_group_scene_btn_1);
        button.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        layout.addView(button, lp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scene scene = findSceneByTag("11");
                if (scene != null) {
                    remove(scene);
                } else {
                    TestGroupScene testGroupScene = new TestGroupScene();
                    testGroupScene.add(TestGroupScene.ID_0, new EmptyScene1(), "0");
                    testGroupScene.add(TestGroupScene.ID_1, new EmptyScene2(), "1");
                    add(frameLayout.getId(), testGroupScene, "11");
                }
            }
        });

        final TestGroupScene testGroupScene = new TestGroupScene();
        testGroupScene.add(TestGroupScene.ID_0, new EmptyScene1(), "0");
        testGroupScene.add(TestGroupScene.ID_1, new EmptyScene2(), "1");

        button = new Button(getActivity());
        button.setText(R.string.case_group_scene_btn_2);
        button.setAllCaps(false);
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        layout.addView(button, lp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scene scene = findSceneByTag("11");
                if (scene != null) {
                    remove(scene);
                } else {
                    add(frameLayout.getId(), testGroupScene, "11");
                }
            }
        });

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));
        layout.setFitsSystemWindows(true);

        mTextView = new TextView(requireActivity());
        ScrollView scrollView = new ScrollView(requireActivity());
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        lp.leftMargin = 30;
        lp.rightMargin = 30;
        scrollView.addView(mTextView, lp);

        layout.addView(scrollView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getNavigationScene().registerChildSceneLifecycleCallbacks(mChildSceneLifecycleAdapterCallbacks, true);

        return layout;
    }

    private StringBuilder stringBuilder = new StringBuilder();

    private void ddd() {
        mTextView.setText(stringBuilder.toString());
    }

    private ChildSceneLifecycleAdapterCallbacks mChildSceneLifecycleAdapterCallbacks = new ChildSceneLifecycleAdapterCallbacks() {
        @Override
        public void onSceneCreated(Scene scene, Bundle savedInstanceState) {
            super.onSceneCreated(scene, savedInstanceState);

            stringBuilder.append(scene.toString() + " onCreated");
            stringBuilder.append("\n");
            ddd();
        }

        @Override
        public void onSceneStarted(Scene scene) {
            super.onSceneStarted(scene);
            stringBuilder.append(scene.toString() + " onStart");
            stringBuilder.append("\n");
            ddd();
        }

        @Override
        public void onSceneResumed(Scene scene) {
            super.onSceneResumed(scene);
            stringBuilder.append(scene.toString() + " onResume");
            stringBuilder.append("\n");
            ddd();
        }

        @Override
        public void onSceneSaveInstanceState(Scene scene, Bundle outState) {
            super.onSceneSaveInstanceState(scene, outState);
            stringBuilder.append(scene.toString() + " onSaveInstanceState");
            stringBuilder.append("\n");
            ddd();
        }

        @Override
        public void onScenePaused(Scene scene) {
            super.onScenePaused(scene);
            stringBuilder.append(scene.toString() + " onPause");
            stringBuilder.append("\n");
            ddd();
        }

        @Override
        public void onSceneStopped(Scene scene) {
            super.onSceneStopped(scene);
            stringBuilder.append(scene.toString() + " onStop");
            stringBuilder.append("\n");
            ddd();
        }

        @Override
        public void onSceneDestroyed(Scene scene) {
            super.onSceneDestroyed(scene);
            stringBuilder.append(scene.toString() + " onDestroy");
            stringBuilder.append("\n");
            ddd();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getNavigationScene().unregisterChildSceneLifecycleCallbacks(mChildSceneLifecycleAdapterCallbacks);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static class TestGroupScene extends GroupScene {
        public static final int ID_0 = View.generateViewId();
        public static final int ID_1 = View.generateViewId();

        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(Color.BLACK);

            FrameLayout frameLayout0 = new FrameLayout(requireActivity());
            frameLayout0.setId(ID_0);
            layout.addView(frameLayout0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));

            FrameLayout frameLayout1 = new FrameLayout(requireActivity());
            frameLayout1.setId(ID_1);
            layout.addView(frameLayout1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));

            layout.setFitsSystemWindows(true);
            return layout;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static class EmptyScene1 extends Scene {
        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));
            layout.setFitsSystemWindows(true);
            return layout;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static class EmptyScene2 extends Scene {
        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 3));
            layout.setFitsSystemWindows(true);
            return layout;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }
}
