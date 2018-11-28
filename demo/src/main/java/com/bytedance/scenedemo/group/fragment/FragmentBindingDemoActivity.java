package com.bytedance.scenedemo.group.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.SceneDelegate;
import com.bytedance.scene.NavigationSceneAvailableCallback;
import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.group.ChildSceneLifecycleAdapterCallbacks;
import com.bytedance.scene.interfaces.SceneNavigation;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.ui.NavigationSceneCompatUtility;
import com.bytedance.scene.utlity.ViewIdGenerator;
import com.bytedance.scenedemo.group.viewpager.ViewPagerGroupScene;

/**
 * Created by JiangQi on 9/5/18.
 */
public class FragmentBindingDemoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new AFragment()).commitNow();
    }

    public static class AFragment extends Fragment {
        private int id = ViewIdGenerator.generateViewId();
        private SceneDelegate delegate;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setId(id);
            return frameLayout;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            delegate = NavigationSceneCompatUtility.setupWithFragment(this, id, savedInstanceState, ViewPagerGroupScene.class,
                    new SceneComponentFactory() {
                        @Override
                        public Scene instantiateScene(ClassLoader cl, String className, Bundle bundle) {
                            return null;
                        }
                    }, null, false);
            delegate.setNavigationSceneAvailableCallback(new NavigationSceneAvailableCallback() {
                @Override
                public void onNavigationSceneAvailable(NavigationScene navigationScene) {
                    navigationScene.registerChildSceneLifecycleCallbacks(new ChildSceneLifecycleAdapterCallbacks() {
                        @Override
                        public void onSceneResumed(Scene scene) {
                            super.onSceneResumed(scene);

                        }
                    }, false);
                    Log.e("ddd", "sss");
                }
            });
            SceneNavigation navigation = delegate.getNavigationScene();
            Log.e("ddd", "sss");
        }
    }
}
