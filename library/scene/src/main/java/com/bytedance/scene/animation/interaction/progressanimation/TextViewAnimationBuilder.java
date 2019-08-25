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
