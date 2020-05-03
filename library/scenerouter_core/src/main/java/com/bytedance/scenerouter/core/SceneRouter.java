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
import com.bytedance.scene.navigation.NavigationScene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SceneRouter {
    private static class UrlMapContainer {
        private Map<String, Class<? extends Scene>> map = new HashMap<>();
    }

    @NonNull
    private final NavigationScene mNavigationScene;
    private final List<Interceptor> mInterceptorList = new ArrayList<>();

    SceneRouter(@NonNull NavigationScene navigationScene) {
        this.mNavigationScene = navigationScene;
    }

    public void register(@NonNull String url, @NonNull Class<? extends Scene> clazz) {
        Utility.throwExceptionIfUrlIncorrect(url);
        UrlMapContainer mapContainer = null;
        if (this.mNavigationScene.getScope().hasServiceInMyScope(UrlMapContainer.class)) {
            mapContainer = this.mNavigationScene.getScope().getService(UrlMapContainer.class);
        } else {
            mapContainer = new UrlMapContainer();
            this.mNavigationScene.getScope().register(UrlMapContainer.class, mapContainer);
        }

        if (mapContainer.map.containsKey(url.trim())) {
            throw new IllegalArgumentException("url duplicate");
        } else {
            mapContainer.map.put(url.trim(), clazz);
        }
    }

    public void unRegister(@NonNull String url) {
        UrlMapContainer mapContainer = null;
        if (this.mNavigationScene.getScope().hasServiceInMyScope(UrlMapContainer.class)) {
            mapContainer = this.mNavigationScene.getScope().getService(UrlMapContainer.class);
        } else {
            return;
        }
        mapContainer.map.remove(url);
    }

    public void registerInterceptor(@NonNull Interceptor interceptor) {
        this.mInterceptorList.add(interceptor);
    }

    public void unRegisterInterceptor(@NonNull Interceptor interceptor) {
        this.mInterceptorList.remove(interceptor);
    }

    public Task url(@NonNull String url) {
        return new Task(this.mNavigationScene, this.mFinder, url, this.mInterceptorAdapter);
    }

    private final TargetSceneFinder mFinder = new TargetSceneFinder() {
        @Override
        public Class<? extends Scene> findSceneClassByUrl(@NonNull String url) {
            return findSceneByUrl(url);
        }
    };

    private final InterceptorAdapter mInterceptorAdapter = new InterceptorAdapter() {
        @Override
        public void run(@NonNull TaskInfo taskInfo, @NonNull final ContinueTask task) {
            if (mInterceptorList.size() == 0) {
                task.onContinue();
            } else {
                List<Interceptor> copy = new ArrayList<>(mInterceptorList);
                dispatchInterceptor(taskInfo, task, copy, 0);
            }
        }
    };

    private static void dispatchInterceptor(final TaskInfo taskInfo, final ContinueTask runnable, final List<Interceptor> interceptorList, final int currentIndex) {
        Interceptor interceptor = interceptorList.get(currentIndex);
        interceptor.process(taskInfo, new ContinueTask() {
            boolean finished = false;

            @Override
            public void onContinue() {
                if (finished) {
                    throw new IllegalStateException("onContinue or onFail is already invoked");
                }
                finished = true;
                if ((currentIndex + 1) == interceptorList.size()) {
                    runnable.onContinue();
                } else {
                    dispatchInterceptor(taskInfo, runnable, interceptorList, currentIndex + 1);
                }
            }

            @Override
            public void onFail(@Nullable Exception exception) {
                if (finished) {
                    throw new IllegalStateException("onContinue or onFail is already invoked");
                }
                finished = true;
                runnable.onFail(exception);
            }
        });
    }

    private Class<? extends Scene> findSceneByUrl(@NonNull String url) {
        Class<? extends Scene> clazz = null;
        UrlMapContainer mapContainer = this.mNavigationScene.getScope().getService(UrlMapContainer.class);
        if (mapContainer != null) {
            clazz = Utility.findSceneByUrlFromClassMap(mapContainer.map, url);
        }

        if (clazz != null) {
            return clazz;
        } else {
            return StaticPluginUrlMap.findSceneByUrl(url);
        }
    }
}
