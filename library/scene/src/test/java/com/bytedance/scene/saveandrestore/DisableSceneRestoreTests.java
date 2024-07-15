package com.bytedance.scene.saveandrestore;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.NavigationSourceUtility;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DisableSceneRestoreTests {

    //test disableSupportRestore GroupScene be added into a support restore GroupScene
    @Test
    public void testDisableSaveAndRestore() {
        Bundle bundle = new Bundle();
        SupportSaveRestoreGroupScene previousChildScene = null;

        {
            SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            TestRootGroupScene rootScene = new TestRootGroupScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory,
                    true, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            SupportSaveRestoreGroupScene childScene = new SupportSaveRestoreGroupScene();
            Bundle childSceneBundle = new Bundle();
            childSceneBundle.putString("key", "value");
            childScene.setArguments(childSceneBundle);
            rootScene.add(rootScene.id, childScene, "supportRestoreTag");

            DisableSaveRestoreGroupScene disableSaveRestoreGroupScene = new DisableSaveRestoreGroupScene("123");
            rootScene.add(rootScene.id, disableSaveRestoreGroupScene, "disableRestoreTag");

            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();
        }

        assertTrue(bundle.size() > 0);

        SupportSaveRestoreGroupScene newChildScene = null;
        {
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            TestRootGroupScene rootScene = new TestRootGroupScene();

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory, true, bundle);
            newChildScene = (SupportSaveRestoreGroupScene) rootScene.getSceneList().get(0);
            assertEquals(1, rootScene.getSceneList().size());
        }

        assertNotNull(newChildScene);
        assertNotSame(newChildScene, previousChildScene);
        assertEquals("value", newChildScene.getArguments().getString("key"));//check onSaveInstanceState and onViewStateRestored
    }

    public static class TestRootGroupScene extends GroupScene {
        public final int id = android.R.id.content;

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(id);
            return layout;
        }
    }

    public static class SupportSaveRestoreGroupScene extends Scene {
        public final int id = ViewIdGenerator.generateViewId();

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(id);
            return layout;
        }
    }


    public static class DisableSaveRestoreGroupScene extends Scene {
        public final int id = ViewIdGenerator.generateViewId();

        public DisableSaveRestoreGroupScene(String value) {
            this.disableSceneRestore();
        }

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(id);
            return layout;
        }
    }
}