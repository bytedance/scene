package com.bytedance.scene.ui;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v4.app.FragmentTransaction;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
class FragmentUtility {
    static void commitFragment(@NonNull FragmentTransaction transaction, boolean commitNow) {
        if (commitNow) {
            transaction.commitNowAllowingStateLoss();
        } else {
            transaction.commitAllowingStateLoss();
        }
    }
}
