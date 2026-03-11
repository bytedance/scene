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
package com.bytedance.scene.group;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.utlity.Utility;

/**
 * Created by JiangQi on 7/30/18.
 * <p>
 */
class GroupRecord implements Parcelable {
    @IdRes
    int viewId = View.NO_ID;
    Scene scene;
    String tag;
    boolean isHidden = false;
    boolean isCurrentFocus = false;
    String sceneClassName;
    @Nullable
    Bundle bundle;

    protected GroupRecord(@NonNull Parcel in) {
        viewId = in.readInt();
        tag = Utility.requireNonNull(in.readString(), "tag not found in Parcel");
        isHidden = in.readByte() != 0;
        isCurrentFocus = in.readByte() != 0;
        sceneClassName = Utility.requireNonNull(in.readString(), "class name not found in Parcel");
    }

    public static final Creator<GroupRecord> CREATOR = new Creator<GroupRecord>() {
        @Override
        public GroupRecord createFromParcel(Parcel in) {
            return new GroupRecord(in);
        }

        @Override
        public GroupRecord[] newArray(int size) {
            return new GroupRecord[size];
        }
    };

    public GroupRecord() {

    }

    static GroupRecord newInstance(@IdRes int viewId, @NonNull Scene scene, @NonNull String tag) {
        GroupRecord record = new GroupRecord();
        record.viewId = viewId;
        record.scene = Utility.requireNonNull(scene, "scene can't be null");
        record.tag = Utility.requireNonNull(tag, "tag can't be null");
        record.sceneClassName = Utility.requireNonNull(scene.getClass().getName(), "Scene class name is null");
        return record;
    }

    public void setHidden(boolean hidden) {
        this.isHidden = hidden;
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(viewId);
        dest.writeString(tag);
        dest.writeByte((byte) (isHidden ? 1 : 0));
        dest.writeByte((byte) (isCurrentFocus ? 1 : 0));
        dest.writeString(sceneClassName);
    }
}