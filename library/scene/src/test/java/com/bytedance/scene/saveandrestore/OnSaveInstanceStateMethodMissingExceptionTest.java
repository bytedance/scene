package com.bytedance.scene.saveandrestore;

import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.NavigationSourceUtility;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.exceptions.OnSaveInstanceStateMethodMissingException;
import com.bytedance.scene.group.GroupScene;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * Created by jiangqi on 2023/6/5
 *
 * @author jiangqi@bytedance.com
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OnSaveInstanceStateMethodMissingExceptionTest {
    @Test(expected = OnSaveInstanceStateMethodMissingException.class)
    public void testException() {
        Bundle bundle = new Bundle();

        {
            SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            TestFixIdGroupScene rootScene = new TestFixIdGroupScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory, null,
                    true, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            TestScene childScene = new TestScene();
            Bundle childSceneBundle = new Bundle();
            childSceneBundle.putString("key", "value");
            childScene.setArguments(childSceneBundle);
            rootScene.add(rootScene.id, childScene, "tag");

            childScene.setValue("Test");
            childScene.getCheckBox().setChecked(true);

            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
//            sceneLifecycleManager.onSaveInstanceState(bundle); //skip onSaveInstanceState
            sceneLifecycleManager.onDestroyView();
        }

        {
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            TestFixIdGroupScene rootScene = new TestFixIdGroupScene();

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            //throw OnSaveInstanceStateMethodMissingException
            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory, null, true, bundle);
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

        public void setValue(String value) {
            this.mValue = value;
        }

        public CheckBox getCheckBox() {
            return (CheckBox) getView();
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("value", mValue);
        }

        @Override
        public void onViewStateRestored(@NonNull Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);
            this.mValue = savedInstanceState.getString("value");
        }
    }

    public static class TestFixIdGroupScene extends GroupScene {
        public final int id = android.R.id.content;

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout layout = new FrameLayout(requireSceneContext());
            layout.setId(id);
            return layout;
        }
    }
}
