package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.State;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by JiangQi on 4/2/21.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationScene_Translucent_Change_Tests {
    @Test
    public void testFromNonTranslucentToTranslucent() {
        final NonTranslucentTestScene scene0 = new NonTranslucentTestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene0);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        //start state
        sceneLifecycleManager.onStart();
        //resume state
        sceneLifecycleManager.onResume();

        NonTranslucentTestScene scene1 = new NonTranslucentTestScene();
        navigationScene.push(scene1);

        assertThat(scene1.getState()).isEqualTo(State.RESUMED);

        NonTranslucentTestScene scene2 = new NonTranslucentTestScene();
        navigationScene.push(scene2);

        assertThat(scene2.getState()).isEqualTo(State.RESUMED);
        assertThat(scene1.getState()).isEqualTo(State.ACTIVITY_CREATED);

        TranslucentTestScene scene3_trans = new TranslucentTestScene();
        navigationScene.push(scene3_trans);

        assertThat(scene3_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene2.getState()).isEqualTo(State.STARTED);
        assertThat(scene1.getState()).isEqualTo(State.ACTIVITY_CREATED);//nothing change

        navigationScene.changeSceneTranslucent(scene3_trans, NavigationScene.TranslucentOption.FROM_TRANSLUCENT);
        assertThat(scene3_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene2.getState()).isEqualTo(State.ACTIVITY_CREATED);
        assertThat(scene1.getState()).isEqualTo(State.ACTIVITY_CREATED);//nothing change

        navigationScene.changeSceneTranslucent(scene3_trans, NavigationScene.TranslucentOption.TO_TRANSLUCENT);
        assertThat(scene3_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene2.getState()).isEqualTo(State.STARTED);
        assertThat(scene1.getState()).isEqualTo(State.ACTIVITY_CREATED);//nothing change

        //pause state
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    @Test
    public void testFromTranslucentToNonTranslucent() {
        final NonTranslucentTestScene bottomScene = new NonTranslucentTestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(bottomScene);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        //start state
        sceneLifecycleManager.onStart();
        //resume state
        sceneLifecycleManager.onResume();

        NonTranslucentTestScene secondScene = new NonTranslucentTestScene();
        navigationScene.push(secondScene);

        assertThat(secondScene.getState()).isEqualTo(State.RESUMED);

        NonTranslucentTestScene thirdScene = new NonTranslucentTestScene();
        navigationScene.push(thirdScene);

        assertThat(thirdScene.getState()).isEqualTo(State.RESUMED);
        assertThat(secondScene.getState()).isEqualTo(State.ACTIVITY_CREATED);

        navigationScene.changeSceneTranslucent(thirdScene, NavigationScene.TranslucentOption.TO_TRANSLUCENT);
        assertThat(thirdScene.getState()).isEqualTo(State.RESUMED);
        assertThat(secondScene.getState()).isEqualTo(State.STARTED);

        navigationScene.changeSceneTranslucent(thirdScene, NavigationScene.TranslucentOption.FROM_TRANSLUCENT);
        assertThat(thirdScene.getState()).isEqualTo(State.RESUMED);
        assertThat(secondScene.getState()).isEqualTo(State.ACTIVITY_CREATED);

        //pause state
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    @Test
    public void testTranslucent_AND_Translucent() {
        final NonTranslucentTestScene scene0 = new NonTranslucentTestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene0);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        //start state
        sceneLifecycleManager.onStart();
        //resume state
        sceneLifecycleManager.onResume();

        NonTranslucentTestScene scene1 = new NonTranslucentTestScene();
        navigationScene.push(scene1);

        assertThat(scene1.getState()).isEqualTo(State.RESUMED);

        TranslucentTestScene scene2_trans = new TranslucentTestScene();
        navigationScene.push(scene2_trans);
        TranslucentTestScene scene3_trans = new TranslucentTestScene();
        navigationScene.push(scene3_trans);
        TranslucentTestScene scene4_trans = new TranslucentTestScene();
        navigationScene.push(scene4_trans);

        assertThat(scene4_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene3_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene2_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene1.getState()).isEqualTo(State.STARTED);
        assertThat(scene0.getState()).isEqualTo(State.ACTIVITY_CREATED);

        navigationScene.changeSceneTranslucent(scene3_trans, NavigationScene.TranslucentOption.FROM_TRANSLUCENT);
        assertThat(scene4_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene3_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene2_trans.getState()).isEqualTo(State.ACTIVITY_CREATED);
        assertThat(scene1.getState()).isEqualTo(State.ACTIVITY_CREATED);
        assertThat(scene0.getState()).isEqualTo(State.ACTIVITY_CREATED);

        navigationScene.changeSceneTranslucent(scene3_trans, NavigationScene.TranslucentOption.TO_TRANSLUCENT);
        assertThat(scene4_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene3_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene2_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene1.getState()).isEqualTo(State.STARTED);
        assertThat(scene0.getState()).isEqualTo(State.ACTIVITY_CREATED);

        //pause state
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    @Test
    public void testNotVisibleTranslucent() {
        final NonTranslucentTestScene scene_0 = new NonTranslucentTestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene_0);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        //start state
        sceneLifecycleManager.onStart();
        //resume state
        sceneLifecycleManager.onResume();

        NonTranslucentTestScene scene_1 = new NonTranslucentTestScene();
        navigationScene.push(scene_1);

        assertThat(scene_1.getState()).isEqualTo(State.RESUMED);

        TranslucentTestScene scene_2_trans = new TranslucentTestScene();
        navigationScene.push(scene_2_trans);

        assertThat(scene_2_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene_1.getState()).isEqualTo(State.STARTED);

        NonTranslucentTestScene scene_3 = new NonTranslucentTestScene();
        navigationScene.push(scene_3);

        assertThat(scene_3.getState()).isEqualTo(State.RESUMED);
        assertThat(scene_2_trans.getState()).isEqualTo(State.ACTIVITY_CREATED);
        assertThat(scene_1.getState()).isEqualTo(State.ACTIVITY_CREATED);

        //nothing change
        navigationScene.changeSceneTranslucent(scene_2_trans, NavigationScene.TranslucentOption.FROM_TRANSLUCENT);
        assertThat(scene_3.getState()).isEqualTo(State.RESUMED);
        assertThat(scene_2_trans.getState()).isEqualTo(State.ACTIVITY_CREATED);
        assertThat(scene_1.getState()).isEqualTo(State.ACTIVITY_CREATED);

        //nothing change
        navigationScene.changeSceneTranslucent(scene_2_trans, NavigationScene.TranslucentOption.TO_TRANSLUCENT);
        assertThat(scene_3.getState()).isEqualTo(State.RESUMED);
        assertThat(scene_2_trans.getState()).isEqualTo(State.ACTIVITY_CREATED);
        assertThat(scene_1.getState()).isEqualTo(State.ACTIVITY_CREATED);

        //pause state
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    @Test
    public void testPartialVisibleTranslucent() {
        final NonTranslucentTestScene scene0 = new NonTranslucentTestScene();
        Pair<SceneLifecycleManager<NavigationScene>, NavigationScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene0);

        SceneLifecycleManager sceneLifecycleManager = pair.first;
        NavigationScene navigationScene = pair.second;
        navigationScene.setDefaultNavigationAnimationExecutor(null);

        //start state
        sceneLifecycleManager.onStart();
        //resume state
        sceneLifecycleManager.onResume();

        NonTranslucentTestScene scene1 = new NonTranslucentTestScene();
        navigationScene.push(scene1);

        assertThat(scene1.getState()).isEqualTo(State.RESUMED);

        TranslucentTestScene scene2_trans = new TranslucentTestScene();
        navigationScene.push(scene2_trans);

        assertThat(scene2_trans.getState()).isEqualTo(State.RESUMED);
        assertThat(scene1.getState()).isEqualTo(State.STARTED);

        TranslucentTestScene scene3 = new TranslucentTestScene();
        navigationScene.push(scene3);

        assertThat(scene3.getState()).isEqualTo(State.RESUMED);
        assertThat(scene2_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene1.getState()).isEqualTo(State.STARTED);

        navigationScene.changeSceneTranslucent(scene2_trans, NavigationScene.TranslucentOption.FROM_TRANSLUCENT);
        assertThat(scene3.getState()).isEqualTo(State.RESUMED);
        assertThat(scene2_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene1.getState()).isEqualTo(State.ACTIVITY_CREATED);

        navigationScene.changeSceneTranslucent(scene2_trans, NavigationScene.TranslucentOption.TO_TRANSLUCENT);
        assertThat(scene3.getState()).isEqualTo(State.RESUMED);
        assertThat(scene2_trans.getState()).isEqualTo(State.STARTED);
        assertThat(scene1.getState()).isEqualTo(State.STARTED);

        //pause state
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();
    }

    public static class NonTranslucentTestScene extends GroupScene {
        public final int mId;

        public NonTranslucentTestScene() {
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

    public static class TranslucentTestScene extends Scene implements SceneTranslucent {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class TestActivity extends Activity {
        public FrameLayout mFrameLayout;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mFrameLayout = new FrameLayout(this);
            setContentView(mFrameLayout);
        }
    }
}
