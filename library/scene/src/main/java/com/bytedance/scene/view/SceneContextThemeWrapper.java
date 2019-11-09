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
package com.bytedance.scene.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.LayoutInflater;
import com.bytedance.scene.R;

import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * A ContextWrapper that allows you to modify the theme from what is in the
 * wrapped context.
 * <p>
 * source code is copied from android.support.v7.view.ContextThemeWrapper, the difference is setTheme() method,
 * SceneContextThemeWrapper setTheme will not modify Activity's theme
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SceneContextThemeWrapper extends ContextWrapper {
    private int mThemeResource;
    private Resources.Theme mTheme;
    private LayoutInflater mInflater;
    private Configuration mOverrideConfiguration;
    private Resources mResources;
    private boolean mIsThemeFromActivity;

    /**
     * Creates a new context wrapper with the specified theme.
     * <p>
     * The specified theme will be applied on top of the base context's theme.
     * Any attributes not explicitly defined in the theme identified by
     * <var>themeResId</var> will retain their original values.
     *
     * @param base       the base context
     * @param themeResId the resource ID of the theme to be applied on top of
     *                   the base context's theme
     */
    public SceneContextThemeWrapper(@NonNull Context base, @StyleRes int themeResId) {
        super(base);
        mThemeResource = themeResId;
        if (mThemeResource == 0) {
            throw new IllegalArgumentException("themeResId can't be zero");
        }
    }

    /**
     * Creates a new context wrapper with the specified theme.
     * <p>
     * Unlike {@link #SceneContextThemeWrapper(Context, int)}, the theme passed to
     * this constructor will completely replace the base context's theme.
     *
     * @param base  the base context
     * @param theme the theme against which resources should be inflated
     */
    public SceneContextThemeWrapper(@NonNull Context base, @NonNull Resources.Theme theme) {
        super(base);
        mTheme = Utility.requireNonNull(theme, "theme can't be null");
        mIsThemeFromActivity = true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    /**
     * Call to set an "override configuration" on this context -- this is
     * a configuration that replies one or more values of the standard
     * configuration that is applied to the context.  See
     * {@link Context#createConfigurationContext(Configuration)} for more
     * information.
     *
     * <p>This method can only be called once, and must be called before any
     * calls to {@link #getResources()} or {@link #getAssets()} are made.
     */
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (mResources != null) {
            throw new IllegalStateException(
                    "getResources() or getAssets() has already been called");
        }
        if (mOverrideConfiguration != null) {
            throw new IllegalStateException("Override configuration has already been set");
        }
        mOverrideConfiguration = new Configuration(overrideConfiguration);
    }

    /**
     * Used by ActivityThread to apply the overridden configuration to onConfigurationChange
     * callbacks.
     *
     * @hide
     */
    public Configuration getOverrideConfiguration() {
        return mOverrideConfiguration;
    }

    @Override
    public Resources getResources() {
        return getResourcesInternal();
    }

    private Resources getResourcesInternal() {
        if (mResources == null) {
            if (mOverrideConfiguration == null) {
                mResources = super.getResources();
            } else if (Build.VERSION.SDK_INT >= 17) {
                final Context resContext = createConfigurationContext(mOverrideConfiguration);
                mResources = resContext.getResources();
            }
        }
        return mResources;
    }

    @Override
    public void setTheme(@StyleRes int resid) {
        if (mThemeResource != resid) {
            mThemeResource = resid;
            if (mIsThemeFromActivity) {
                //reset
                mTheme = null;
                mResources = null;
            }
            initializeTheme();
        }
    }

    public int getThemeResId() {
        return mThemeResource;
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme != null) {
            return mTheme;
        }

        initializeTheme();
        return mTheme;
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return mInflater;
        }
        return getBaseContext().getSystemService(name);
    }

    /**
     * Called by {@link #setTheme} and {@link #getTheme} to apply a theme
     * resource to the current Theme object.  Can override to change the
     * default (simple) behavior.  This method will not be called in multiple
     * threads simultaneously.
     *
     * @param theme The Theme object being modified.
     * @param resid The theme style resource being applied to <var>theme</var>.
     * @param first Set to true if this is the first time a style is being
     *              applied to <var>theme</var>.
     */
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(resid, true);
    }

    private void initializeTheme() {
        final boolean first = mTheme == null;
        if (first) {
            mTheme = getResources().newTheme();
            Resources.Theme theme = getBaseContext().getTheme();
            if (theme != null) {
                mTheme.setTo(theme);
            }
        }
        onApplyThemeResource(mTheme, mThemeResource, first);
    }

    @Override
    public AssetManager getAssets() {
        // Ensure we're returning assets with the correct configuration.
        return getResources().getAssets();
    }
}