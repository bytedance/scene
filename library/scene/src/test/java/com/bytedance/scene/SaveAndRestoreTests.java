package com.bytedance.scene;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.ViewIdGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SaveAndRestoreTests {

    @Test
    public void test() {
        Bundle bundle = new Bundle();
        TestScene previousChildScene = null;

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
                    rootScene, rootScopeFactory,
                    true, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            TestScene childScene = new TestScene();
            Bundle childSceneBundle = new Bundle();
            childSceneBundle.putString("key", "value");
            childScene.setArguments(childSceneBundle);
            rootScene.add(rootScene.id, childScene, "tag");

            previousChildScene = childScene;
            previousChildScene.setValue("Test");
            previousChildScene.getCheckBox().setChecked(true);

            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();
        }

        assertTrue(bundle.size() > 0);

        TestScene newChildScene = null;

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
            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory, true, bundle);
            newChildScene = (TestScene) rootScene.getSceneList().get(0);
        }

        assertNotNull(previousChildScene);
        assertNotNull(newChildScene);
        assertNotSame(newChildScene, previousChildScene);
        assertTrue(newChildScene.getCheckBox().isChecked());//check View state restore
        assertEquals("Test", newChildScene.mValue);//check onSaveInstanceState and onViewStateRestored
        assertEquals("value", newChildScene.getArguments().getString("key"));//check onSaveInstanceState and onViewStateRestored
    }

    /**
     * GroupScene view id is not fixed, throw view not found exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGroupSceneSaveAndRestoreExceptionBecauseOfIdNotFixed() {
        Bundle bundle = new Bundle();
        TestScene previousChildScene = null;

        {
            SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            TestGroupScene rootScene = new TestGroupScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };


            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory, true, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            previousChildScene = new TestScene();
            rootScene.add(rootScene.id, previousChildScene, "TAG");

            previousChildScene.setValue("Test");
            previousChildScene.getCheckBox().setChecked(true);

            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();
        }

        assertTrue(bundle.size() > 0);

        TestScene newChildScene = null;

        {
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            TestGroupScene rootScene = new TestGroupScene();

            Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
                @Override
                public Scope getRootScope() {
                    return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
                }
            };

            SceneLifecycleManager<GroupScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory, true, bundle);
            newChildScene = rootScene.findSceneByTag("TAG");
        }

        assertNotNull(previousChildScene);
        assertNotNull(newChildScene);
        assertNotSame(newChildScene, previousChildScene);
        assertTrue(newChildScene.getCheckBox().isChecked());//check View state restore
        assertEquals("Test", newChildScene.mValue);//check onSaveInstanceState and onViewStateRestored
    }

    @Test
    public void testParentSceneViewStateBundleShouldNotSaveChildSceneViewState() {
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
                rootScene, rootScopeFactory, true, null);

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestScene childScene = new TestScene();
        rootScene.add(rootScene.id, childScene, "TAG");

        childScene.setValue("Test");
        childScene.getCheckBox().setChecked(true);

        Bundle bundle = new Bundle();
        sceneLifecycleManager.onSaveInstanceState(bundle);
        ArrayList<Bundle> groupSceneChildrenSceneBundleList = bundle.getParcelableArrayList(ParcelConstants.KEY_GROUP_SCENE_MANAGER_TAG);
        Bundle groupSceneBundle = groupSceneChildrenSceneBundleList.get(0);
        SparseArray<Parcelable> childSceneViewStateArray = groupSceneBundle.getSparseParcelableArray(ParcelConstants.KEY_SCENE_VIEWS_TAG);
        //Child Scene's view state should be saved in this SparseArray
        assertNotNull(childSceneViewStateArray.get(childScene.requireView().getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashGroupSceneAnonymousClass() {
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
                rootScene, rootScopeFactory, true, null);

        rootScene.add(rootScene.id, new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }, "TAG");//crash
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

class PackageClass extends Scene {
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireSceneContext());
    }
}