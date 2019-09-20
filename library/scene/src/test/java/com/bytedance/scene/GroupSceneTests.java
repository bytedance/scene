package com.bytedance.scene;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.group.Creator;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneTests {
    @Test
    public void testCreateOrReuse() {
        final int id = ViewIdGenerator.generateViewId();
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                getView().setId(id);
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(groupScene);

        TestScene scene = new TestScene();
        groupScene.add(id, scene, "TAG");

        TestScene createdScene = groupScene.createOrReuse("TAG", new Creator<TestScene>() {
            @Override
            public TestScene call() {
                return new TestScene();
            }
        });
        assertSame(scene, createdScene);
        groupScene.remove(scene);

        TestScene newCreatedScene = groupScene.createOrReuse("TAG", new Creator<TestScene>() {
            @Override
            public TestScene call() {
                return new TestScene();
            }
        });
        assertNotNull(newCreatedScene);
        assertNotSame(scene, newCreatedScene);
    }

    public static class TestScene extends Scene {

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}
