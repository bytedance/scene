package com.bytedance.scene.animation.interaction.progressanimation;

import android.widget.TextView;

/**
 * Created by JiangQi on 9/2/18.
 */
public class TextViewAnimationBuilder extends ViewOtherAnimationBuilder<TextViewAnimationBuilder> {
    private TextView mView;

    TextViewAnimationBuilder(TextView view) {
        super(view);
        this.mView = view;
    }

//    public TextViewAnimationBuilder textColor(int fromValue, int toValue) {
//
//    }
//
//    public TextViewAnimationBuilder textColor(int toValue) {
//
//    }
//
//    public TextViewAnimationBuilder text(CharSequence fromValue, CharSequence toValue) {
//
//    }
//
//    public TextViewAnimationBuilder text(CharSequence toValue) {
//
//    }
}
