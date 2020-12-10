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

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.bytedance.scene.dialog.R;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

class AlertController {
    private final Context mContext;
    private final AlertDialogScene mDialog;
    private final Window mWindow;
    private final int mButtonIconDimen;
    private CharSequence mTitle;
    private CharSequence mMessage;
    private ListView mListView;
    private View mView;
    private int mViewLayoutResId;
    private int mViewSpacingLeft;
    private int mViewSpacingTop;
    private int mViewSpacingRight;
    private int mViewSpacingBottom;
    private boolean mViewSpacingSpecified = false;
    private Button mButtonPositive;
    private CharSequence mButtonPositiveText;
    private Message mButtonPositiveMessage;
    private Drawable mButtonPositiveIcon;
    private Button mButtonNegative;
    private CharSequence mButtonNegativeText;
    private Message mButtonNegativeMessage;
    private Drawable mButtonNegativeIcon;
    private Button mButtonNeutral;
    private CharSequence mButtonNeutralText;
    private Message mButtonNeutralMessage;
    private Drawable mButtonNeutralIcon;
    private NestedScrollView mScrollView;
    private int mIconId = 0;
    private Drawable mIcon;
    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mMessageView;
    private View mCustomTitleView;
    private ListAdapter mAdapter;
    private int mCheckedItem = -1;
    private int mAlertDialogLayout;
    private int mButtonPanelSideLayout;
    private int mListLayout;
    private int mMultiChoiceItemLayout;
    private int mSingleChoiceItemLayout;
    private int mListItemLayout;
    private boolean mShowTitle;
    private int mButtonPanelLayoutHint = AlertDialogScene.LAYOUT_HINT_NONE;
    private Handler mHandler;
    private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Message m;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
                m = Message.obtain(mButtonNeutralMessage);
            } else {
                m = null;
            }
            if (m != null) {
                m.sendToTarget();
            }
            // Post a message so we dismiss after the above handlers are executed
            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialog)
                .sendToTarget();
        }
    };


    private static final class ButtonHandler extends Handler {
        // Button clicks have Message.what as the BUTTON{1,2,3} constant
        private static final int MSG_DISMISS_DIALOG = 1;
        private WeakReference<DialogInterface> mDialog;


        private ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<>(dialog);
        }


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;
                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }


    private static boolean shouldCenterSingleButton(Context context) {
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.alertDialogCenterButtons, outValue, true);
        return outValue.data != 0;
    }


    public AlertController(Context context, AlertDialogScene di, Window window) {
        mContext = context;
        mDialog = di;
        mWindow = window;
        mHandler = new ButtonHandler(di);
        final TypedArray a = context.obtainStyledAttributes(null, R.styleable.AlertDialog,
            R.attr.alertDialogStyle, 0);
        mAlertDialogLayout = a.getResourceId(R.styleable.AlertDialog_android_layout, 0);
        mButtonPanelSideLayout = a.getResourceId(R.styleable.AlertDialog_buttonPanelSideLayout, 0);
        mListLayout = a.getResourceId(R.styleable.AlertDialog_listLayout, 0);
        mMultiChoiceItemLayout = a.getResourceId(R.styleable.AlertDialog_multiChoiceItemLayout, 0);
        mSingleChoiceItemLayout = a
            .getResourceId(R.styleable.AlertDialog_singleChoiceItemLayout, 0);
        mListItemLayout = a.getResourceId(R.styleable.AlertDialog_listItemLayout, 0);
        mShowTitle = a.getBoolean(R.styleable.AlertDialog_showTitle, true);
        mButtonIconDimen = a.getDimensionPixelSize(R.styleable.AlertDialog_buttonIconDimen, 0);
        a.recycle();
    }


    private static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }
        return false;
    }


    public int installContent() {
        return selectContentView();
    }


    private int selectContentView() {
        if (mButtonPanelSideLayout == 0) {
            return mAlertDialogLayout;
        }
        if (mButtonPanelLayoutHint == AlertDialogScene.LAYOUT_HINT_SIDE) {
            return mButtonPanelSideLayout;
        }
        return mAlertDialogLayout;
    }


    void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }


    /**
     * @see AlertDialogScene.Builder#setCustomTitle(View)
     */
    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }


    void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }


    /**
     * Set the view resource to display in the dialog.
     */
    public void setView(int layoutResId) {
        mView = null;
        mViewLayoutResId = layoutResId;
        mViewSpacingSpecified = false;
    }


    /**
     * Set the view to display in the dialog.
     */
    public void setView(View view) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = false;
    }


    /**
     * Set the view to display in the dialog along with the spacing around that view
     */
    void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight,
                 int viewSpacingBottom) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
    }


    /**
     * Sets a hint for the best button panel layout.
     */
    void setButtonPanelLayoutHint(int layoutHint) {
        mButtonPanelLayoutHint = layoutHint;
    }


    /**
     * Sets an icon, a click listener or a message to be sent when the button is clicked. You only
     * need to pass one of {@code icon}, {@code listener} or {@code msg}.
     *
     * @param whichButton Which button, can be one of {@link DialogInterface#BUTTON_POSITIVE},
     *                    {@link DialogInterface#BUTTON_NEGATIVE}, or {@link
     *                    DialogInterface#BUTTON_NEUTRAL}
     * @param text        The text to display in positive button.
     * @param listener    The {@link DialogInterface.OnClickListener} to use.
     * @param msg         The {@link Message} to be sent when clicked.
     * @param icon        The (@link Drawable) to be used as an icon for the button.
     */
    void setButton(int whichButton, CharSequence text,
                   DialogInterface.OnClickListener listener, Message msg, Drawable icon) {
        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                mButtonPositiveIcon = icon;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                mButtonNegativeIcon = icon;
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                mButtonNeutralText = text;
                mButtonNeutralMessage = msg;
                mButtonNeutralIcon = icon;
                break;
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }


    /**
     * Specifies the icon to display next to the alert title.
     *
     * @param resId the resource identifier of the drawable to use as the icon, or 0 for no icon
     */
    void setIcon(int resId) {
        mIcon = null;
        mIconId = resId;
        if (mIconView != null) {
            if (resId != 0) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageResource(mIconId);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    }


    /**
     * Specifies the icon to display next to the alert title.
     *
     * @param icon the drawable to use as the icon or null for no icon
     */
    void setIcon(Drawable icon) {
        mIcon = icon;
        mIconId = 0;
        if (mIconView != null) {
            if (icon != null) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageDrawable(icon);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    }


    /**
     * @param attrId the attributeId of the theme-specific drawable to resolve the resourceId for.
     * @return resId the resourceId of the theme-specific drawable
     */
    private int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }


    ListView getListView() {
        return mListView;
    }


    Button getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }


    @SuppressWarnings({ "UnusedDeclaration" }) boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }


    @SuppressWarnings({ "UnusedDeclaration" }) boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }


    /**
     * Resolves whether a custom or default panel should be used. Removes the default panel if a
     * custom panel should be used. If the resolved panel is a view stub, inflates before
     * returning.
     *
     * @param customPanel  the custom panel
     * @param defaultPanel the default panel
     * @return the panel to use
     */
    @Nullable
    private ViewGroup resolvePanel(@Nullable View customPanel, @Nullable View defaultPanel) {
        if (customPanel == null) {
            // Inflate the default panel, if needed.
            if (defaultPanel instanceof ViewStub) {
                defaultPanel = ((ViewStub) defaultPanel).inflate();
            }
            return (ViewGroup) defaultPanel;
        }
        // Remove the default panel entirely.
        if (defaultPanel != null) {
            final ViewParent parent = defaultPanel.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(defaultPanel);
            }
        }
        // Inflate the custom panel, if needed.
        if (customPanel instanceof ViewStub) {
            customPanel = ((ViewStub) customPanel).inflate();
        }
        return (ViewGroup) customPanel;
    }



     void setupView(View contentView) {
        final View parentPanel = contentView.findViewById(R.id.parentPanel);
        final View defaultTopPanel = parentPanel.findViewById(R.id.topPanel);
        final View defaultContentPanel = parentPanel.findViewById(R.id.contentPanel);
        final View defaultButtonPanel = parentPanel.findViewById(R.id.buttonPanel);
        // Install custom content before setting up the title or buttons so
        // that we can handle panel overrides.
        final ViewGroup customPanel = parentPanel.findViewById(R.id.customPanel);
        setupCustomContent(customPanel);
        final View customTopPanel = customPanel.findViewById(R.id.topPanel);
        final View customContentPanel = customPanel.findViewById(R.id.contentPanel);
        final View customButtonPanel = customPanel.findViewById(R.id.buttonPanel);
        // Resolve the correct panels and remove the defaults, if needed.
        final ViewGroup topPanel = resolvePanel(customTopPanel, defaultTopPanel);
        final ViewGroup contentPanel = resolvePanel(customContentPanel, defaultContentPanel);
        final ViewGroup buttonPanel = resolvePanel(customButtonPanel, defaultButtonPanel);
        if(contentPanel != null) {
            setupContent(contentPanel);
        }
        if(buttonPanel != null) {
            setupButtons(buttonPanel);
        }
        setupTitle(topPanel);
        final boolean hasCustomPanel = customPanel.getVisibility() != View.GONE;
        final boolean hasTopPanel = topPanel != null
            && topPanel.getVisibility() != View.GONE;
        final boolean hasButtonPanel = buttonPanel.getVisibility() != View.GONE;
        // Only display the text spacer if we don't have buttons.
        if (!hasButtonPanel) {
            if (contentPanel != null) {
                final View spacer = contentPanel.findViewById(R.id.textSpacerNoButtons);
                if (spacer != null) {
                    spacer.setVisibility(View.VISIBLE);
                }
            }
        }
        if (hasTopPanel) {
            // Only clip scrolling content to padding if we have a title.
            if (mScrollView != null) {
                mScrollView.setClipToPadding(true);
            }
            // Only show the divider if we have a title.
            View divider = null;
            if (mMessage != null || mListView != null) {
                divider = topPanel.findViewById(R.id.titleDividerNoCustom);
            }
            if (divider != null) {
                divider.setVisibility(View.VISIBLE);
            }
        } else {
            if (contentPanel != null) {
                final View spacer = contentPanel.findViewById(R.id.textSpacerNoTitle);
                if (spacer != null) {
                    spacer.setVisibility(View.VISIBLE);
                }
            }
        }

        try {
            if(mListView != null) {
                Method method = mListView.getClass().getDeclaredMethod("setHasDecor", boolean.class, boolean.class);
                method.invoke(mListView, hasTopPanel, hasButtonPanel);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        // Update scroll indicators as needed.
        if (!hasCustomPanel) {
            final View content = mListView != null ? mListView : mScrollView;
            if (content != null) {
                final int indicators = (hasTopPanel ? ViewCompat.SCROLL_INDICATOR_TOP : 0)
                    | (hasButtonPanel ? ViewCompat.SCROLL_INDICATOR_BOTTOM : 0);
                setScrollIndicators(contentPanel, content, indicators,
                    ViewCompat.SCROLL_INDICATOR_TOP | ViewCompat.SCROLL_INDICATOR_BOTTOM);
            }
        }
        final ListView listView = mListView;
        if (listView != null && mAdapter != null) {
            listView.setAdapter(mAdapter);
            final int checkedItem = mCheckedItem;
            if (checkedItem > -1) {
                listView.setItemChecked(checkedItem, true);
                listView.setSelection(checkedItem);
            }
        }
    }


    private void setScrollIndicators(ViewGroup contentPanel, View content,
                                     final int indicators, final int mask) {
        // Set up scroll indicators (if present).
        View indicatorUp = contentPanel.findViewById(R.id.scrollIndicatorUp);
        View indicatorDown = contentPanel.findViewById(R.id.scrollIndicatorDown);
        if (Build.VERSION.SDK_INT >= 23) {
            // We're on Marshmallow so can rely on the View APIs
            ViewCompat.setScrollIndicators(content, indicators, mask);
            // We can also remove the compat indicator views
            if (indicatorUp != null) {
                contentPanel.removeView(indicatorUp);
            }
            if (indicatorDown != null) {
                contentPanel.removeView(indicatorDown);
            }
        } else {
            // First, remove the indicator views if we're not set to use them
            if (indicatorUp != null && (indicators & ViewCompat.SCROLL_INDICATOR_TOP) == 0) {
                contentPanel.removeView(indicatorUp);
                indicatorUp = null;
            }
            if (indicatorDown != null && (indicators & ViewCompat.SCROLL_INDICATOR_BOTTOM) == 0) {
                contentPanel.removeView(indicatorDown);
                indicatorDown = null;
            }
            if (indicatorUp != null || indicatorDown != null) {
                final View top = indicatorUp;
                final View bottom = indicatorDown;
                if (mMessage != null) {
                    // We're just showing the ScrollView, set up listener.
                    mScrollView.setOnScrollChangeListener(
                        new NestedScrollView.OnScrollChangeListener() {
                            @Override
                            public void onScrollChange(NestedScrollView v, int scrollX,
                                                       int scrollY,
                                                       int oldScrollX, int oldScrollY) {
                                manageScrollIndicators(v, top, bottom);
                            }
                        });
                    // Set up the indicators following layout.
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            manageScrollIndicators(mScrollView, top, bottom);
                        }
                    });
                } else if (mListView != null) {
                    // We're just showing the AbsListView, set up listener.
                    mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {}


                        @Override
                        public void onScroll(AbsListView v, int firstVisibleItem,
                                             int visibleItemCount, int totalItemCount) {
                            manageScrollIndicators(v, top, bottom);
                        }
                    });
                    // Set up the indicators following layout.
                    mListView.post(new Runnable() {
                        @Override
                        public void run() {
                            manageScrollIndicators(mListView, top, bottom);
                        }
                    });
                } else {
                    // We don't have any content to scroll, remove the indicators.
                    if (top != null) {
                        contentPanel.removeView(top);
                    }
                    if (bottom != null) {
                        contentPanel.removeView(bottom);
                    }
                }
            }
        }
    }


    private void setupCustomContent(ViewGroup customPanel) {
        final View customView;
        if (mView != null) {
            customView = mView;
        } else if (mViewLayoutResId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            customView = inflater.inflate(mViewLayoutResId, customPanel, false);
        } else {
            customView = null;
        }
        final boolean hasCustomView = customView != null;
        if (!hasCustomView || !canTextInput(customView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
        if (hasCustomView) {
            FrameLayout custom = customPanel.findViewById(R.id.custom);
            if(custom == null){
                custom = customPanel.findViewById(android.R.id.custom);
            }
            custom.addView(customView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            if (mViewSpacingSpecified) {
                custom.setPadding(
                    mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight, mViewSpacingBottom);
            }
            if (mListView != null) {
                ((LinearLayoutCompat.LayoutParams) customPanel.getLayoutParams()).weight = 0;
            }
        } else {
            customPanel.setVisibility(View.GONE);
        }
    }




    private void setupTitle(ViewGroup topPanel) {
        if (mCustomTitleView != null) {
            // Add the custom title view directly to the topPanel layout
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            topPanel.addView(mCustomTitleView, 0, lp);
            // Hide the title template
            View titleTemplate = topPanel.findViewById(R.id.title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
            mIconView = topPanel.findViewById(android.R.id.icon);
            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
            if (hasTextTitle && mShowTitle) {
                // Display the title if a title is supplied, else hide it.
                mTitleView = topPanel.findViewById(R.id.alertTitle);
                mTitleView.setText(mTitle);
                // Do this last so that if the user has supplied any icons we
                // use them instead of the default ones. If the user has
                // specified 0 then make it disappear.
                if (mIconId != 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else {
                    // Apply the padding from the icon to ensure the title is
                    // aligned correctly.
                    mTitleView.setPadding(mIconView.getPaddingLeft(),
                        mIconView.getPaddingTop(),
                        mIconView.getPaddingRight(),
                        mIconView.getPaddingBottom());
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                // Hide the title template
                final View titleTemplate = topPanel.findViewById(R.id.title_template);
                titleTemplate.setVisibility(View.GONE);
                mIconView.setVisibility(View.GONE);
                topPanel.setVisibility(View.GONE);
            }
        }
    }


    private void setupContent(ViewGroup contentPanel) {
        mScrollView = contentPanel.findViewById(R.id.scrollView);
        mScrollView.setFocusable(false);
        mScrollView.setNestedScrollingEnabled(false);
        // Special case for users that only want to display a String
        mMessageView = contentPanel.findViewById(android.R.id.message);
        if (mMessageView == null) {
            return;
        }
        if (mMessage != null) {
            mMessageView.setText(mMessage);
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);
            if (mListView != null) {
                final ViewGroup scrollParent = (ViewGroup) mScrollView.getParent();
                final int childIndex = scrollParent.indexOfChild(mScrollView);
                scrollParent.removeViewAt(childIndex);
                scrollParent.addView(mListView, childIndex,
                    new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }
    }


    private static void manageScrollIndicators(View v, View upIndicator, View downIndicator) {
        if (upIndicator != null) {
            upIndicator.setVisibility(
                v.canScrollVertically(-1) ? View.VISIBLE : View.INVISIBLE);
        }
        if (downIndicator != null) {
            downIndicator.setVisibility(
                v.canScrollVertically(1) ? View.VISIBLE : View.INVISIBLE);
        }
    }


    private void setupButtons(ViewGroup buttonPanel) {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;
        mButtonPositive = buttonPanel.findViewById(android.R.id.button1);
        mButtonPositive.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonPositiveText) && mButtonPositiveIcon == null) {
            mButtonPositive.setVisibility(View.GONE);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            if (mButtonPositiveIcon != null) {
                mButtonPositiveIcon.setBounds(0, 0, mButtonIconDimen, mButtonIconDimen);
                mButtonPositive.setCompoundDrawables(mButtonPositiveIcon, null, null, null);
            }
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }
        mButtonNegative = buttonPanel.findViewById(android.R.id.button2);
        mButtonNegative.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonNegativeText) && mButtonNegativeIcon == null) {
            mButtonNegative.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            if (mButtonNegativeIcon != null) {
                mButtonNegativeIcon.setBounds(0, 0, mButtonIconDimen, mButtonIconDimen);
                mButtonNegative.setCompoundDrawables(mButtonNegativeIcon, null, null, null);
            }
            mButtonNegative.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }
        mButtonNeutral = buttonPanel.findViewById(android.R.id.button3);
        mButtonNeutral.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonNeutralText) && mButtonNeutralIcon == null) {
            mButtonNeutral.setVisibility(View.GONE);
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            if (mButtonPositiveIcon != null) {
                mButtonPositiveIcon.setBounds(0, 0, mButtonIconDimen, mButtonIconDimen);
                mButtonPositive.setCompoundDrawables(mButtonPositiveIcon, null, null, null);
            }
            mButtonNeutral.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }
        if (shouldCenterSingleButton(mContext)) {
            /*
             * If we only have 1 button it should be centered on the layout and
             * expand to fill 50% of the available space.
             */
            if (whichButtons == BIT_BUTTON_POSITIVE) {
                centerButton(mButtonPositive);
            } else if (whichButtons == BIT_BUTTON_NEGATIVE) {
                centerButton(mButtonNegative);
            } else if (whichButtons == BIT_BUTTON_NEUTRAL) {
                centerButton(mButtonNeutral);
            }
        }
        final boolean hasButtons = whichButtons != 0;
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
        }
    }


    private void centerButton(Button button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.weight = 0.5f;
        button.setLayoutParams(params);
    }





    public static class AlertParams {
        final Context mContext;
        final LayoutInflater mInflater;
        int theme = 0;
        int mIconId = 0;
        Drawable mIcon;
        int mIconAttrId = 0;
        CharSequence mTitle;
        View mCustomTitleView;
        CharSequence mMessage;
        CharSequence mPositiveButtonText;
        Drawable mPositiveButtonIcon;
        DialogInterface.OnClickListener mPositiveButtonListener;
        CharSequence mNegativeButtonText;
        Drawable mNegativeButtonIcon;
        DialogInterface.OnClickListener mNegativeButtonListener;
        CharSequence mNeutralButtonText;
        Drawable mNeutralButtonIcon;
        DialogInterface.OnClickListener mNeutralButtonListener;
        boolean mCancelable;
        DialogInterface.OnCancelListener mOnCancelListener;
        DialogInterface.OnDismissListener mOnDismissListener;
        DialogInterface.OnKeyListener mOnKeyListener;
        CharSequence[] mItems;
        ListAdapter mAdapter;
        DialogInterface.OnClickListener mOnClickListener;
        int mViewLayoutResId;
        View mView;
        int mViewSpacingLeft;
        int mViewSpacingTop;
        int mViewSpacingRight;
        int mViewSpacingBottom;
        boolean mViewSpacingSpecified = false;
        boolean[] mCheckedItems;
        boolean mIsMultiChoice;
        boolean mIsSingleChoice;
        int mCheckedItem = -1;
        DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        Cursor mCursor;
        String mLabelColumn;
        String mIsCheckedColumn;
        boolean mForceInverseBackground;
        AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        OnPrepareListViewListener mOnPrepareListViewListener;
        boolean mRecycleOnMeasure = true;


        /**
         * Interface definition for a callback to be invoked before the ListView will be bound to an
         * adapter.
         */
        public interface OnPrepareListViewListener {
            /**
             * Called before the ListView is bound to an adapter.
             *
             * @param listView The ListView that will be shown in the dialog.
             */
            void onPrepareListView(ListView listView);
        }


        AlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        public void apply(AlertController dialog) {
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId != 0) {
                    dialog.setIcon(mIconId);
                }
                if (mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null || mPositiveButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
                    mPositiveButtonListener, null, mPositiveButtonIcon);
            }
            if (mNegativeButtonText != null || mNegativeButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
                    mNegativeButtonListener, null, mNegativeButtonIcon);
            }
            if (mNeutralButtonText != null || mNeutralButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
                    mNeutralButtonListener, null, mNeutralButtonIcon);
            }
            // For a list, the client can either supply an array of items or an
            // adapter or a cursor
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight,
                        mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            } else if (mViewLayoutResId != 0) {
                dialog.setView(mViewLayoutResId);
            }
           /*
           dialog.setCancelable(mCancelable);
           dialog.setOnCancelListener(mOnCancelListener);
           if (mOnKeyListener != null) {
               dialog.setOnKeyListener(mOnKeyListener);
           }
           */
        }


        private void createListView(final AlertController dialog) {
            final ListView listView =
                (ListView) mInflater.inflate(dialog.mListLayout, null);
            final ListAdapter adapter;
            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(
                        mContext, dialog.mMultiChoiceItemLayout, android.R.id.text1, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (mCheckedItems != null) {
                                boolean isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;
                        private final int mIsCheckedIndex;


                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }


                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            CheckedTextView text = view.findViewById(android.R.id.text1);
                            text.setText(cursor.getString(mLabelIndex));
                            listView.setItemChecked(cursor.getPosition(),
                                cursor.getInt(mIsCheckedIndex) == 1);
                        }


                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return mInflater.inflate(dialog.mMultiChoiceItemLayout,
                                parent, false);
                        }

                    };
                }
            } else {
                final int layout;
                if (mIsSingleChoice) {
                    layout = dialog.mSingleChoiceItemLayout;
                } else {
                    layout = dialog.mListItemLayout;
                }

                if (mCursor != null) {
                    adapter = new SimpleCursorAdapter(mContext, layout, mCursor,
                        new String[] { mLabelColumn }, new int[] { android.R.id.text1 });
                } else if (mAdapter != null) {
                    adapter = mAdapter;
                } else {
                    adapter = new CheckedItemAdapter(mContext, layout, android.R.id.text1, mItems);
                }
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }

            /* Don't directly set the adapter on the ListView as we might
             * want to add a footer to the ListView later.
             */
            dialog.mAdapter = adapter;
            dialog.mCheckedItem = mCheckedItem;

            if (mOnClickListener != null) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        mOnClickListener.onClick(dialog.mDialog, position);
                        if (!mIsSingleChoice) {
                            dialog.mDialog.dismiss();
                        }
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(
                            dialog.mDialog, position, listView.isItemChecked(position));
                    }
                });
            }

            // Attach a given OnItemSelectedListener to the ListView
            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            dialog.mListView = listView;
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        CheckedItemAdapter(Context context, int resource, int textViewResourceId,
                           CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }


        @Override
        public boolean hasStableIds() {
            return true;
        }


        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}