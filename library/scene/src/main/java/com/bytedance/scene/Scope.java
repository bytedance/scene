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
package com.bytedance.scene;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.ExceptionsUtility;
import com.bytedance.scene.utlity.SceneInternalException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by JiangQi on 9/11/18.
 */
public class Scope {
    public interface Scoped {
        void onUnRegister();
    }

    public interface RootScopeFactory {
        @NonNull
        Scope getRootScope();
    }

    public static final RootScopeFactory DEFAULT_ROOT_SCOPE_FACTORY = new RootScopeFactory() {
        @Override
        public Scope getRootScope() {
            return new Scope(null, null, generateScopeKey(null));
        }
    };

    private final Scope mParentScope;
    private final String mScopeKey;
    @Nullable
    private final Class<? extends Scene> mSceneClass;
    private final Map<String, Scope> mChildrenScopes = new HashMap<>();
    private final Map<Object, Object> mServices = new HashMap<>();

    private final boolean validateScopeAndViewModelStoreSceneClassStrategy = SceneGlobalConfig.validateScopeAndViewModelStoreSceneClassStrategy;

    @NonNull
    Scope buildScope(@NonNull final Scene scene, @Nullable Bundle bundle) {
        String scopeKey = null;
        if (bundle != null) {
            scopeKey = getScopeKeyFromBundle(bundle);
        }
        if (TextUtils.isEmpty(scopeKey)) {
            scopeKey = generateScopeKey(scene);
        }
        Scope scope = this.mChildrenScopes.get(scopeKey);

        if (scope != null) {
            final Class<? extends Scene> previousSceneClass = scope.mSceneClass;
            if (validateScopeAndViewModelStoreSceneClassStrategy && previousSceneClass != null && previousSceneClass != scene.getClass()) {
                ExceptionsUtility.invokeAndThrowExceptionToNextUILoop(new Runnable() {
                    @Override
                    public void run() {
                        throw new SceneInternalException("Scene buildScope() error, Scope type mismatch, request " + scene.getClass().getName() + " but get " + previousSceneClass.getName());
                    }
                });
            }
        } else {
            scope = new Scope(this, scene.getClass(), scopeKey);
            this.mChildrenScopes.put(scopeKey, scope);
        }
        return scope;
    }

    private void removeChildScope(String scopeKey) {
        this.mChildrenScopes.remove(scopeKey);
    }

    private Scope(Scope parentScope, @Nullable Class<? extends Scene> sceneClass, String scopeKey) {
        this.mParentScope = parentScope;
        this.mSceneClass = sceneClass;
        this.mScopeKey = scopeKey;
    }

    public void register(@NonNull Object key, @NonNull Object service) {
        this.mServices.put(key, service);
    }

    public void registerInMyScope(@NonNull Object key, @NonNull Object service) {
        this.mServices.put(key, service);
    }

    public void unRegister(@NonNull Object key) {
        Object value = this.mServices.get(key);
        if (value != null) {
            if (value instanceof Scoped) {
                ((Scoped) value).onUnRegister();
            }
            this.mServices.remove(key);
        }
    }

    public boolean hasServiceInMyScope(@NonNull Object key) {
        return mServices.containsKey(key);
    }

    public <T> T getServiceInMyScope(@NonNull Object key) {
        return (T) this.mServices.get(key);
    }

    @Nullable
    public <T> T getService(@NonNull Object key) {
        Object value = mServices.get(key);
        if (value != null) {
            return (T) value;
        } else if (mParentScope != null) {
            return mParentScope.getService(key);
        } else {
            return null;
        }
    }

    private static final AtomicInteger SCENE_COUNT = new AtomicInteger(0);

    private static String generateScopeKey(@Nullable Scene scene) {
        switch (SceneGlobalConfig.genScopeStrategy) {
            case 1: {
                if (scene == null) {
                    return "Scene_" + UUID.randomUUID();
                } else {
                    return scene.getClass().getName() + "_" + UUID.randomUUID();
                }
            }
            default: {
                return "Scene #" + SCENE_COUNT.getAndIncrement();
            }
        }
    }

    private static String getScopeKeyFromBundle(@NonNull Bundle bundle) {
        return bundle.getString(ParcelConstants.KEY_SCENE_SCOPE_KEY_TAG);
    }

    public void saveInstance(@NonNull Bundle bundle) {
        bundle.putString(ParcelConstants.KEY_SCENE_SCOPE_KEY_TAG, mScopeKey);
    }

    void destroy() {
        if (mParentScope != null) {
            mParentScope.removeChildScope(this.mScopeKey);
        }

        Collection<Object> values = mServices.values();
        for (Object value : values) {
            if (value instanceof Scoped) {
                ((Scoped) value).onUnRegister();
            }
        }
        mServices.clear();
        mChildrenScopes.clear();
    }
}
