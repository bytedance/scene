package com.bytedance.scene.navigation;

import android.content.res.Configuration;
import android.support.annotation.NonNull;

/**
 * Created by JiangQi on 9/4/18.
 */
public interface ConfigurationChangedListener {
    void onConfigurationChanged(@NonNull Configuration newConfig);
}
