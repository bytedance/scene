package com.bytedance.scene;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utility.TestActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LifeCycleFrameLayoutTests {
    @Test
    public void test() {
        final Scene rootScene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(rootScene.getClass());
        navigationScene.setArguments(options.toBundle());

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(rootScene.getClass().getName())) {
                    return rootScene;
                }
                return null;
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(null);

        LifeCycleFrameLayout frameLayout = new LifeCycleFrameLayout(testActivity) {
            @Override
            public boolean isSupportRestore() {
                return false;
            }
        };

        frameLayout.setNavigationScene(navigationScene);
        frameLayout.setRootSceneComponentFactory(sceneComponentFactory);
        frameLayout.setRootScopeFactory(rootScopeFactory);

        frameLayout.onActivityCreated(null);
        assertEquals(State.ACTIVITY_CREATED, rootScene.getState());
        frameLayout.onStart();
        assertEquals(State.STARTED, rootScene.getState());
        frameLayout.onResume();
        assertEquals(State.RESUMED, rootScene.getState());
        frameLayout.onPause();
        assertEquals(State.STARTED, rootScene.getState());
        frameLayout.onStop();
        assertEquals(State.ACTIVITY_CREATED, rootScene.getState());
        frameLayout.onDestroyView();
        assertEquals(State.NONE, rootScene.getState());
    }
}
