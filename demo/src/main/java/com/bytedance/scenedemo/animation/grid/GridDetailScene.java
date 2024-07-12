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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.bytedance.scene.ktx.SceneExtensionsKt;
import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.other_library.glide.SceneGlideExtensionsKt;

/**
 * Our secondary Activity which is launched from {@link GridMainScene}. Has a simple detail UI
 * which has a large banner image, title and body text.
 */
public class GridDetailScene extends AppCompatScene {
    // View name of the header image. Used for activity scene transitions
    public static final String VIEW_NAME_HEADER_IMAGE = "detail:header:image";

    // View name of the header title. Used for activity scene transitions
    public static final String VIEW_NAME_HEADER_TITLE = "detail:header:title";

    private ImageView mHeaderImageView;
    private TextView mHeaderTitle;

    private final Item mItem;

    public GridDetailScene(Item item) {
        this.mItem = item;
    }

    @Nullable
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.grid_details, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHeaderImageView = findViewById(R.id.imageview_header);
        mHeaderTitle = findViewById(R.id.textview_title);

        ViewCompat.setTransitionName(mHeaderImageView, VIEW_NAME_HEADER_IMAGE + mItem.getId());
        ViewCompat.setTransitionName(mHeaderTitle, VIEW_NAME_HEADER_TITLE + mItem.getId());

        loadItem();
    }

    private void loadItem() {
        // Set the title TextView to the item's name and author
        mHeaderTitle.setText(getString(R.string.image_header, mItem.getName(), mItem.getAuthor()));

        loadThumbnail();
        SceneExtensionsKt.postDelayed(this, new Runnable() {
            @Override
            public void run() {
                // As the transition has ended, we can now load the full-size image
                loadFullSizeImage();
            }
        }, 300);
    }

    /**
     * Load the item's thumbnail image into our {@link ImageView}.
     */
    private void loadThumbnail() {
        SceneGlideExtensionsKt.requireGlide(GridDetailScene.this)
                .load(mItem.getThumbnailUrl())
                .placeholder(mHeaderImageView.getDrawable())
                .dontAnimate()
                .into(mHeaderImageView);
    }

    /**
     * Load the item's full-size image into our {@link ImageView}.
     */
    private void loadFullSizeImage() {
        SceneGlideExtensionsKt.requireGlide(GridDetailScene.this)
                .load(mItem.getPhotoUrl())
                .placeholder(mHeaderImageView.getDrawable())
                .dontAnimate()
                .into(mHeaderImageView);
    }
}
