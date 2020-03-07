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
