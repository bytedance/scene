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
package com.bytedance.scene.navigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.PushResultCallback;

/**
 * Created by JiangQi on 7/30/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class Record implements Parcelable {
    @NonNull
    public Scene mScene;
    public boolean mIsTranslucent;
    @Nullable
    public ActivityStatusRecord mActivityStatusRecord;
    @Nullable
    public NavigationAnimationExecutor mNavigationAnimationExecutor;
    @Nullable
    public Object mPushResult;
    @Nullable
    boolean mSceneBackgroundSet = false; //ignore restore
    @Nullable
    public Bundle mPreviousSavedState = null;

    /**
     * In case of A starts B,
     * the attached callback will be placed in B's Record's mPushResultCallback
     */
    @Nullable
    public PushResultCallback mPushResultCallback;

    String mSceneClassName;

    /**
     * this value should not be saved and restore, because it will be reset after onCreateView
     */
    @Nullable
    Configuration mConfiguration;
    boolean mLastSceneWindowFocused = false;

    protected Record(Parcel in) {
        mActivityStatusRecord = in.readParcelable(ActivityStatusRecord.class.getClassLoader());
        mIsTranslucent = in.readByte() != 0;
        mSceneClassName = in.readString();
        mLastSceneWindowFocused = in.readByte() != 0;
    }

    public static final Creator<Record> CREATOR = new Creator<Record>() {
        @Override
        public Record createFromParcel(Parcel in) {
            return new Record(in);
        }

        @Override
        public Record[] newArray(int size) {
            return new Record[size];
        }
    };

    public Record() {

    }

    public static Record newInstance(Scene scene, boolean isTranslucent, NavigationAnimationExecutor navigationAnimationExecutor) {
        Record record = new Record();
        record.mScene = scene;
        record.mSceneClassName = scene.getClass().getName();
        record.mIsTranslucent = isTranslucent;
        record.mNavigationAnimationExecutor = navigationAnimationExecutor;
        return record;
    }

    /**
     * Must be generated each time,
     * because it is possible to use as a transparent page for placeholders
     */
    public void saveActivityStatus() {
        mActivityStatusRecord = ActivityStatusRecord.newInstance(mScene.requireActivity());
    }

    public void saveActivityCompatibleInfo() {
        //Only store Configuration when Scene has ActivityCompatibleManager
        if (ActivityCompatibleInfoCollector.containsConfigChanges(mScene)) {
            mConfiguration = new Configuration(mScene.requireActivity().getResources().getConfiguration());
        }
    }

    public void saveActivityCompatibleInfo(Configuration newConfig) {
        //Only store Configuration when Scene has ActivityCompatibleManager
        if (ActivityCompatibleInfoCollector.containsConfigChanges(mScene)) {
            mConfiguration = new Configuration(newConfig);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mActivityStatusRecord, flags);
        dest.writeByte((byte) (mIsTranslucent ? 1 : 0));
        dest.writeString(mSceneClassName);
        dest.writeByte((byte) (mLastSceneWindowFocused ? 1 : 0));
    }
}
