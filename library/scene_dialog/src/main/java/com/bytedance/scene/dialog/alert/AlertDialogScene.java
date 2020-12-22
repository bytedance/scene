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
package com.bytedance.scene.dialog.alert;

import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.ArrayRes;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.bytedance.scene.Scene;
import com.bytedance.scene.dialog.DialogScene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.OnBackPressedListener;
import com.bytedance.scene.utlity.Experimental;
import com.bytedance.scene.utlity.Utility;
import com.bytedance.scene.view.SceneContextThemeWrapper;
import java.lang.ref.WeakReference;

@Experimental
public final class AlertDialogScene extends DialogScene implements DialogInterface {

    private AlertController mAlert;
    static final int LAYOUT_HINT_NONE = 0;
    static final int LAYOUT_HINT_SIDE = 1;

    @StyleRes
    private int mTheme;
    private AlertDialogScene.Builder builder;

    private FrameLayout mFrameRootLayout;
    private FrameLayout mFrameContainer;

    private boolean mCanceled = false;

    private Message mCancelMessage;
    private Message mDismissMessage;

    private static final int DISMISS = 0x43;
    private static final int CANCEL = 0x44;

    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = false;
    private float mBgDim;
    private boolean mBgDimHasSet = false;

    private final Handler mHandler = new Handler();
    private final Runnable mDismissAction = new Runnable() {
        @Override public void run() {
            dismissDialog();
        }
    };

    private final Handler mListenersHandler = new ListenersHandler(this);


    private static final class ListenersHandler extends Handler {
        private final WeakReference<DialogInterface> mDialog;


        public ListenersHandler(DialogInterface dialog) {
            mDialog = new WeakReference<>(dialog);
        }


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS:
                    ((OnDismissListener) msg.obj).onDismiss(mDialog.get());
                    break;
                case CANCEL:
                    ((OnCancelListener) msg.obj).onCancel(mDialog.get());
                    break;
            }
        }
    }


    static int resolveDialogTheme(@NonNull Context context, @StyleRes int resid) {
        if ((resid >>> 24 & 255) >= 1) {
            return resid;
        } else {
            TypedValue outValue = new TypedValue();
            context.getTheme()
                .resolveAttribute(android.support.design.R.attr.alertDialogTheme, outValue, true);
            return outValue.resourceId;
        }
    }

    private AlertDialogScene(){

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShowing = true;
    }


    @NonNull
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.setTheme(resolveDialogTheme(requireSceneContext(), mTheme));

        initRootView();

        View child = onCreateContentView(inflater, container, savedInstanceState);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        if (mFrameContainer != null) {
            mFrameContainer.addView(child, layoutParams);
            mFrameRootLayout.addView(mFrameContainer, layoutParams);
        } else {
            mFrameRootLayout.addView(child, layoutParams);
        }

        return mFrameRootLayout;
    }


    @NonNull
    protected View onCreateContentView(@NonNull LayoutInflater inflater,
                                       @NonNull ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        this.mAlert = new AlertController(requireSceneContext(), this, getActivity().getWindow());
        this.builder.P.apply(mAlert);
        int resId = this.mAlert.installContent();
        View view = inflater.inflate(resId, container, false);
        this.mAlert.setupView(view);

        TypedArray b = requireSceneContext().obtainStyledAttributes(
            new int[] { android.R.attr.windowBackground });
        Drawable background = b.getDrawable(0);
        if (background != null) {
            mFrameContainer = new FrameLayout(requireSceneContext());
            mFrameContainer.setBackgroundDrawable(background);
        }
        b.recycle();

        return view;
    }


    private void initRootView() {
        mFrameRootLayout = new FrameLayout(requireSceneContext());

        if (!mBgDimHasSet) {
            TypedArray a = getApplicationContext().obtainStyledAttributes(
                new int[] { android.R.attr.backgroundDimAmount });
            mBgDim = a.getFloat(0, 0.6f);
        }
        mFrameRootLayout.setBackgroundColor(Utility.getColorWithAlpha(mBgDim,
            ContextCompat.getColor(requireSceneContext(), android.R.color.black)));
        mFrameRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCanceledOnTouchOutside) {
                    requireNavigationScene().pop();
                }
            }
        });
    }

    public void setBackGroundDim(float dim) {
        mBgDimHasSet = true;
        if (mBgDim != dim) {
            mBgDim = dim;
            if (mFrameRootLayout != null) {
                mFrameRootLayout.setBackgroundColor(Utility.getColorWithAlpha(mBgDim,
                    ContextCompat.getColor(requireSceneContext(), android.R.color.black)));
            }
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requireNavigationScene().addOnBackPressedListener(this, new OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                return !mCancelable;
            }
        });
    }


    /**
     * Sets whether this dialog is cancelable when press button
     *
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
    }


    /**
     * Sets whether this dialog is canceled when touched outside the window's bounds. If setting to
     * true, the dialog is set to be cancelable if not already set.
     *
     * @param cancel Whether the dialog should be canceled when touched outside the window.
     */
    public void setCanceledOnTouchOutside(boolean cancel) {
        if (cancel && !mCancelable) {
            mCancelable = true;
        }
        mCanceledOnTouchOutside = cancel;
    }


    public Button getButton(int whichButton) {
        return this.mAlert.getButton(whichButton);
    }


    public ListView getListView() {
        return this.mAlert.getListView();
    }


    public void setTitle(CharSequence title) {
        this.mAlert.setTitle(title);
    }


    public void setCustomTitle(View customTitleView) {
        this.mAlert.setCustomTitle(customTitleView);
    }


    public void setMessage(CharSequence message) {
        this.mAlert.setMessage(message);
    }


    public void setView(View view) {
        this.mAlert.setView(view);
    }


    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mAlert.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight,
            viewSpacingBottom);
    }


    @RestrictTo({ RestrictTo.Scope.LIBRARY_GROUP })
    void setButtonPanelLayoutHint(int layoutHint) {
        this.mAlert.setButtonPanelLayoutHint(layoutHint);
    }


    public void setButton(int whichButton, CharSequence text, Message msg) {
        this.mAlert.setButton(whichButton, text, null, msg, null);
    }


    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener) {
        this.mAlert.setButton(whichButton, text, listener, null, null);
    }


    public void setButton(int whichButton, CharSequence text, Drawable icon, DialogInterface.OnClickListener listener) {
        this.mAlert.setButton(whichButton, text, listener, null, icon);
    }


    public void setIcon(int resId) {
        this.mAlert.setIcon(resId);
    }


    public void setIcon(Drawable icon) {
        this.mAlert.setIcon(icon);
    }


    public void setIconAttribute(int attrId) {
        TypedValue out = new TypedValue();
        requireSceneContext().getTheme().resolveAttribute(attrId, out, true);
        this.mAlert.setIcon(out.resourceId);
    }


    @Override
    public void cancel() {
        if (!mCanceled && mCancelMessage != null) {
            mCanceled = true;
            // Obtain a new message so this dialog can be re-used
            Message.obtain(mCancelMessage).sendToTarget();
        }
        dismiss();
    }


    @Override
    public void dismiss() {
        if (Looper.myLooper() == mHandler.getLooper()) {
            dismissDialog();
        } else {
            mHandler.post(mDismissAction);
        }
    }


    private boolean mShowing = false;


    void dismissDialog() {
        if (!mShowing) {
            return;
        }
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        NavigationScene navigationScene = getNavigationScene();
        if (navigationScene != null) {
            navigationScene.remove(this);
            sendDismissMessage();
        }
        mShowing = false;
    }


    private void sendDismissMessage() {
        if (mDismissMessage != null) {
            // Obtain a new message so this dialog can be re-used
            Message.obtain(mDismissMessage).sendToTarget();
        }
    }


    public static AlertDialogScene create(AlertDialogScene.Builder builder) {
        AlertDialogScene scene = new AlertDialogScene();
        scene.builder = builder;
        scene.mTheme = builder.P.theme;
        scene.setCancelable(builder.P.mCancelable);
        if (builder.P.mCancelable) {
            scene.setCanceledOnTouchOutside(true);
        }
        scene.setOnCancelListener(builder.P.mOnCancelListener);
        scene.setOnDismissListener(builder.P.mOnDismissListener);
        return scene;
    }


    public void setOnCancelListener(@Nullable OnCancelListener listener) {
        if (listener != null) {
            mCancelMessage = mListenersHandler.obtainMessage(CANCEL, listener);
        } else {
            mCancelMessage = null;
        }
    }


    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        if (listener != null) {
            mDismissMessage = mListenersHandler.obtainMessage(DISMISS, listener);
        } else {
            mDismissMessage = null;
        }
    }


    public static class Builder {
        private final AlertController.AlertParams P;
        private final Scene hostScene;

        public Builder(@NonNull Scene hostScene) {
            this(hostScene,
                    AlertDialogScene.resolveDialogTheme(hostScene.requireSceneContext(), 0));
        }


        public Builder(@NonNull Scene hostScene, @StyleRes int themeResId) {
            this.hostScene = hostScene;
            Context context = hostScene.requireSceneContext();
            this.P = new AlertController.AlertParams(new SceneContextThemeWrapper(context,
                    AlertDialogScene.resolveDialogTheme(context, themeResId)));
            this.P.theme = themeResId;
        }

        @NonNull
        public Context getContext() {
            return this.P.mContext;
        }


        public AlertDialogScene.Builder setTitle(@StringRes int titleId) {
            this.P.mTitle = this.P.mContext.getText(titleId);
            return this;
        }


        public AlertDialogScene.Builder setTitle(@Nullable CharSequence title) {
            this.P.mTitle = title;
            return this;
        }


        public AlertDialogScene.Builder setCustomTitle(@Nullable View customTitleView) {
            this.P.mCustomTitleView = customTitleView;
            return this;
        }


        public AlertDialogScene.Builder setMessage(@StringRes int messageId) {
            this.P.mMessage = this.P.mContext.getText(messageId);
            return this;
        }


        public AlertDialogScene.Builder setMessage(@Nullable CharSequence message) {
            this.P.mMessage = message;
            return this;
        }


        public AlertDialogScene.Builder setIcon(@DrawableRes int iconId) {
            this.P.mIconId = iconId;
            return this;
        }


        public AlertDialogScene.Builder setIcon(@Nullable Drawable icon) {
            this.P.mIcon = icon;
            return this;
        }


        public AlertDialogScene.Builder setIconAttribute(@AttrRes int attrId) {
            TypedValue out = new TypedValue();
            this.P.mContext.getTheme().resolveAttribute(attrId, out, true);
            this.P.mIconId = out.resourceId;
            return this;
        }


        public AlertDialogScene.Builder setPositiveButton(
            @StringRes int textId, DialogInterface.OnClickListener listener) {
            this.P.mPositiveButtonText = this.P.mContext.getText(textId);
            this.P.mPositiveButtonListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
            this.P.mPositiveButtonText = text;
            this.P.mPositiveButtonListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setPositiveButtonIcon(Drawable icon) {
            this.P.mPositiveButtonIcon = icon;
            return this;
        }


        public AlertDialogScene.Builder setNegativeButton(
            @StringRes int textId, DialogInterface.OnClickListener listener) {
            this.P.mNegativeButtonText = this.P.mContext.getText(textId);
            this.P.mNegativeButtonListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
            this.P.mNegativeButtonText = text;
            this.P.mNegativeButtonListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setNegativeButtonIcon(Drawable icon) {
            this.P.mNegativeButtonIcon = icon;
            return this;
        }


        public AlertDialogScene.Builder setNeutralButton(
            @StringRes int textId, DialogInterface.OnClickListener listener) {
            this.P.mNeutralButtonText = this.P.mContext.getText(textId);
            this.P.mNeutralButtonListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
            this.P.mNeutralButtonText = text;
            this.P.mNeutralButtonListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setNeutralButtonIcon(Drawable icon) {
            this.P.mNeutralButtonIcon = icon;
            return this;
        }


        public AlertDialogScene.Builder setCancelable(boolean cancelable) {
            this.P.mCancelable = cancelable;
            return this;
        }


        public AlertDialogScene.Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
            this.P.mOnCancelListener = onCancelListener;
            return this;
        }


        public AlertDialogScene.Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            this.P.mOnDismissListener = onDismissListener;
            return this;
        }


        public AlertDialogScene.Builder setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
            this.P.mOnKeyListener = onKeyListener;
            return this;
        }


        public AlertDialogScene.Builder setItems(
            @ArrayRes int itemsId, DialogInterface.OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setAdapter(ListAdapter adapter, DialogInterface.OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setCursor(Cursor cursor, DialogInterface.OnClickListener listener, String labelColumn) {
            this.P.mCursor = cursor;
            this.P.mLabelColumn = labelColumn;
            this.P.mOnClickListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setMultiChoiceItems(@ArrayRes int itemsId,
                                                            boolean[] checkedItems,
                                                            DialogInterface.OnMultiChoiceClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }


        public AlertDialogScene.Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener) {
            this.P.mItems = items;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }


        public AlertDialogScene.Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, DialogInterface.OnMultiChoiceClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mIsCheckedColumn = isCheckedColumn;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsMultiChoice = true;
            return this;
        }


        public AlertDialogScene.Builder setSingleChoiceItems(
            @ArrayRes int itemsId, int checkedItem, DialogInterface.OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }


        public AlertDialogScene.Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, DialogInterface.OnClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsSingleChoice = true;
            return this;
        }


        public AlertDialogScene.Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }


        public AlertDialogScene.Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, DialogInterface.OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }


        public AlertDialogScene.Builder setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
            this.P.mOnItemSelectedListener = listener;
            return this;
        }


        public AlertDialogScene.Builder setView(int layoutResId) {
            this.P.mView = null;
            this.P.mViewLayoutResId = layoutResId;
            this.P.mViewSpacingSpecified = false;
            return this;
        }


        public AlertDialogScene.Builder setView(View view) {
            this.P.mView = view;
            this.P.mViewLayoutResId = 0;
            this.P.mViewSpacingSpecified = false;
            return this;
        }


        /**
         * @deprecated
         */
        @Deprecated
        @RestrictTo({ RestrictTo.Scope.LIBRARY_GROUP })
        public AlertDialogScene.Builder setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
            this.P.mView = view;
            this.P.mViewLayoutResId = 0;
            this.P.mViewSpacingSpecified = true;
            this.P.mViewSpacingLeft = viewSpacingLeft;
            this.P.mViewSpacingTop = viewSpacingTop;
            this.P.mViewSpacingRight = viewSpacingRight;
            this.P.mViewSpacingBottom = viewSpacingBottom;
            return this;
        }


        /**
         * @deprecated
         */
        @Deprecated
        public AlertDialogScene.Builder setInverseBackgroundForced(boolean useInverseBackground) {
            this.P.mForceInverseBackground = useInverseBackground;
            return this;
        }


        @RestrictTo({ RestrictTo.Scope.LIBRARY_GROUP })
        public AlertDialogScene.Builder setRecycleOnMeasureEnabled(boolean enabled) {
            this.P.mRecycleOnMeasure = enabled;
            return this;
        }

        @NonNull
        public AlertDialogScene create() {
            AlertDialogScene.Builder builder = this;
            AlertDialogScene scene = new AlertDialogScene();
            scene.builder = builder;
            scene.mTheme = builder.P.theme;
            scene.setCancelable(builder.P.mCancelable);
            if (builder.P.mCancelable) {
                scene.setCanceledOnTouchOutside(true);
            }
            scene.setOnCancelListener(builder.P.mOnCancelListener);
            scene.setOnDismissListener(builder.P.mOnDismissListener);
            return scene;
        }

        @NonNull
        public AlertDialogScene show() {
            AlertDialogScene scene = create();
            scene.show(hostScene);
            return scene;
        }
    }
}