package com.bytedance.scene.navigation;

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

import com.bytedance.scene.navigation.NavigationSourceUtility;
import com.bytedance.scene.navigation.PublicClassScene;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.SceneLifecycleManager;
import com.bytedance.scene.Scope;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SaveAndRestoreTests {

    @Test
    public void test() {
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
            previousRootScene.setValue("Test");
            previousRootScene.getCheckBox().setChecked(true);

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
            newRootScene = (TestScene) navigationScene.getCurrentScene();
        }

        assertNotNull(previousRootScene);
        assertNotNull(newRootScene);
        assertNotSame(newRootScene, previousRootScene);
        assertTrue(newRootScene.getCheckBox().isChecked());//check View state restore
        assertEquals("Test", newRootScene.mValue);//check onSaveInstanceState and onViewStateRestored
    }

    @Test
    public void testGroupSceneSaveAndRestore() {
        Bundle bundle = new Bundle();
        TestFixIdGroupScene previousRootScene = null;
        TestScene previousChildScene = null;

        {
            SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            NavigationScene navigationScene = new NavigationScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestFixIdGroupScene.class);
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
                    navigationScene, rootScopeFactory, true, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            previousRootScene = (TestFixIdGroupScene) navigationScene.getCurrentScene();
            previousChildScene = new TestScene();
            previousRootScene.add(previousRootScene.id, previousChildScene, "TAG");

            previousChildScene.setValue("Test");
            previousChildScene.getCheckBox().setChecked(true);

            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();
        }

        assertTrue(bundle.size() > 0);

        TestFixIdGroupScene newRootScene = null;
        TestScene newChildScene = null;

        {
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestFixIdGroupScene.class);
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
            newRootScene = (TestFixIdGroupScene) navigationScene.getCurrentScene();
            newChildScene = newRootScene.findSceneByTag("TAG");
        }

        assertNotNull(previousRootScene);
        assertNotNull(previousChildScene);
        assertNotNull(newRootScene);
        assertNotNull(newChildScene);
        assertNotSame(newRootScene, previousRootScene);
        assertNotSame(newChildScene, previousChildScene);
        assertTrue(newChildScene.getCheckBox().isChecked());//check View state restore
        assertEquals("Test", newChildScene.mValue);//check onSaveInstanceState and onViewStateRestored
    }

    /**
     * GroupScene view id is not fixed, throw view not found exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGroupSceneSaveAndRestoreExceptionBecauseOfIdNotFixed() {
        Bundle bundle = new Bundle();
        TestGroupScene previousRootScene = null;
        TestScene previousChildScene = null;

        {
            SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
            NavigationScene navigationScene = new NavigationScene();
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestGroupScene.class);
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
                    navigationScene, rootScopeFactory, true, null);

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();

            previousRootScene = (TestGroupScene) navigationScene.getCurrentScene();
            previousChildScene = new TestScene();
            previousRootScene.add(previousRootScene.id, previousChildScene, "TAG");

            previousChildScene.setValue("Test");
            previousChildScene.getCheckBox().setChecked(true);

            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();
        }

        assertTrue(bundle.size() > 0);

        TestGroupScene newRootScene = null;
        TestScene newChildScene = null;

        {
            ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
            NavigationSourceUtility.TestActivity testActivity = controller.get();
            NavigationSceneOptions options = new NavigationSceneOptions(TestGroupScene.class);
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
            newRootScene = (TestGroupScene) navigationScene.getCurrentScene();
            newChildScene = newRootScene.findSceneByTag("TAG");
        }

        assertNotNull(previousRootScene);
        assertNotNull(previousChildScene);
        assertNotNull(newRootScene);
        assertNotNull(newChildScene);
        assertNotSame(newRootScene, previousRootScene);
        assertNotSame(newChildScene, previousChildScene);
        assertTrue(newChildScene.getCheckBox().isChecked());//check View state restore
        assertEquals("Test", newChildScene.mValue);//check onSaveInstanceState and onViewStateRestored
    }

    @Test
    public void testParentSceneViewStateBundleShouldNotSaveChildSceneViewState() {
        SceneLifecycleManager<NavigationScene> sceneLifecycleManager = new SceneLifecycleManager<>();
        NavigationScene navigationScene = new NavigationScene();
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationSceneOptions options = new NavigationSceneOptions(TestFixIdGroupScene.class);
        options.setUsePostInLifecycle(true);
        navigationScene.setArguments(options.toBundle());

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        navigationScene.setDefaultNavigationAnimationExecutor(null);

        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, rootScopeFactory, true, null);

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestFixIdGroupScene parentGroupScene = (TestFixIdGroupScene) navigationScene.getCurrentScene();
        TestScene childScene = new TestScene();
        parentGroupScene.add(parentGroupScene.id, childScene, "TAG");

        childScene.setValue("Test");
        childScene.getCheckBox().setChecked(true);

        Bundle bundle = new Bundle();
        sceneLifecycleManager.onSaveInstanceState(bundle);
        ArrayList<Bundle> navigationSceneChildrenSceneBundleList = bundle.getParcelableArrayList(ParcelConstants.KEY_NAVIGATION_SCENE_MANAGER_TAG);
        Bundle groupSceneBundle = navigationSceneChildrenSceneBundleList.get(0);
        SparseArray<Parcelable> groupSceneViewStateArray = groupSceneBundle.getSparseParcelableArray(ParcelConstants.KEY_SCENE_VIEWS_TAG);
        //Parent Scene's view state bundle should not have child Scene's view state, child Scene' view state is saved to child Scene's bundle.
        assertNull(groupSceneViewStateArray.get(childScene.requireView().getId()));
        ArrayList<Bundle> groupSceneChildrenSceneBundleList = groupSceneBundle.getParcelableArrayList(ParcelConstants.KEY_GROUP_SCENE_MANAGER_TAG);
        Bundle groupChildSceneBundle = groupSceneChildrenSceneBundleList.get(0);
        SparseArray<Parcelable> childSceneViewStateArray = groupChildSceneBundle.getSparseParcelableArray(ParcelConstants.KEY_SCENE_VIEWS_TAG);
        //Child Scene's view state should be saved in this SparseArray
        assertNotNull(childSceneViewStateArray.get(childScene.requireView().getId()));
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

    @Test(expected = IllegalArgumentException.class)
    public void crashAnonymousClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        });//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashGroupSceneAnonymousClass() {
        TestGroupScene testGroupScene = new TestGroupScene();
        createNavigationScene(testGroupScene);
        testGroupScene.add(testGroupScene.id, new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }, "TAG");//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashLocalClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);

        class LocalClass extends Scene {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        }

        navigationScene.push(new LocalClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPrivateMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PrivateMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPackageMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PackageMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashProtectedMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new ProtectedMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPublicMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPackageStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PackageStaticMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashProtectedStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new ProtectedStaticMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPrivateStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PrivateStaticMemberClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPackageClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PackageClass());//crash
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashPublicStaticMemberNotEmptyParamsConstructorClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicStaticMemberNotEmptyParamsConstructorClass(1));//crash
    }

    @Test
    public void testPublicStaticMemberClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicStaticMemberClass());//ok
    }

    @Test
    public void testPublicClass() {
        TestScene testScene = new TestScene();
        NavigationScene navigationScene = createNavigationScene(testScene);
        navigationScene.push(new PublicClassScene());//ok
    }

    class PackageMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    protected class ProtectedMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    private class PrivateMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public class PublicMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    static class PackageStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    protected static class ProtectedStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    private static class PrivateStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class PublicStaticMemberClass extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
        }
    }

    public static class PublicStaticMemberNotEmptyParamsConstructorClass extends Scene {
        public PublicStaticMemberNotEmptyParamsConstructorClass(int value) {

        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new View(requireSceneContext());
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