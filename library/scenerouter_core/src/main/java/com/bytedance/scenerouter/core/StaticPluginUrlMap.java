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
