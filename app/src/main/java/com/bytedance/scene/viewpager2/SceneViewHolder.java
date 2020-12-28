package com.bytedance.scene.viewpager2;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.ViewHolder} implementation for handling {@link com.bytedance.scene.Scene}s. Used in
 * {@link SceneAdapter}.
 */
public final class SceneViewHolder extends RecyclerView.ViewHolder {
    private SceneViewHolder(@NonNull FrameLayout container) {
        super(container);
    }

    @NonNull
    static SceneViewHolder create(@NonNull ViewGroup parent) {
        FrameLayout container = new FrameLayout(parent.getContext());
        container.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        container.setId(ViewCompat.generateViewId());
        container.setSaveEnabled(false);
        return new SceneViewHolder(container);
    }

    @NonNull
    FrameLayout getContainer() {
        return (FrameLayout) itemView;
    }
}