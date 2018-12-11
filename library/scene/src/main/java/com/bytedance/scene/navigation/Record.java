package com.bytedance.scene.navigation;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    @Nullable
    PushResultCallback mPushResultCallback;
    int mPushForResultTargetHashCode = 0;

    String className;

    protected Record(Parcel in) {
        mActivityStatusRecord = in.readParcelable(ActivityStatusRecord.class.getClassLoader());
        mIsTranslucent = in.readByte() != 0;

        className = in.readString();
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
        record.mIsTranslucent = isTranslucent;
        record.mNavigationAnimationExecutor = navigationAnimationExecutor;
        return record;
    }

    public void saveActivityStatus() {
        if (mActivityStatusRecord == null) {
            mActivityStatusRecord = ActivityStatusRecord.newInstance(mScene.requireActivity());
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
        dest.writeString(mScene.getClass().getName());
    }
}
