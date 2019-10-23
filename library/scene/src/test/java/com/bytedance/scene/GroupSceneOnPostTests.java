package com.bytedance.scene;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.utlity.ViewIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GroupSceneOnPostTests {
    @Test
    public void test() {
        final StringBuilder stringBuilder = new StringBuilder();
        final int id = ViewIdGenerator.generateViewId();
        final GroupScene childScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                stringBuilder.append("Child_onActivityCreated ");
            }

            @Override
            protected void onPostActivityCreated() {
                super.onPostActivityCreated();
                stringBuilder.append("Child_onPostActivityCreated ");
            }

            @Override
            public void onStart() {
                super.onStart();
                stringBuilder.append("Child_onStart ");
            }

            @Override
            protected void onPostStart() {
                super.onPostStart();
                stringBuilder.append("Child_onPostStart ");
            }

            @Override
            public void onResume() {
                super.onResume();
                stringBuilder.append("Child_onResume ");
            }

            @Override
            protected void onPostResume() {
                super.onPostResume();
                stringBuilder.append("Child_onPostResume ");
            }
        };
        GroupScene groupScene = new GroupScene() {
            @NonNull
            @Override
            public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
                return new FrameLayout(requireSceneContext());
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                getView().setId(id);
                this.add(id, childScene, "TAG");
            }

            @Override
            public void onActivityCreated(@Nullable Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                stringBuilder.append("Parent_onActivityCreated ");
            }

            @Override
            protected void onPostActivityCreated() {
                super.onPostActivityCreated();
                stringBuilder.append("Parent_onPostActivityCreated ");
            }

            @Override
            public void onStart() {
                super.onStart();
                stringBuilder.append("Parent_onStart ");
            }

            @Override
            protected void onPostStart() {
                super.onPostStart();
                stringBuilder.append("Parent_onPostStart ");
            }

            @Override
            public void onResume() {
                super.onResume();
                stringBuilder.append("Parent_onResume ");
            }

            @Override
            protected void onPostResume() {
                super.onPostResume();
                stringBuilder.append("Parent_onPostResume");
            }
        };

        NavigationSourceUtility.createFromSceneLifecycleManager(groupScene);

        String expertValue = "Parent_onActivityCreated Child_onActivityCreated Child_onPostActivityCreated "
                + "Parent_onPostActivityCreated Parent_onStart Child_onStart Child_onPostStart Parent_onPostStart "
                + "Parent_onResume Child_onResume Child_onPostResume Parent_onPostResume";
        assertEquals(expertValue, stringBuilder.toString());
    }
}
