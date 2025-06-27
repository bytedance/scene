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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneComponentFactory;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.SceneInstanceUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/1/18.
 */
class RecordStack {
    private List<Record> mBackStackList = new ArrayList<>();

    public void push(Record record) {
        this.mBackStackList.add(record);
    }

    public void pop() {
        this.mBackStackList.remove(this.mBackStackList.size() - 1);
    }

    public void remove(Record record) {
        this.mBackStackList.remove(record);
    }

    public Record getCurrentRecord() {
        return mBackStackList.size() > 0 ? mBackStackList.get(mBackStackList.size() - 1) : null;
    }

    public Record getRecordByScene(Scene scene) {
        for (int i = 0; i < this.mBackStackList.size(); i++) {
            Record record = this.mBackStackList.get(i);
            if (record.mScene == scene) {
                return record;
            }
        }
        return null;
    }

    public Record getPreviousScene() {
        if (mBackStackList.size() < 2) {
            return null;
        }
        return mBackStackList.get(mBackStackList.size() - 2);
    }

    public boolean canPop() {
        return this.mBackStackList.size() > 1;
    }

    public List<Record> getCurrentRecordList() {
        return new ArrayList<>(mBackStackList);
    }

    public String getStackHistory() {
        StringBuilder stringBuilder = new StringBuilder("NavigationScene history: ");

        for (Record record : mBackStackList) {
            stringBuilder.append(" ------> " + record.mScene.getClass().getSimpleName());
        }

        return stringBuilder.toString();
    }

    public void saveToBundle(Bundle bundle) {
        ArrayList<Record> tmp = new ArrayList<>(mBackStackList);
        ArrayList<Parcelable> resultList = new ArrayList<>(tmp.size());
        //skip restore disabled scene
        for (int i = 0; i <= tmp.size() - 1; i++) {
            Record record = tmp.get(i);
            if (record.mScene.isSceneRestoreEnabled()) {
                resultList.add(record);
            }
        }
        bundle.putParcelableArrayList(ParcelConstants.KEY_NAVIGATION_RECORD_LIST, resultList);
    }

    public void restoreFromBundle(Context context, Bundle bundle, SceneComponentFactory rootSceneComponentFactory) {
        ArrayList<Record> list = bundle.getParcelableArrayList(ParcelConstants.KEY_NAVIGATION_RECORD_LIST);
        this.mBackStackList = new ArrayList<>(list);
        for (int i = 0; i < this.mBackStackList.size(); i++) {
            Record record = this.mBackStackList.get(i);
            Scene scene = createNewSceneInstance(context,i,record,rootSceneComponentFactory);
            record.mScene = scene;
        }
    }

    public static Scene createNewSceneInstance(Context context, int i, Record record, SceneComponentFactory rootSceneComponentFactory) {
        Scene scene = null;
        // The first Scene will generated with the SceneComponentFactory first
        if (i == 0 && rootSceneComponentFactory != null) {
            Scene rootScene = rootSceneComponentFactory.instantiateScene(context.getClassLoader(), record.mSceneClassName, null);
            if (rootScene != null && rootScene.getParentScene() != null) {
                throw new IllegalArgumentException("SceneComponentFactory instantiateScene return Scene already has a parent");
            }
            scene = rootScene;
        }
        if (scene == null) {
            scene = SceneInstanceUtility.getInstanceFromClassName(context, record.mSceneClassName, null);
        }
        return scene;
    }

    public int getOrderByView(View view) {
        for (int i = 0; i < mBackStackList.size(); i++) {
            if (view == mBackStackList.get(i).mScene.getView()) {
                return i;
            }
        }
        return -1;
    }
}
