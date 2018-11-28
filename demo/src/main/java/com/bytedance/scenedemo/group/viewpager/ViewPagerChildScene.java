package com.bytedance.scenedemo.group.viewpager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 7/30/18.
 */
public class ViewPagerChildScene extends UserVisibleHintGroupScene {
    int index = 0;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.child_scene_viewpager_item, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        index = bundle.getInt("index");
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));

//        getParentGroupScene().setFocusListener(this, new GroupScene.FocusListener() {
//            @Override
//            public void onFocus(boolean focus) {
//                if (!focus) {
//                    Toast.makeText(getContext(), "" + index + " 不可见", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((TextView) getView().findViewById(R.id.name)).setText("" + getStateHistory());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((TextView) getView().findViewById(R.id.name)).setText("" + getStateHistory());
    }

    private StringBuilder stringBuilder = new StringBuilder("setUserVisibleHint ");

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        stringBuilder.append(" " + isVisibleToUser);
    }
}
