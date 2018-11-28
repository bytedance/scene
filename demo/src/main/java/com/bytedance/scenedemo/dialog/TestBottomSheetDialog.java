package com.bytedance.scenedemo.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by JiangQi on 11/25/18.
 */
//public class TestBottomSheetDialog extends BottomSheetDialogScene {
//    @NonNull
//    @Override
//    protected View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
//        LinearLayout view = new LinearLayout(getActivity());
//        view.setOrientation(LinearLayout.VERTICAL);
//
//        TextView first = new TextView(getActivity());
//        first.setBackgroundColor(Color.BLUE);
//        first.setText("Expand Me!");
//        first.setTextColor(Color.WHITE);
//        first.setGravity(Gravity.CENTER_VERTICAL);
//        first.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//        view.addView(first, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));
//
//        View second = new View(getActivity());
//        second.setBackgroundColor(Color.YELLOW);
//        view.addView(second, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1600));
//        return view;
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        getBehavior().setPeekHeight(400);
//    }
//}
