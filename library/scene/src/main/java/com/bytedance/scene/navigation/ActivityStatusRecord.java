package com.bytedance.scene.navigation;

import android.app.Activity;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;

/**
 * Created by JiangQi on 7/30/18.
 */

class ActivityStatusRecord implements Parcelable {
    private int mStatusBarColor;
    private int mNavigationBarColor;
    private int mSystemUiVisibility;
    private int mSoftInputMode;
    private int mWindowFlags;

    private ActivityStatusRecord() {

    }

    protected ActivityStatusRecord(Parcel in) {
        mStatusBarColor = in.readInt();
        mNavigationBarColor = in.readInt();
        mSystemUiVisibility = in.readInt();
        mSoftInputMode = in.readInt();
        mWindowFlags = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mStatusBarColor);
        dest.writeInt(mNavigationBarColor);
        dest.writeInt(mSystemUiVisibility);
        dest.writeInt(mSoftInputMode);
        dest.writeInt(mWindowFlags);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ActivityStatusRecord> CREATOR = new Parcelable.Creator<ActivityStatusRecord>() {
        @Override
        public ActivityStatusRecord createFromParcel(Parcel in) {
            return new ActivityStatusRecord(in);
        }

        @Override
        public ActivityStatusRecord[] newArray(int size) {
            return new ActivityStatusRecord[size];
        }
    };

    public static ActivityStatusRecord newInstance(Activity activity) {
        ActivityStatusRecord record = new ActivityStatusRecord();
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            record.mStatusBarColor = window.getStatusBarColor();
            record.mNavigationBarColor = window.getNavigationBarColor();
        }
        record.mSystemUiVisibility = decorView.getSystemUiVisibility();
        record.mSoftInputMode = window.getAttributes().softInputMode;
        record.mWindowFlags = window.getAttributes().flags;
        return record;
    }

    public void restore(Activity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(mStatusBarColor);
            window.setNavigationBarColor(mNavigationBarColor);
        }
        window.getDecorView().setSystemUiVisibility(mSystemUiVisibility);
        window.setSoftInputMode(mSoftInputMode);
        int currentFlags = window.getAttributes().flags;
        int previousFlags = mWindowFlags;

        int commonFlags = currentFlags & previousFlags;
        int clearFlags = previousFlags & ~commonFlags;
        window.addFlags(clearFlags);

        int addFlags = currentFlags & ~commonFlags;
        window.clearFlags(addFlags);
    }
}
