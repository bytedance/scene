package com.bytedance.scene;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class NavigationSourceUtility {
    public static GroupScene createFromSceneLifecycleManager(final Scene childScene) {
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = createFromInitSceneLifecycleManager(childScene);
        SceneLifecycleManager<GroupScene> sceneLifecycleManager = pair.first;
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();
        return pair.second;
    }

    public static Pair<SceneLifecycleManager<GroupScene>, GroupScene> createFromSceneLifecycleManagerWithManager(final Scene childScene) {
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = createFromInitSceneLifecycleManager(childScene);
        return pair;
    }

    public static Pair<SceneLifecycleManager<GroupScene>, GroupScene> createFromInitSceneLifecycleManager(final Scene childScene) {
        return createFromInitSceneLifecycleManager(childScene, false);
    }

    public static Pair<SceneLifecycleManager<GroupScene>, GroupScene> createFromInitSceneLifecycleManager(final Scene childScene, boolean separateCreate) {
        ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).create().start().resume();
        TestActivity testActivity = controller.get();
        TestGroupScene groupScene = new TestGroupScene();
        groupScene.disableSupportRestore();
        groupScene.setSeparateCreateFromCreateView(separateCreate);

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        groupScene.add(groupScene.mId, childScene, "childScene");

        SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                groupScene, rootScopeFactory,
                false, null);
        return Pair.create(sceneLifecycleManager, (GroupScene) groupScene);
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
