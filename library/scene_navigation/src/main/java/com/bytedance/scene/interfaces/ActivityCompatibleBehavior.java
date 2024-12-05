package com.bytedance.scene.interfaces;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;

/**
 * Created by jiangqi on 2024/5/21
 *
 * @author jiangqi@bytedance.com
 */
public interface ActivityCompatibleBehavior {
    void onConfigurationChanged(@NonNull Configuration newConfig);

    /**
     * <p>An scene can never receive a new intent in the resumed state. You can count on
     * {@link Scene#onResume} being called after this method, though not necessarily immediately after
     * the completion this callback. If the scene was resumed, it will be paused and new arguments
     * will be delivered, followed by {@link Scene#onResume}. If the scene wasn't in the resumed
     * state, then new arguments can be delivered immediately, with {@link Scene#onResume()} called
     * sometime later when scene becomes active again.
     *
     * <p>Note that {@link Scene#getArguments} still returns the original arguments.  You
     * can use {@link Scene#setArguments} to update it to this new arguments.
     *
     * @param arguments The new arguments that was started for the scene.
     * @see Scene#getArguments()
     * @see Scene#setArguments
     * @see Scene#onResume
     */
    void onNewIntent(@Nullable Bundle arguments);

    void onWindowFocusChanged(boolean hasFocus);
}
