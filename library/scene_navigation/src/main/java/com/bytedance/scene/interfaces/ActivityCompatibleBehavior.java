package com.bytedance.scene.interfaces;

import android.content.res.Configuration;

import androidx.annotation.NonNull;

/**
 * Created by jiangqi on 2024/7/4
 *
 * @author jiangqi@bytedance.com
 */
public interface ActivityCompatibleBehavior {
    void onConfigurationChanged(@NonNull Configuration newConfig);
}
