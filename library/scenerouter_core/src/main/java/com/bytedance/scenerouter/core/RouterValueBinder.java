package com.bytedance.scenerouter.core;

import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;

import java.lang.reflect.Method;

final class RouterValueBinder {
    private static final String SCENE_ROUTER_BIND_CLASS_NAME_PREFIX = "SceneRouter_";
    private static final String SCENE_ROUTER_BIND_CLASS_METHOD = "bind";

    static void bind(@NonNull Scene scene) {
        String clazzName = scene.getClass().getPackage().getName() + "." + SCENE_ROUTER_BIND_CLASS_NAME_PREFIX + scene.getClass().getSimpleName();
        try {
            Class clazz = Class.forName(clazzName);
            Object object = clazz.newInstance();
            Method method = clazz.getMethod(SCENE_ROUTER_BIND_CLASS_METHOD, scene.getClass());
            method.invoke(object, scene);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
