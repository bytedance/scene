package com.bytedance.scene.saveandrestore;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.bytedance.scene.SceneStateSaveStrategy;
import com.bytedance.scene.Scope;
import com.bytedance.scene.group.GroupScene;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * Created by jiangqi on 2023/3/30
 *
 * @author jiangqi@bytedance.com
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneStateSaveStrategyTests {
    @Test
    public void testSceneStateSaveStrategy() {
        Bundle bundle = new Bundle();
        TestScene previousChildScene = null;

        final boolean[] sceneStateSaveStrategyInvoked = new boolean[3];
        SceneStateSaveStrategy sceneStateSaveStrategy = new SceneStateSaveStrategy() {
            @Nullable
            @Override
            public Bundle onRestoreInstanceState(@NonNull Bundle hostSavedInstanceState) {
                sceneStateSaveStrategyInvoked[0] = true;
                return hostSavedInstanceState;
            }

            @Override
            public void onSaveInstanceState(@NonNull Bundle hostOutState, @NonNull Bundle sceneOutState) {
                sceneStateSaveStrategyInvoked[1] = true;
                hostOutState.putAll(sceneOutState);
            }

            @Override
            public void onClear() {
                sceneStateSaveStrategyInvoked[2] = true;
            }
        };

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
                    rootScene, rootScopeFactory, sceneStateSaveStrategy,
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

            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onSaveInstanceState(bundle);
            sceneLifecycleManager.onDestroyView();

            assertTrue(bundle.size() > 0);
            assertTrue(sceneStateSaveStrategyInvoked[1]);
            assertFalse(sceneStateSaveStrategyInvoked[2]);
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
            sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                    rootScene, rootScopeFactory, sceneStateSaveStrategy, true, bundle);

            assertTrue(sceneStateSaveStrategyInvoked[0]);

            TestScene newChildScene = (TestScene) rootScene.getSceneList().get(0);


            assertNotNull(previousChildScene);
            assertNotNull(newChildScene);
            assertNotSame(newChildScene, previousChildScene);
            assertTrue(newChildScene.getCheckBox().isChecked());//check View state restore
            assertEquals("Test", newChildScene.mValue);//check onSaveInstanceState and onViewStateRestored
            assertEquals("value", newChildScene.getArguments().getString("key"));//check onSaveInstanceState and onViewStateRestored

            sceneLifecycleManager.onStart();
            sceneLifecycleManager.onResume();
            sceneLifecycleManager.onPause();
            sceneLifecycleManager.onStop();
            sceneLifecycleManager.onDestroyView();

            assertTrue(sceneStateSaveStrategyInvoked[2]);
        }
    }

    @Test
    public void testSceneStateSaveStrategyMakeSureOnClearInvoked() {
        Bundle bundle = new Bundle();

        final boolean[] sceneStateSaveStrategyInvoked = new boolean[3];
        SceneStateSaveStrategy sceneStateSaveStrategy = new SceneStateSaveStrategy() {
            @Nullable
            @Override
            public Bundle onRestoreInstanceState(@NonNull Bundle hostSavedInstanceState) {
                sceneStateSaveStrategyInvoked[0] = true;
                return hostSavedInstanceState;
            }

            @Override
            public void onSaveInstanceState(@NonNull Bundle hostOutState, @NonNull Bundle sceneOutState) {
                sceneStateSaveStrategyInvoked[1] = true;
                hostOutState.putAll(sceneOutState);
            }

            @Override
            public void onClear() {
                sceneStateSaveStrategyInvoked[2] = true;
            }
        };

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
                rootScene, rootScopeFactory, sceneStateSaveStrategy,
                true, null);

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestScene childScene = new TestScene();
        Bundle childSceneBundle = new Bundle();
        childSceneBundle.putString("key", "value");
        childScene.setArguments(childSceneBundle);
        rootScene.add(rootScene.id, childScene, "tag");

        //start new page
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onSaveInstanceState(bundle);

        //return to previous page
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        //exit page
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();

        assertTrue(bundle.size() > 0);
        assertTrue(sceneStateSaveStrategyInvoked[1]);
        assertTrue(sceneStateSaveStrategyInvoked[2]);
    }

    @Test
    public void testSceneStateSaveStrategyMakeSureOnClearNotInvoked() {
        Bundle bundle = new Bundle();

        final boolean[] sceneStateSaveStrategyInvoked = new boolean[3];
        SceneStateSaveStrategy sceneStateSaveStrategy = new SceneStateSaveStrategy() {
            @Nullable
            @Override
            public Bundle onRestoreInstanceState(@NonNull Bundle hostSavedInstanceState) {
                sceneStateSaveStrategyInvoked[0] = true;
                return hostSavedInstanceState;
            }

            @Override
            public void onSaveInstanceState(@NonNull Bundle hostOutState, @NonNull Bundle sceneOutState) {
                sceneStateSaveStrategyInvoked[1] = true;
                hostOutState.putAll(sceneOutState);
            }

            @Override
            public void onClear() {
                sceneStateSaveStrategyInvoked[2] = true;
            }
        };

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
                rootScene, rootScopeFactory, sceneStateSaveStrategy,
                true, null);

        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        TestScene childScene = new TestScene();
        Bundle childSceneBundle = new Bundle();
        childSceneBundle.putString("key", "value");
        childScene.setArguments(childSceneBundle);
        rootScene.add(rootScene.id, childScene, "tag");

        //start new page
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onSaveInstanceState(bundle);

        //return to previous page
        sceneLifecycleManager.onStart();
        sceneLifecycleManager.onResume();

        //switch to other app and killed by oom killer
        sceneLifecycleManager.onSaveInstanceState(bundle);
        sceneLifecycleManager.onPause();
        sceneLifecycleManager.onStop();
        sceneLifecycleManager.onDestroyView();

        assertTrue(bundle.size() > 0);
        assertTrue(sceneStateSaveStrategyInvoked[1]);
        assertFalse(sceneStateSaveStrategyInvoked[2]);
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
