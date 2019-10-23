package com.bytedance.scene;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.animatorexecutor.AlphaNavigationSceneAnimatorExecutor;
import com.bytedance.scene.animation.animatorexecutor.HorizontalTransitionAnimatorExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.CancellationSignal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AnimationExecutorTests {
    @Test
    public void testNavigationSceneDefaultAnimation() {
        final Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };

        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        NavigationScene navigationScene = new NavigationScene();
        NavigationSceneOptions options = new NavigationSceneOptions(scene.getClass());
        navigationScene.setArguments(options.toBundle());

        NavigationScene.NavigationSceneHost navigationSceneHost = new NavigationScene.NavigationSceneHost() {
            @Override
            public boolean isSupportRestore() {
                return false;
            }
        };

        Scope.RootScopeFactory rootScopeFactory = new Scope.RootScopeFactory() {
            @Override
            public Scope getRootScope() {
                return Scope.DEFAULT_ROOT_SCOPE_FACTORY.getRootScope();
            }
        };

        SceneComponentFactory sceneComponentFactory = new SceneComponentFactory() {
            @Override
            public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                if (className.equals(scene.getClass().getName())) {
                    return scene;
                }
                return null;
            }
        };

        SceneLifecycleManager sceneLifecycleManager = new SceneLifecycleManager();
        sceneLifecycleManager.onActivityCreated(testActivity, testActivity.mFrameLayout,
                navigationScene, navigationSceneHost, rootScopeFactory,
                sceneComponentFactory, null);

        assertNotNull(navigationScene.getDefaultNavigationAnimationExecutor());
        NavigationAnimationExecutor navigationAnimationExecutor = new NavigationAnimationExecutor() {
            @Override
            public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
                return false;
            }

            @Override
            public void executePushChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull Runnable endAction, @NonNull CancellationSignal cancellationSignal) {

            }

            @Override
            public void executePopChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull Runnable endAction, @NonNull CancellationSignal cancellationSignal) {

            }
        };
        navigationScene.setDefaultNavigationAnimationExecutor(navigationAnimationExecutor);
        assertSame(navigationAnimationExecutor, navigationScene.getDefaultNavigationAnimationExecutor());
    }

    @Test
    public void testGetNavigationAnimationExecutor() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        final NavigationAnimationExecutor navigationAnimationExecutor = new AlphaNavigationSceneAnimatorExecutor();
        navigationScene.setDefaultNavigationAnimationExecutor(navigationAnimationExecutor);
        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                assertNull(requireNavigationScene().getNavigationAnimationExecutor(this));
            }
        });
    }

    @Test
    public void testOverrideNavigationAnimationExecutor() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        final NavigationAnimationExecutor navigationAnimationExecutor = new AlphaNavigationSceneAnimatorExecutor();
        navigationScene.setDefaultNavigationAnimationExecutor(navigationAnimationExecutor);
        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                final NavigationAnimationExecutor overrideNavigationAnimationExecutor = new HorizontalTransitionAnimatorExecutor();
                requireNavigationScene().overrideNavigationAnimationExecutor(this, overrideNavigationAnimationExecutor);
                assertSame(overrideNavigationAnimationExecutor, requireNavigationScene().getNavigationAnimationExecutor(this));
            }
        });
    }

    @Test
    public void testPushOptionsSetAnimation() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        NavigationScene navigationScene = NavigationSourceUtility.createFromSceneLifecycleManager(scene);
        navigationScene.setDefaultNavigationAnimationExecutor(null);
        final NavigationAnimationExecutor navigationAnimationExecutor = new AlphaNavigationSceneAnimatorExecutor();
        navigationScene.push(new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                assertSame(navigationAnimationExecutor, requireNavigationScene().getNavigationAnimationExecutor(this));
            }
        }, new PushOptions.Builder().setAnimation(navigationAnimationExecutor).build());
    }
}
