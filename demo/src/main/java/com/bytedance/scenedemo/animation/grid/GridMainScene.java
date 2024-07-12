/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bytedance.scenedemo.animation.grid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.view.ViewCompat;

import com.bytedance.scene.animation.SharedElementSceneTransitionExecutor;
import com.bytedance.scene.animation.interaction.scenetransition.AutoSceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.SceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.Fade;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.ktx.NavigationSceneExtensionsKt;
import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.other_library.glide.SceneGlideExtensionsKt;

public class GridMainScene extends AppCompatScene {

    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.grid, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Setup the GridView and set the adapter
        GridView grid = findViewById(R.id.grid);
        grid.setOnItemClickListener(mOnItemClickListener);
        GridAdapter adapter = new GridAdapter();
        grid.setAdapter(adapter);
    }

    private final AdapterView.OnItemClickListener mOnItemClickListener
            = new AdapterView.OnItemClickListener() {

        /**
         * Called when an item in the {@link GridView} is clicked. Here will launch
         * the {@link GridDetailScene}, using the Scene Transition animation functionality.
         */
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Item item = (Item) adapterView.getItemAtPosition(position);

            NavigationSceneExtensionsKt.requireNavigationScene(GridMainScene.this).disableSupportRestore();

            ArrayMap<String, SceneTransition> map = new ArrayMap<>();
            map.put(GridDetailScene.VIEW_NAME_HEADER_IMAGE + item.getId(), new AutoSceneTransition());
            map.put(GridDetailScene.VIEW_NAME_HEADER_TITLE + item.getId(), new AutoSceneTransition());
            SharedElementSceneTransitionExecutor sharedElementSceneTransitionExecutor = new SharedElementSceneTransitionExecutor(map, new Fade());
            NavigationSceneExtensionsKt.requireNavigationScene(GridMainScene.this)
                    .push(new GridDetailScene(item), new PushOptions.Builder().setAnimation(sharedElementSceneTransitionExecutor).build());
        }
    };

    /**
     * {@link BaseAdapter} which displays items.
     */
    private class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return Item.ITEMS.length;
        }

        @Override
        public Item getItem(int position) {
            return Item.ITEMS[position];
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.grid_item, viewGroup, false);
            }

            final Item item = getItem(position);

            // Load the thumbnail image
            ImageView image = view.findViewById(R.id.imageview_item);
            SceneGlideExtensionsKt.requireGlide(GridMainScene.this).load(item.getThumbnailUrl()).into(image);

            // Set the TextView's contents
            TextView name = view.findViewById(R.id.textview_name);
            name.setText(item.getName());

            ViewCompat.setTransitionName(image, GridDetailScene.VIEW_NAME_HEADER_IMAGE + item.getId());
            ViewCompat.setTransitionName(name, GridDetailScene.VIEW_NAME_HEADER_TITLE + item.getId());

            return view;
        }
    }
}
