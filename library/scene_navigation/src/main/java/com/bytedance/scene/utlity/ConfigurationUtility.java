/*
 * Copyright (C) 2008 The Android Open Source Project
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
package com.bytedance.scene.utlity;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.util.ArrayList;

/**
 * Created by jiangqi on 2024/11/6
 *
 * @author jiangqi@bytedance.com
 */
/**
 * @hide
 */
@RestrictTo(LIBRARY)
public class ConfigurationUtility {
    public static final int CONFIG_ASSETS_PATHS = 0x80000000;
    public static final int CONFIG_WINDOW_CONFIGURATION = 0x20000000;

    public static final int CONFIG_FONT_WEIGHT_ADJUSTMENT = 0x10000000;

    public static final int CONFIG_GRAMMATICAL_GENDER = 0x8000;

    /**
     * update to api 34
     *
     * @param diff
     * @return
     */
    @NonNull
    public static String configurationDiffToString(int diff) {
        ArrayList<String> list = new ArrayList<>();
        if ((diff & ActivityInfo.CONFIG_MCC) != 0) {
            list.add("CONFIG_MCC");
        }
        if ((diff & ActivityInfo.CONFIG_MNC) != 0) {
            list.add("CONFIG_MNC");
        }
        if ((diff & ActivityInfo.CONFIG_LOCALE) != 0) {
            list.add("CONFIG_LOCALE");
        }
        if ((diff & ActivityInfo.CONFIG_TOUCHSCREEN) != 0) {
            list.add("CONFIG_TOUCHSCREEN");
        }
        if ((diff & ActivityInfo.CONFIG_KEYBOARD) != 0) {
            list.add("CONFIG_KEYBOARD");
        }
        if ((diff & ActivityInfo.CONFIG_KEYBOARD_HIDDEN) != 0) {
            list.add("CONFIG_KEYBOARD_HIDDEN");
        }
        if ((diff & ActivityInfo.CONFIG_NAVIGATION) != 0) {
            list.add("CONFIG_NAVIGATION");
        }
        if ((diff & ActivityInfo.CONFIG_ORIENTATION) != 0) {
            list.add("CONFIG_ORIENTATION");
        }
        if ((diff & ActivityInfo.CONFIG_SCREEN_LAYOUT) != 0) {
            list.add("CONFIG_SCREEN_LAYOUT");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if ((diff & ActivityInfo.CONFIG_COLOR_MODE) != 0) {
                list.add("CONFIG_COLOR_MODE");
            }
        }
        if ((diff & ActivityInfo.CONFIG_UI_MODE) != 0) {
            list.add("CONFIG_UI_MODE");
        }
        if ((diff & ActivityInfo.CONFIG_SCREEN_SIZE) != 0) {
            list.add("CONFIG_SCREEN_SIZE");
        }
        if ((diff & ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE) != 0) {
            list.add("CONFIG_SMALLEST_SCREEN_SIZE");
        }
        if ((diff & ActivityInfo.CONFIG_DENSITY) != 0) {
            list.add("CONFIG_DENSITY");
        }
        if ((diff & ActivityInfo.CONFIG_LAYOUT_DIRECTION) != 0) {
            list.add("CONFIG_LAYOUT_DIRECTION");
        }
        if ((diff & ActivityInfo.CONFIG_FONT_SCALE) != 0) {
            list.add("CONFIG_FONT_SCALE");
        }
        if ((diff & CONFIG_ASSETS_PATHS) != 0) {
            list.add("CONFIG_ASSETS_PATHS");
        }
        if ((diff & CONFIG_WINDOW_CONFIGURATION) != 0) {
            list.add("CONFIG_WINDOW_CONFIGURATION");
        }
        if (Build.VERSION.SDK_INT >= 31) {
            if ((diff & CONFIG_FONT_WEIGHT_ADJUSTMENT) != 0) {
                list.add("CONFIG_AUTO_BOLD_TEXT");
            }
        }
        if (Build.VERSION.SDK_INT >= 34) {
            if ((diff & CONFIG_GRAMMATICAL_GENDER) != 0) {
                list.add("CONFIG_GRAMMATICAL_GENDER");
            }
        }
        if (list.size() > 0) {
            return "{" + TextUtils.join(", ", list) + "}";
        } else {
            return "";
        }
    }

    public static int removePrivateDiff(int diff) {
        //remove private diff ActivityInfo.CONFIG_WINDOW_CONFIGURATION
        diff = (diff & ~ConfigurationUtility.CONFIG_WINDOW_CONFIGURATION);
        return diff;
    }


    public static int getConfigChanges(Activity activity) {
        try {
            PackageManager pm = activity.getPackageManager();
            ActivityInfo activityInfo = pm.getActivityInfo(getComponentName(activity, activity.getClass().getName()), 0);
            return activityInfo.configChanges;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static ComponentName getComponentName(Activity activity, String activityName) {
        String pkgName = activity.getPackageName();
        return new ComponentName(pkgName, activityName);
    }
}
