package com.bytedance.scene.navigation.saveandrestore;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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


/**
 * non translucent Scene + non translucent Scene
 * <p>
 * after restore, change non translucent Scene to translucent
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OnlyRestoreVisibleSceneTests_ChangeTranslucent {

    /**
     * non translucent Scene + non translucent Scene
     */
    @Test
    public void testRestore_NonTranslucent_NonTranslucent() {
        Bundle bundle = new Bundle();
        TestScene previousRootScene = null;

        {
            SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            NavigationScene navigationScene = new NavigationScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestScene.class);
            options.setOnlyRestoreVisibleScene(true);
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
            options.setOnlyRestoreVisibleScene(true);
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

            newRootScene = (TestScene) navigationScene.getSceneList().get(0);

            SupportSaveRestoreGroupScene supportSaveRestoreGroupScene = (SupportSaveRestoreGroupScene) navigationScene.getSceneList().get(1);
            assertEquals("value", supportSaveRestoreGroupScene.getArguments().getString("key"));//check onSaveInstanceState and onViewStateRestored

            assertNotNull(previousRootScene);
            assertNotNull(newRootScene);
            assertNotSame(newRootScene, previousRootScene);

            //import!, the first scene is not created because the top scene is not translucent
            assertEquals(2, navigationScene.getSceneList().size());
            assertNull(navigationScene.getSceneList().get(0).getView());
            assertNotNull(navigationScene.getSceneList().get(1).getView());

            navigationScene.changeSceneTranslucent(navigationScene.getSceneList().get(1), NavigationScene.TranslucentOption.TO_TRANSLUCENT);
            assertEquals(2, navigationScene.getSceneList().size());
            assertNotNull(navigationScene.getSceneList().get(0).getView());
            assertNotNull(navigationScene.getSceneList().get(1).getView());

            ViewGroup parentContainer = (ViewGroup) navigationScene.getCurrentScene().getView().getParent();
            View firstSceneView = navigationScene.getSceneList().get(0).getView();
            View secondSceneView = navigationScene.getSceneList().get(1).getView();

            //check view index
            assertEquals(0, parentContainer.indexOfChild(firstSceneView));
            assertEquals(1, parentContainer.indexOfChild(secondSceneView));
        }
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
}