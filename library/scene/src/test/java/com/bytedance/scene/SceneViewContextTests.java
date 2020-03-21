package com.bytedance.scene;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneViewContextTests {
    @Test
    public void test() {
        Scene activityContextScene = new Scene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return (ViewGroup) inflater.inflate(TestResources.getLayout(this, "layout_place_holder_view"), container, false);
            }
        };
        NavigationSourceUtility.createFromInitSceneLifecycleManager(activityContextScene);
        assertSame(activityContextScene.requireView().getContext(), activityContextScene.requireActivity());
    }

    @Test
    public void testTheme() {
        Scene sceneContextScene = new Scene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                setTheme(android.R.style.DeviceDefault_ButtonBar);
                return (ViewGroup) inflater.inflate(TestResources.getLayout(this, "layout_place_holder_view"), container, false);
            }
        };
        NavigationSourceUtility.createFromInitSceneLifecycleManager(sceneContextScene);
        assertSame(sceneContextScene.requireView().getContext(), sceneContextScene.requireSceneContext());
    }

    @Test
    public void testTheme2() {
        Scene sceneContextScene = new Scene() {
            @Override
            public void onAttach() {
                super.onAttach();
                setTheme(android.R.style.DeviceDefault_ButtonBar);
            }

            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return (ViewGroup) inflater.inflate(TestResources.getLayout(this, "layout_place_holder_view"), container, false);
            }
        };
        NavigationSourceUtility.createFromInitSceneLifecycleManager(sceneContextScene);
        assertSame(sceneContextScene.requireView().getContext(), sceneContextScene.requireSceneContext());
    }
}
