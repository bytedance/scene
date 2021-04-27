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

import android.animation.IntEvaluator;
import android.graphics.Matrix;
import android.util.IntProperty;
import android.util.Property;
import android.widget.ImageView;

import com.bytedance.scene.animation.interaction.evaluator.MatrixEvaluator;

import java.util.HashMap;
import java.util.Set;

public class ImageViewAnimationBuilder extends ViewOtherAnimationBuilder<ImageViewAnimationBuilder> {
    private static final Property<ImageView, Integer> IMAGE_ALPHA = new IntProperty<ImageView>("imageAlpha") {
        @Override
        public void setValue(ImageView object, int value) {
            object.setImageAlpha(value);
        }

        @Override
        public Integer get(ImageView object) {
            return object.getImageAlpha();
        }
    };

    private static final Property<ImageView, Matrix> IMAGE_MATRIX = new Property<ImageView, Matrix>(Matrix.class, "imageMatrix") {
        @Override
        public void set(ImageView object, Matrix value) {
            object.setImageMatrix(value);
        }

        @Override
        public Matrix get(ImageView object) {
            return object.getImageMatrix();
        }
    };

    private ImageView mView;
    private HashMap<Property, Holder> hashMap = new HashMap<>();

    ImageViewAnimationBuilder(ImageView view) {
        super(view);
        this.mView = view;
    }

    public ImageViewAnimationBuilder imageAlpha(int fromValue, int toValue) {
        hashMap.put(IMAGE_ALPHA, new Holder(new IntEvaluator(), fromValue, toValue));
        return this;
    }

    public ImageViewAnimationBuilder imageAlphaBy(int deltaValue) {
        return imageAlpha(mView.getImageAlpha() + deltaValue);
    }

    public ImageViewAnimationBuilder imageAlpha(int value) {
        return imageAlpha(mView.getImageAlpha(), value);
    }

    public ImageViewAnimationBuilder imageMatrix(Matrix fromValue, Matrix toValue) {
        hashMap.put(IMAGE_MATRIX, new Holder(new MatrixEvaluator(), fromValue, toValue));
        return this;
    }

    public ImageViewAnimationBuilder imageMatrixBy(Matrix deltaValue) {
        Matrix matrix = new Matrix(mView.getImageMatrix());
        matrix.postConcat(deltaValue);
        return imageMatrix(matrix);
    }

    public ImageViewAnimationBuilder imageMatrix(Matrix value) {
        return imageMatrix(mView.getImageMatrix(), value);
    }

    @Override
    protected void onProgress(float progress) {
        super.onProgress(progress);

        Set<Property> set = hashMap.keySet();
        for (Property property : set) {
            Holder value = hashMap.get(property);
            property.set(mView, value.typeEvaluator.evaluate(progress, value.fromValue, value.toValue));
        }
    }
}
