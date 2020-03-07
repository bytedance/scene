/*
 * Copyright (C) 2019 ByteDance Inc
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
package com.bytedance.scenerouter.core;

import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;

import java.util.Map;
import java.util.concurrent.Callable;

final class StaticPluginUrlMap {
    private static Class sClazz = null;
    private static Map<String, String> sMap = null;

    static Class<? extends Scene> findSceneByUrl(@NonNull String url) {
        init();
        return Utility.findSceneByUrlFromStringMap(sMap, url);
    }

    private static void init() {
        if (sClazz == null) {
            try {
                sClazz = Class.forName("com.bytedance.scene.scene_router.SceneRouterMap");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (sMap == null && sClazz != null) {
            Object object = null;
            try {
                object = sClazz.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            if (object != null) {
                try {
                    sMap = ((Callable<Map<String, String>>) object).call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
