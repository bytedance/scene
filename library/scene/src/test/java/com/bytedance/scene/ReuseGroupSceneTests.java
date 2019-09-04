package com.bytedance.scene;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.group.InheritedScene;
import com.bytedance.scene.group.ReuseGroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ReuseGroupSceneTests {

    @Test
    public void test() {
        TestScene groupScene = new TestScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(groupScene);

        TestChildScene childScene = new TestChildScene();
        groupScene.add(groupScene.mId, childScene, "Child");
        assertSame(childScene.getState(), State.RESUMED);
        View childSceneView = childScene.getView();

        groupScene.remove(childScene);
        assertSame(childScene.getState(), State.NONE);
        assertNull(childScene.getView());

        groupScene.add(groupScene.mId, childScene, "Child");
        assertSame(childScene.getState(), State.RESUMED);
        assertNotNull(childScene.getView());

        assertSame(childScene.getView(), childSceneView);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException() {
        TestScene groupScene = new TestScene();
        NavigationSourceUtility.createFromSceneLifecycleManager(groupScene);

        TestChildScene childScene = new TestChildScene();
        groupScene.add(groupScene.mId, childScene, "Child");
        Activity activity = childScene.requireActivity();

        View childSceneView = childScene.getView();
        groupScene.remove(childScene);
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.addView(childSceneView);
        groupScene.add(groupScene.mId, childScene, "Child");
    }

    public static class TestScene extends InheritedScene {
        public final int mId;

        public TestScene() {
            mId = ViewIdGenerator.generateViewId();
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireSceneContext());
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.setId(mId);
        }
    }

    public static class TestChildScene extends ReuseGroupScene {
        @NonNull
        @Override
        protected ViewGroup onCreateNewView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new FrameLayout(requireSceneContext());
        }
    }
}
