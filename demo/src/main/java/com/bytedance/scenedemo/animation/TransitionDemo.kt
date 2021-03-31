package com.bytedance.scenedemo.animation;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.transition.AutoTransition;
import androidx.transition.ChangeTransform;
import androidx.transition.Slide;
import androidx.transition.Transition;

import com.bytedance.scene.animation.NavigationTransitionExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/23/18.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class TransitionDemo extends GroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.layout_transition_0, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                getNavigationScene().push(TransitionDemo2.class, null, new PushOptions.Builder().setAnimation(new Test()).build());
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));
    }

    public static class TransitionDemo2 extends GroupScene {

        @NonNull
        @Override
        public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return (ViewGroup) inflater.inflate(R.layout.layout_transition_1, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));
        }
    }

    class Test extends NavigationTransitionExecutor {
        @Override
        protected Transition getSharedElementTransition() {
            Transition autoTransition = new AutoTransition().addTransition(new ChangeTransform());
            autoTransition.addTarget("wo");
            autoTransition.addTarget("imageview");
            return autoTransition;
        }

        @Override
        protected Transition getOthersTransition() {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            return slide;
        }
    }
}