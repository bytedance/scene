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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.interfaces.PushResultCallback;

/**
 * Created by JiangQi on 7/30/18.
 */
class Record implements Parcelable {
    @NonNull
    Scene mScene;
    boolean mIsTranslucent;
    @Nullable
    ActivityStatusRecord mActivityStatusRecord;
    @Nullable
    NavigationAnimationExecutor mNavigationAnimationExecutor;
    @Nullable
    Object mPushResult;
    /**
     * In case of A starts B,
     * the attached callback will be placed in B's Record's mPushResultCallback
     */
    @Nullable
    PushResultCallback mPushResultCallback;

    String mSceneClassName;

    protected Record(Parcel in) {
        mActivityStatusRecord = in.readParcelable(ActivityStatusRecord.class.getClassLoader());
        mIsTranslucent = in.readByte() != 0;
        mSceneClassName = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mActivityStatusRecord, flags);
        dest.writeByte((byte) (mIsTranslucent ? 1 : 0));
        dest.writeString(mSceneClassName);
    }
}
