package com.bytedance.scene;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bytedance.scene.group.Creator;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void testIsShow() {
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
        assertTrue(groupScene.isShow(scene));
        assertFalse(groupScene.isShow(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }));
    }

    @Test
    public void testFindSceneByTag_Tag_null() {
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }
        };
        NavigationSourceUtility.createFromSceneLifecycleManager(groupScene);
        assertNull(groupScene.findSceneByTag(null));
    }

    @Test
    public void testFindTagByScene() {
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
        assertEquals("TAG", groupScene.findTagByScene(scene));

        TestScene secondScene = new TestScene();
        groupScene.add(id, secondScene, "TAG2");
        assertNotEquals("TAG", groupScene.findTagByScene(secondScene));

        assertNull(groupScene.findTagByScene(new TestScene()));
    }

    @Test
    public void testAnimation() {
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
        groupScene.add(id, scene, "TAG", android.R.anim.fade_in);
        assertNotNull(scene.getView().getAnimation());
        groupScene.hide(scene, android.R.anim.fade_out);
        groupScene.show(scene, android.R.anim.fade_in);
        assertNotNull(scene.getView().getAnimation());
        groupScene.remove(scene, android.R.anim.fade_out);

        groupScene.add(id, scene, "TAG", 0);
        assertNull(scene.getView().getAnimation());
        groupScene.hide(scene, 0);
        groupScene.show(scene, 0);
        assertNull(scene.getView().getAnimation());
        groupScene.remove(scene, 0);
    }

    public static class TestScene extends Scene {

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }
}
