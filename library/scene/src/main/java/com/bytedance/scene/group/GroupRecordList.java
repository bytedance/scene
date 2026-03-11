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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.SceneInstanceUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JiangQi on 7/30/18.
 * <p>
 */
class GroupRecordList {
    private static final String KEY_TAG = ParcelConstants.KEY_GROUP_RECORD_LIST;

    private List<GroupRecord> mSceneList = new ArrayList<>();
    private final Map<Scene, GroupRecord> mSceneMap = new HashMap<>();
    private final Map<String, GroupRecord> mTagMap = new HashMap<>();

    public void add(GroupRecord record) {
        this.mSceneList.add(record);
        this.mSceneMap.put(record.scene, record);
        this.mTagMap.put(record.tag, record);
    }

    public void remove(GroupRecord record) {
        this.mSceneList.remove(record);
        this.mSceneMap.remove(record.scene);
        this.mTagMap.remove(record.tag);
    }

    public GroupRecord findByScene(Scene scene) {
        return this.mSceneMap.get(scene);
    }

    public GroupRecord findByTag(String tag) {
        return this.mTagMap.get(tag);
    }

    public GroupRecord findByView(View view) {
        GroupRecord groupRecord = null;
        for (GroupRecord record : mSceneList) {
            if (view.equals(record.scene.getView())) {
                groupRecord = record;
                break;
            }
        }
        return groupRecord;
    }

    public List<Scene> getChildSceneList() {
        List<Scene> sceneList = new ArrayList<>();
        for (GroupRecord record : mSceneList) {
            sceneList.add(record.scene);
        }
        return Collections.unmodifiableList(sceneList);
    }

    public List<GroupRecord> getChildSceneRecordList() {
        return Collections.unmodifiableList(mSceneList);
    }

    public void saveToBundle(@NonNull Bundle bundle) {
        ArrayList<GroupRecord> tmp = new ArrayList<>(mSceneList);
        ArrayList<Parcelable> resultList = new ArrayList<>(tmp.size());
        //skip restore disabled scene
        for (int i = 0; i <= tmp.size() - 1; i++) {
            GroupRecord record = tmp.get(i);
            if (record.scene.isSceneRestoreEnabled()) {
                resultList.add(record);
            }
        }
        bundle.putParcelableArrayList(KEY_TAG, new ArrayList<Parcelable>(resultList));
    }

    public void restoreFromBundle(@NonNull Context context, @NonNull Bundle bundle) {
        if (this.mSceneList != null && this.mSceneList.size() > 0) {
            throw new IllegalStateException("mSceneList size is not zero, Scene is added before restore");
        }
        this.mSceneList = new ArrayList<>(bundle.<GroupRecord>getParcelableArrayList(KEY_TAG));
        for (GroupRecord record : this.mSceneList) {
            record.scene = SceneInstanceUtility.getInstanceFromClassName(context, record.sceneClassName, null);
            this.mSceneMap.put(record.scene, record);
            this.mTagMap.put(record.tag, record);
        }
    }

    public void clear() {
        this.mSceneList.clear();
        this.mSceneMap.clear();
        this.mTagMap.clear();
    }
}
