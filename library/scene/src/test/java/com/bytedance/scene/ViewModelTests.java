package com.bytedance.scene;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.SceneViewTreeViewModelStoreOwner;
import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ViewModelTests {
    @Test
    public void testViewModel() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                TestViewModel testViewModel = SceneViewModelProviders.of(this).get(TestViewModel.class);
                assertNotNull(testViewModel);
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);

        Truth.assertThat(SceneViewTreeViewModelStoreOwner.get(scene.requireView())).isEqualTo(scene);
    }

    @Test
    public void testViewModelFactory() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                TestViewModel testViewModel = SceneViewModelProviders.of(this, new NewInstanceFactory()).get(TestViewModel.class);
                assertNotNull(testViewModel);
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
    }

    @Test
    public void testViewModelDestroy() {
        final TestViewModel testViewModel = new TestViewModel();
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                SceneViewModelProviders.of(this, new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) testViewModel;
                    }
                }).get(testViewModel.getClass());
            }
        };
        Pair<SceneLifecycleManager<GroupScene>, GroupScene> pair = NavigationSourceUtility.createFromInitSceneLifecycleManager(scene);
        SceneLifecycleManager lifecycleManager = pair.first;
        lifecycleManager.onStart();
        lifecycleManager.onResume();
        lifecycleManager.onPause();
        lifecycleManager.onStop();
        lifecycleManager.onDestroyView();
        assertTrue(testViewModel.value[0]);
    }

    @Test(expected = IllegalStateException.class)
    public void testViewModelBeforeOnAttach() {
        Scene scene = new Scene() {
            @NonNull
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new View(requireSceneContext());
            }
        };
        SceneViewModelProviders.of(scene).get(TestViewModel.class);
    }

    public static class TestViewModel extends ViewModel {
        final boolean[] value = new boolean[1];

        @Override
        protected void onCleared() {
            super.onCleared();
            value[0] = true;
        }
    }

    private static class NewInstanceFactory implements ViewModelProvider.Factory {

        @SuppressWarnings("ClassNewInstance")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection TryWithIdenticalCatches
            try {
                return modelClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
