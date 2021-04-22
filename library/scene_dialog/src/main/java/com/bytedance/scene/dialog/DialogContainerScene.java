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
package com.bytedance.scene.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.lifecycle.Lifecycle;
import com.bytedance.scene.navigation.NavigationSceneGetter;
import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.Experimental;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * replacement for DialogFragment
 */
@Experimental
public class DialogContainerScene extends Scene implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({STYLE_NORMAL, STYLE_NO_TITLE, STYLE_NO_FRAME, STYLE_NO_INPUT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DialogStyle {
    }

    /**
     * Style for {@link #setStyle(int, int)}: a basic,
     * normal dialog.
     */
    public static final int STYLE_NORMAL = 0;

    /**
     * Style for {@link #setStyle(int, int)}: don't include
     * a title area.
     */
    public static final int STYLE_NO_TITLE = 1;

    /**
     * Style for {@link #setStyle(int, int)}: don't draw
     * any frame at all; the view hierarchy returned by {@link #onCreateView}
     * is entirely responsible for drawing the dialog.
     */
    public static final int STYLE_NO_FRAME = 2;

    /**
     * Style for {@link #setStyle(int, int)}: like
     * {@link #STYLE_NO_FRAME}, but also disables all input to the dialog.
     * The user can not touch it, and its window will not receive input focus.
     */
    public static final int STYLE_NO_INPUT = 3;

    private static final String SAVED_DIALOG_STATE_TAG = "android:savedDialogState";
    private static final String SAVED_STYLE = "android:style";
    private static final String SAVED_THEME = "android:theme";
    private static final String SAVED_CANCELABLE = "android:cancelable";

    int mStyle = STYLE_NORMAL;
    int mTheme = 0;
    boolean mCancelable = true;

    Dialog mDialog;
    boolean mViewDestroyed;
    boolean mDismissed;
    boolean mShownByMe;

    public DialogContainerScene() {
    }

    /**
     * Call to customize the basic appearance and behavior of the
     * Scene's dialog.  This can be used for some common dialog behaviors,
     * taking care of selecting flags, theme, and other options for you.  The
     * same effect can be achieve by manually setting Dialog and Window
     * attributes yourself.  Calling this after the Scene's Dialog is
     * created will have no effect.
     *
     * @param style Selects a standard style: may be {@link #STYLE_NORMAL},
     *              {@link #STYLE_NO_TITLE}, {@link #STYLE_NO_FRAME}, or
     *              {@link #STYLE_NO_INPUT}.
     * @param theme Optional custom theme.  If 0, an appropriate theme (based
     *              on the style) will be selected for you.
     */
    public void setStyle(@DialogContainerScene.DialogStyle int style, @StyleRes int theme) {
        mStyle = style;
        if (mStyle == STYLE_NO_FRAME || mStyle == STYLE_NO_INPUT) {
            mTheme = android.R.style.Theme_Panel;
        }
        if (theme != 0) {
            mTheme = theme;
        }
    }

    public void show(@NonNull Scene hostScene) {
        if (hostScene.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        Activity activity = hostScene.getActivity();
        if (activity == null) {
            return;
        }

        if (activity.isFinishing()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            return;
        }

        NavigationScene navigationScene = NavigationSceneGetter.getNavigationScene(hostScene);
        if (navigationScene == null) {
            return;
        }
        mDismissed = false;
        mShownByMe = true;
        navigationScene.push(this, new PushOptions.Builder().setTranslucent(true)
                .setAnimation(new NoAnimationExecutor()).build());
    }

    /**
     * Dismiss the Scene and its dialog.
     */
    public void dismiss() {
        dismissInternal(false);
    }

    private void dismissInternal(boolean allowStateLoss) {
        if (mDismissed) {
            return;
        }
        mDismissed = true;
        mShownByMe = false;
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mViewDestroyed = true;
        NavigationScene navigationScene = NavigationSceneGetter.getNavigationScene(this);
        if (navigationScene != null) {
            navigationScene.remove(this);
        }
    }

    public Dialog getDialog() {
        return mDialog;
    }

    /**
     * Control whether the shown Dialog is cancelable.  Use this instead of
     * directly calling {@link Dialog#setCancelable(boolean)
     * Dialog.setCancelable(boolean)}, because DialogScene needs to change
     * its behavior based on this.
     *
     * @param cancelable If true, the dialog is cancelable.  The default
     *                   is true.
     */
    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        if (mDialog != null) mDialog.setCancelable(cancelable);
    }

    /**
     * Return the current value of {@link #setCancelable(boolean)}.
     */
    public boolean isCancelable() {
        return mCancelable;
    }

    @Override
    public void onAttach() {
        super.onAttach();
        if (!mShownByMe) {
            // If not explicitly shown through our API, take this as an
            // indication that the dialog is no longer dismissed.
            mDismissed = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (!mShownByMe && !mDismissed) {
            // The Scene was not shown by a direct call here, it is not
            // dismissed, and now it is being detached...  well, okay, thou
            // art now dismissed.  Have fun.
            mDismissed = true;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mStyle = savedInstanceState.getInt(SAVED_STYLE, STYLE_NORMAL);
            mTheme = savedInstanceState.getInt(SAVED_THEME, 0);
            mCancelable = savedInstanceState.getBoolean(SAVED_CANCELABLE, true);
        }
    }

    @NonNull
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(requireSceneContext());
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public void setupDialog(Dialog dialog, int style) {
        switch (style) {
            case STYLE_NO_INPUT:
                dialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // fall through...
            case STYLE_NO_FRAME:
            case STYLE_NO_TITLE:
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    /**
     * Override to build your own custom Dialog container.  This is typically
     * used to show an AlertDialog instead of a generic Dialog; when doing so,
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} does not need
     * to be implemented since the AlertDialog takes care of its own content.
     *
     * <p>This method will be called after {@link #onCreate(Bundle)} and
     * before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.  The
     * default implementation simply instantiates and returns a {@link Dialog}
     * class.
     *
     * <p><em>Note: DialogScene own the {@link Dialog#setOnCancelListener
     * Dialog.setOnCancelListener} and {@link Dialog#setOnDismissListener
     * Dialog.setOnDismissListener} callbacks.  You must not set them yourself.</em>
     * To find out about these events, override {@link #onCancel(DialogInterface)}
     * and {@link #onDismiss(DialogInterface)}.</p>
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(requireActivity(), mTheme);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!mViewDestroyed) {
            // Note: we need to use allowStateLoss, because the dialog
            // dispatches this asynchronously so we can receive the call
            // after the activity is paused.  Worst case, when the user comes
            // back to the activity they see the dialog again.
            dismissInternal(true);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDialog = onCreateDialog(savedInstanceState);
        final Activity activity = requireActivity();
        mDialog.setOwnerActivity(activity);
        mDialog.setCancelable(mCancelable);
        mDialog.setOnCancelListener(this);
        mDialog.setOnDismissListener(this);
        if (savedInstanceState != null) {
            Bundle dialogState = savedInstanceState.getBundle(SAVED_DIALOG_STATE_TAG);
            if (dialogState != null) {
                mDialog.onRestoreInstanceState(dialogState);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mDialog != null) {
            mViewDestroyed = false;
            mDialog.show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDialog != null) {
            Bundle dialogState = mDialog.onSaveInstanceState();
            if (dialogState != null) {
                outState.putBundle(SAVED_DIALOG_STATE_TAG, dialogState);
            }
        }
        if (mStyle != STYLE_NORMAL) {
            outState.putInt(SAVED_STYLE, mStyle);
        }
        if (mTheme != 0) {
            outState.putInt(SAVED_THEME, mTheme);
        }
        if (!mCancelable) {
            outState.putBoolean(SAVED_CANCELABLE, mCancelable);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.hide();
        }
    }

    /**
     * Remove dialog.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDialog != null) {
            // Set removed here because this dismissal is just to hide
            // the dialog -- we don't want this to cause the Scene to
            // actually be removed.
            mViewDestroyed = true;
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
