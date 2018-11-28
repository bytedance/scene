package com.bytedance.scenedemo.navigation.softkeyboard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bytedance.scene.group.GroupScene;

/**
 * Created by JiangQi on 8/19/18.
 */
public class SoftkeyboardDemoScene extends GroupScene {
    EditText textView;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        textView = new EditText(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setText("返回");
        button.setAllCaps(false);

        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().pop();
            }
        });

        button = new Button(getActivity());
        button.setText("新页面 Pan");
        button.setAllCaps(false);

        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyPanScene.class);
            }
        });

        button = new Button(getActivity());
        button.setText("新页面 Resize");
        button.setAllCaps(false);

        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyResizeScene.class);
            }
        });

        button = new Button(getActivity());
        button.setText("新页面 Nothing");
        button.setAllCaps(false);

        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyNothingScene.class);
            }
        });

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        focusAndShowInputMethod(textView);
    }

    public static class EmptyPanScene extends GroupScene {
        EditText et;

        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout view = new LinearLayout(getActivity());
            view.setOrientation(LinearLayout.VERTICAL);

            Button button = new Button(getActivity());
            button.setText("返回");
            button.setAllCaps(false);

            view.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNavigationScene().pop();
                }
            });

            View space = new View(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            layoutParams.weight = 1;
            view.addView(space, layoutParams);

            et = new EditText(getActivity());
            view.addView(et);
            et.setText("ddddddd");

            return view;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

//            getActivity().getWindow().getAttributes().softInputMode
        }

        @Override
        public void onResume() {
            super.onResume();
            focusAndShowInputMethod(et);
        }
    }

    public static class EmptyResizeScene extends GroupScene {
        EditText et;

        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            ScrollView scrollView = new ScrollView(getActivity());
            scrollView.setFillViewport(true);
            LinearLayout view = new LinearLayout(getActivity());
            view.setOrientation(LinearLayout.VERTICAL);

            Button button = new Button(getActivity());
            button.setText("返回");
            button.setAllCaps(false);

            view.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNavigationScene().pop();
                }
            });

            View space = new View(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            layoutParams.weight = 1;
            view.addView(space, layoutParams);

            et = new EditText(getActivity());
            view.addView(et);
            et.setText("ddddddd");

            scrollView.addView(view);
            return scrollView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

//            getActivity().getWindow().getAttributes().softInputMode
        }

        @Override
        public void onResume() {
            super.onResume();
            focusAndShowInputMethod(et);
        }
    }

    public static class EmptyNothingScene extends GroupScene {

        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            LinearLayout view = new LinearLayout(getActivity());
            view.setOrientation(LinearLayout.VERTICAL);

            Button button = new Button(getActivity());
            button.setText("返回");
            button.setAllCaps(false);

            view.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNavigationScene().pop();
                }
            });

            View space = new View(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            layoutParams.weight = 1;
            view.addView(space, layoutParams);

            EditText et = new EditText(getActivity());
            view.addView(et);
            et.setText("ddddddd");

            return view;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    public static void focusAndShowInputMethod(EditText editText) {
        if (editText == null) {
            return;
        }
        editText.requestFocus();
        //防止过滤器没生效，增加兜底策略，重新设置一次Text。
        editText.setText(editText.getText());
        editText.setSelection(TextUtils.isEmpty(editText.getText()) ? 0 : editText.getText().length());
        ((InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
    }
}
