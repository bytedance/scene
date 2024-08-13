package com.bytedance.scene.navigation.saveandrestore;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.navigation.NavigationSourceUtility;
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

    //test disableSupportRestore GroupScene be added into a support restore NavigationScene
    @Test
    public void testDisableSaveAndRestore() {
        Bundle bundle = new Bundle();
        TestScene previousRootScene = null;

        {
            SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            NavigationScene navigationScene = new NavigationScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
            options.setUsePostInLifecycle(true);
            navigationScene.setArguments(options.toBundle());

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());

            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    navigationScene, rootScopeFactory,
                    true, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            previousRootScene = (TestScene) navigationScene.getCurrentScene();

            //add restore disabled scene
            DisableSaveRestoreGroupScene disableSaveRestoreGroupScene = new DisableSaveRestoreGroupScene("123");
            navigationScene.push(disableSaveRestoreGroupScene);

            //add restore enabled scene
            SupportSaveRestoreGroupScene supportSaveRestoreGroupScene = new SupportSaveRestoreGroupScene();
            Bundle childSceneBundle = new Bundle();
            childSceneBundle.putString("key", "value");
            supportSaveRestoreGroupScene.setArguments(childSceneBundle);
            navigationScene.push(supportSaveRestoreGroupScene);

            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();
        }

        assertTrue(bundle.size() > 0);

        TestScene newRootScene = null;

        {
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
            options.setUsePostInLifecycle(true);
            NavigationScene navigationScene = new NavigationScene();
            navigationScene.setArguments(options.toBundle());

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            navigationScene.setDefaultNavigationAnimationExecutor(new NoAnimationExecutor());
            SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    navigationScene, rootScopeFactory, true, bundle);
            assertEquals(2, navigationScene.getSceneList().size());

            newRootScene = (TestScene) navigationScene.getSceneList().get(0);

            SupportSaveRestoreGroupScene supportSaveRestoreGroupScene = (SupportSaveRestoreGroupScene) navigationScene.getSceneList().get(1);
            assertEquals("value", supportSaveRestoreGroupScene.getArguments().getString("key"));//check onSaveInstanceState and onViewStateRestored
        }

        assertNotNull(previousRootScene);
        assertNotNull(newRootScene);
        assertNotSame(newRootScene, previousRootScene);
    }

    public static NavigationScene createNavigationScene(final Scene rootScene) {
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        NavigationScene navigationScene = new NavigationScene();
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
        options.setUsePostInLifecycle(true);
        navigationScene.setArguments(options.toBundle());

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(null);
        navigationScene.setRootSceneComponentFactory(new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                return rootScene;
            }
        });
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, rootScopeFactory, true, null);

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        return navigationScene;
    }

    public static class TestScene extends Scene {
        public final int mId;
        private String mValue;

        public TestScene() {
            mId = 1;
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new CheckBox(requireSceneContext());
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.setId(mId);
        }
    }

    public static class TestGroupScene extends GroupScene {
        public final int id = ViewIdGenerator.generateViewId();

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