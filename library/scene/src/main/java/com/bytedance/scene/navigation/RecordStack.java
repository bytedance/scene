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
        Record dst = null;
        for (Record record : mBackStackList) {
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
        bundle.putParcelableArrayList(ParcelConstants.KEY_NAVIGATION_RECORD_LIST, new ArrayList<Parcelable>(mBackStackList));
    }

    public void restoreFromBundle(Context context, Bundle bundle, SceneComponentFactory rootSceneComponentFactory) {
        ArrayList<Record> list = bundle.getParcelableArrayList(ParcelConstants.KEY_NAVIGATION_RECORD_LIST);
        this.mBackStackList = new ArrayList<>(list);
        for (int i = 0; i < this.mBackStackList.size(); i++) {
            Record record = this.mBackStackList.get(i);
            //第一个Scene优先用SceneComponentFactory生成
            if (i == 0 && rootSceneComponentFactory != null) {
                record.mScene = rootSceneComponentFactory.instantiateScene(context.getClassLoader(), record.className, null);
            }
            if (record.mScene == null) {
                record.mScene = SceneInstanceUtility.getInstanceFromClassName(context, record.className, null);
            }
        }
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
