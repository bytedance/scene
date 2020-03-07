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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.PushResultCallback;
import com.bytedance.scene.navigation.NavigationScene;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Task {
    @NonNull
    private final NavigationScene mNavigationScene;
    @NonNull
    private final TargetSceneFinder mTargetSceneFinder;
    @NonNull
    private final Bundle mArguments;
    @NonNull
    private final String mUrl;
    @NonNull
    private final InterceptorAdapter mInterceptorAdapter;

    @AnimRes
    @AnimatorRes
    private int mPushAnimResId = 0;
    @AnimRes
    @AnimatorRes
    private int mPopAnimResId = 0;

    Task(@NonNull NavigationScene navigationScene, @NonNull TargetSceneFinder targetSceneFinder,
         @NonNull String url, @NonNull InterceptorAdapter interceptorAdapter) {
        this.mNavigationScene = navigationScene;
        this.mTargetSceneFinder = targetSceneFinder;
        this.mUrl = url;//TODO 剔除参数
        this.mInterceptorAdapter = interceptorAdapter;
        this.mArguments = new Bundle();
        argument(urlToBundle(url));
    }

    private Class<? extends Scene> findSceneByUrl(@NonNull String url) {
        return this.mTargetSceneFinder.findSceneClassByUrl(url);
    }

    public void open(@NonNull final OpenCallback openCallback) {
        final Class<? extends Scene> clazz = findSceneByUrl(this.mUrl.trim());
        if (clazz == null) {
            openCallback.onFail(SceneNotFoundException.INSTANCE);
            return;
        }
        this.mInterceptorAdapter.run(new TaskInfo(this.mUrl, this.mArguments), new ContinueTask() {
            @Override
            public void onContinue() {
                if (mPushAnimResId != 0 || mPopAnimResId != 0) {
                    mNavigationScene.push(clazz, mArguments,
                            new PushOptions.Builder().setAnimation(mNavigationScene.requireActivity(), mPushAnimResId, mPopAnimResId).build());
                } else {
                    mNavigationScene.push(clazz, mArguments);
                }
                openCallback.onSuccess();
            }

            @Override
            public void onFail(@Nullable Exception exception) {
                openCallback.onFail(exception);
            }
        });
    }

    public void open() {
        this.open(EMPTY_CALLBACK);
    }

    private static OpenCallback EMPTY_CALLBACK = new OpenCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFail(@Nullable Exception exception) {

        }
    };

    public boolean open(@NonNull PushResultCallback callback) {
        Class<? extends Scene> clazz = findSceneByUrl(this.mUrl.trim());
        if (clazz == null) {
            return false;
        }
        if (this.mPushAnimResId != 0 || this.mPopAnimResId != 0) {
            this.mNavigationScene.push(clazz, mArguments, new PushOptions.Builder()
                    .setAnimation(this.mNavigationScene.requireActivity(), this.mPushAnimResId, this.mPopAnimResId)
                    .setPushResultCallback(callback)
                    .build());
        } else {
            this.mNavigationScene.push(clazz, mArguments, new PushOptions.Builder().setPushResultCallback(callback).build());
        }
        return true;
    }

    public Task argument(@NonNull String key, boolean value) {
        mArguments.putBoolean(key, value);
        return this;
    }

    public Task argument(@NonNull String key, boolean[] values) {
        mArguments.putBooleanArray(key, values);
        return this;
    }

    public Task argument(@NonNull String key, @NonNull Bundle value) {
        mArguments.putBundle(key, value);
        return this;
    }

    public Task argument(@NonNull String key, byte value) {
        mArguments.putByte(key, value);
        return this;
    }

    public Task argument(@NonNull String key, byte[] values) {
        mArguments.putByteArray(key, values);
        return this;
    }

    public Task argument(@NonNull String key, char value) {
        mArguments.putChar(key, value);
        return this;
    }

    public Task argument(@NonNull String key, char[] values) {
        mArguments.putCharArray(key, values);
        return this;
    }


    public Task argument(@NonNull String key, @NonNull CharSequence value) {
        mArguments.putCharSequence(key, value);
        return this;
    }

    public Task argument(@NonNull String key, @NonNull CharSequence[] values) {
        mArguments.putCharSequenceArray(key, values);
        return this;
    }

    public Task argumentCharSequenceList(@NonNull String key, @NonNull ArrayList<CharSequence> values) {
        mArguments.putCharSequenceArrayList(key, values);
        return this;
    }

    public Task argument(@NonNull String key, double value) {
        mArguments.putDouble(key, value);
        return this;
    }

    public Task argument(@NonNull String key, double[] values) {
        mArguments.putDoubleArray(key, values);
        return this;
    }


    public Task argument(@NonNull String key, float value) {
        mArguments.putFloat(key, value);
        return this;
    }

    public Task argument(@NonNull String key, float[] values) {
        mArguments.putFloatArray(key, values);
        return this;
    }

    public Task argument(@NonNull String key, int value) {
        mArguments.putInt(key, value);
        return this;
    }

    public Task argument(@NonNull String key, int[] values) {
        mArguments.putIntArray(key, values);
        return this;
    }

    public Task argumentIntegerList(@NonNull String key, @NonNull ArrayList<Integer> values) {
        mArguments.putIntegerArrayList(key, values);
        return this;
    }

    public Task argument(@NonNull String key, long value) {
        mArguments.putLong(key, value);
        return this;
    }

    public Task argument(@NonNull String key, long[] values) {
        mArguments.putLongArray(key, values);
        return this;
    }

    public Task argument(@NonNull String key, @NonNull Parcelable value) {
        mArguments.putParcelable(key, value);
        return this;
    }

    public Task argument(@NonNull String key, @NonNull Parcelable[] values) {
        mArguments.putParcelableArray(key, values);
        return this;
    }

    public Task argumentParcelableList(@NonNull String key, @NonNull ArrayList<? extends Parcelable> values) {
        mArguments.putParcelableArrayList(key, values);
        return this;
    }


    public Task argument(@NonNull String key, @NonNull Serializable value) {
        mArguments.putSerializable(key, value);
        return this;
    }

    public Task argument(@NonNull String key, short value) {
        mArguments.putShort(key, value);
        return this;
    }

    public Task argument(@NonNull String key, short[] values) {
        mArguments.putShortArray(key, values);
        return this;
    }

    public Task argument(@NonNull String key, @NonNull String value) {
        mArguments.putString(key, value);
        return this;
    }

    public Task argument(@NonNull String key, @NonNull String[] values) {
        mArguments.putStringArray(key, values);
        return this;
    }

    public Task argumentStringList(@NonNull String key, @NonNull ArrayList<String> values) {
        mArguments.putStringArrayList(key, values);
        return this;
    }

    public Task argument(@NonNull Bundle bundle) {
        mArguments.putAll(bundle);
        return this;
    }

    public Task argument(@NonNull Intent intent) {
        mArguments.putAll(intent.getExtras());
        return this;
    }

    public Task animation(@AnimRes @AnimatorRes int enterAnim, @AnimRes @AnimatorRes int exitAnim) {
        this.mPushAnimResId = enterAnim;
        this.mPopAnimResId = exitAnim;
        return this;
    }

    private Bundle urlToBundle(@NonNull String url) {
        Map<String, String> params = getUrlParams(url);
        if (null == params || params.size() == 0) {
            return new Bundle();
        }

        Bundle bundle = new Bundle();
        Map.Entry<String, String> entry = null;
        for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
            entry = stringStringEntry;
            bundle.putString(entry.getKey(), entry.getValue());
        }
        return bundle;
    }

    private static Map<String, String> getUrlParams(@NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        Uri uri = Uri.parse(url);
        if (!uri.isHierarchical()) {
            return null;
        }

        String key = "";
        String value = "";
        Set<String> paramKeys = uri.getQueryParameterNames();
        Iterator<String> iterator = paramKeys.iterator();
        Map<String, String> map = new HashMap<String, String>();
        while (iterator.hasNext()) {
            key = iterator.next();
            if (TextUtils.isEmpty(key)) {
                continue;
            }

            value = uri.getQueryParameter(key);
            if (null == value) {
                value = "";
            }

            map.put(key, value);
        }
        return map;
    }
}
