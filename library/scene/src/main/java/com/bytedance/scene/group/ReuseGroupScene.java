package com.bytedance.scene.group;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by JiangQi on 8/13/18.
 */
public abstract class ReuseGroupScene extends GroupScene {
    private ViewGroup mReuseView;
    private LayoutInflater mReuseLayoutInflater;
    private android.support.v7.view.ContextThemeWrapper mReuseContextThemeWrapper;
    private int mReuseViewHostActivityIdentifyHashCode = -1;
    private int mReuseViewHostActivityThemeIdentifyHashCode = -1;

    @Override
    public void onAttach() {
        super.onAttach();
        if (this.mReuseViewHostActivityIdentifyHashCode == -1 && this.mReuseViewHostActivityThemeIdentifyHashCode == -1) {
            return;
        }
        if (this.mReuseViewHostActivityIdentifyHashCode != requireActivity().hashCode() ||
                this.mReuseViewHostActivityThemeIdentifyHashCode != requireActivity().getTheme().hashCode()) {
            this.mReuseView = null;
            this.mReuseLayoutInflater = null;
            this.mReuseContextThemeWrapper = null;
        }
    }

    @NonNull
    @Override
    public final ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*
         * Can not be reused during the animation process.
         * Once the Parent is found, throw an Exception.
         */
        if (this.mReuseView != null) {
            if (this.mReuseView.getParent() != null) {
                throw new IllegalArgumentException("ReuseGroupScene reuseView already have parent");
            }
        }

        if (this.mReuseView != null) {
            return this.mReuseView;
        } else {
            return onCreateNewView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public final LayoutInflater onGetLayoutInflater() {
        if (getActivity() == null) {
            throw new IllegalStateException("onGetLayoutInflater() cannot be executed until the "
                    + "Scene is attached to the Activity.");
        }

        if (this.mReuseLayoutInflater != null) {
            return this.mReuseLayoutInflater;
        }
        return super.onGetLayoutInflater();
    }

    @Nullable
    @Override
    public ContextThemeWrapper onGetSceneContext() {
        if (this.mReuseContextThemeWrapper != null) {
            return this.mReuseContextThemeWrapper;
        }
        return super.onGetSceneContext();
    }

    @NonNull
    protected abstract ViewGroup onCreateNewView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mReuseViewHostActivityIdentifyHashCode = requireActivity().hashCode();
        this.mReuseViewHostActivityThemeIdentifyHashCode = requireActivity().getTheme().hashCode();
        this.mReuseLayoutInflater = getLayoutInflater();
        this.mReuseContextThemeWrapper = requireSceneContext();
        this.mReuseView = (ViewGroup) getView();
    }
}
