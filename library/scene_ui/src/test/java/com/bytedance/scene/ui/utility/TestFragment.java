package com.bytedance.scene.ui.utility;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.bytedance.scene.utlity.ViewIdGenerator;

public class TestFragment extends Fragment {
    public int mId = ViewIdGenerator.generateViewId();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout layout = new FrameLayout(requireActivity());
        layout.setId(mId);
        return layout;
    }
}
