package com.bytedance.scene;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ThemeTests {
    @Test
    public void test() {
        TestGroupScene scene = new TestGroupScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        scene.add(scene.mId, new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                setTheme(TestResources.getStyle(this, "ut_theme"));
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                assertEquals(TestResources.getStyle(this, "ut_theme"), getTheme());
            }
        }, "tag");
    }

    public static class TestGroupScene extends GroupScene {
        public final int mId;

        public TestGroupScene() {
            mId = ViewIdGenerator.generateViewId();
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(mId);
            return layout;
        }
    }
}
