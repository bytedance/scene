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
import android.support.annotation.Nullable;

import com.bytedance.scene.Scene;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class Utility {
    private Utility() {

    }

    /**
     * 要求
     * 1，不能带host
     * 2，不能带端口号
     * 3，不能带除了/ : 英文和数字之外的其他字符
     */
    static void checkUrlInvalidate(@NonNull String url) {
        url = url.toLowerCase().trim();

        //万一aaa:///////////zzz怎么办
        if (!Pattern.matches("[a-zA-Z0-9]*:///[a-zA-Z0-9/]*", url)) {
            throw new IllegalArgumentException("Url format invalidate, format should be schema:///path");
        }


    }

    @Nullable
    static Class<? extends Scene> findSceneByUrlFromStringMap(Map<String, String> map, @NonNull String url) {
        url = url.toLowerCase().trim();

        Set<Map.Entry<String, String>> set = map.entrySet();
        for (Map.Entry<String, String> entry : set) {
            if (isTargetValidate(url, entry.getKey())) {
                String clazzName = entry.getValue();
                try {
                    return (Class<? extends Scene>) Class.forName(clazzName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Nullable
    static Class<? extends Scene> findSceneByUrlFromClassMap(Map<String, Class<? extends Scene>> map, @NonNull String url) {
        url = url.toLowerCase().trim();

        Set<Map.Entry<String, Class<? extends Scene>>> set = map.entrySet();
        for (Map.Entry<String, Class<? extends Scene>> entry : set) {
            if (isTargetValidate(url, entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 要求
     * 1，如果url没有scheme，补app://
     */
    private static boolean isTargetValidate(@NonNull String url, @NonNull String targetUrl) {
        url = url.toLowerCase().trim();
        targetUrl = targetUrl.toLowerCase().trim();

        if (url.equals(targetUrl)) {
            return true;
        }

        return false;
    }
}
