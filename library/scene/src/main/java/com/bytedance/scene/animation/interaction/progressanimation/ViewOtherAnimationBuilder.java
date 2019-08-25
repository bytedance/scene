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

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import com.bytedance.scene.animation.interaction.evaluator.RectEvaluator;

import java.util.HashMap;
import java.util.Set;

public class ViewOtherAnimationBuilder<T> extends ViewAnimationBuilder<T> {
    private static final Property<View, Rect> CLIP = new Property<View, Rect>(Rect.class, "clip") {
        @Override
        public void set(View object, Rect value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                object.setClipBounds(value);
            }
        }

        @Override
        public Rect get(View object) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return object.getClipBounds();
            } else {
                return new Rect();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static final Property<View, Float> BOUNDS_RADIUS = new Property<View, Float>(Float.class, "bounds_radius") {
        private float mValue = 0;
        private final ViewOutlineProvider mViewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mValue);
            }
        };

        private Outline outline = new Outline();

        @Override
        public void set(View object, final Float value) {
            this.mValue = value;
            object.setOutlineProvider(mViewOutlineProvider);
        }

        @Override
        public Float get(View object) {
            ViewOutlineProvider outlineProvider = object.getOutlineProvider();
            if (outlineProvider == null) {
                return 0.0f;
            }
            outlineProvider.getOutline(object, outline);
            return outline.getRadius();
        }
    };

    private static final Property<View, Integer> SCROLL_X = new Property<View, Integer>(Integer.class, "scroll_x") {
        @Override
        public void set(View object, final Integer value) {
            object.scrollTo(value, object.getScrollY());
        }

        @Override
        public Integer get(View object) {
            return object.getScrollX();
        }
    };

    private static final Property<View, Integer> SCROLL_Y = new Property<View, Integer>(Integer.class, "scroll_y") {
        @Override
        public void set(View object, final Integer value) {
            object.scrollTo(object.getScrollX(), value);
        }

        @Override
        public Integer get(View object) {
            return object.getScrollY();
        }
    };

    private static final Property<View, Rect> BOUNDS = new Property<View, Rect>(Rect.class, "bounds") {
        @Override
        public void set(View object, final Rect value) {
            //todo 优化，用setLeftTopRightBottom反射
//            ViewUtils.setLeftTopRightBottom(object, mLeft, mTop, mRight, mBottom);
            object.setLeft(value.left);
            object.setTop(value.top);
            object.setRight(value.right);
            object.setBottom(value.bottom);
        }

        @Override
        public Rect get(View object) {
            ViewGroup viewGroup = (ViewGroup) object.getParent();
            Rect rect = new Rect();
            viewGroup.offsetDescendantRectToMyCoords(viewGroup, rect);
            return rect;
        }
    };

    private HashMap<Property, Holder> hashMap = new HashMap<>();

    ViewOtherAnimationBuilder(View view) {
        super(view);
    }

//    //todo
//    public T translation(Path path) {
//
//    }

    public T clip(Rect fromValue, Rect toValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            hashMap.put(CLIP, new Holder(new RectEvaluator(), fromValue, toValue));
        }
        return (T) this;
    }

    public T clip(Rect value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return clip(mView.getClipBounds(), value);
        } else {
            return (T) this;
        }
    }

    public T boundsRadius(float toValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Outline outline = new Outline();
            mView.getOutlineProvider().getOutline(mView, outline);
            float radius = outline.getRadius();
            return boundsRadius(radius, toValue);
        }
        return (T) this;
    }

    public T boundsRadius(float fromValue, float toValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hashMap.put(BOUNDS_RADIUS, new Holder(new FloatEvaluator(), fromValue, toValue));
        }
        return (T) this;
    }

    public T boundsRadiusBy(float deltaValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Outline outline = new Outline();
            mView.getOutlineProvider().getOutline(mView, outline);
            float radius = outline.getRadius();
            return boundsRadius(radius, radius + deltaValue);
        }
        return (T) this;
    }

    public T bounds(Rect fromValue, Rect toValue) {
        hashMap.put(BOUNDS, new Holder(new RectEvaluator(), fromValue, toValue));
        return (T) this;
    }

    public T bounds(Rect toValue) {
        ViewGroup viewGroup = (ViewGroup) mView.getParent();
        Rect rect = new Rect();
        viewGroup.offsetDescendantRectToMyCoords(viewGroup, rect);
        return bounds(rect, toValue);
    }

    public T scrollX(int fromValue, int toValue) {
        hashMap.put(SCROLL_X, new Holder(new IntEvaluator(), fromValue, toValue));
        return (T) this;
    }

    public T scrollX(int toValue) {
        return scrollX(mView.getScrollX(), toValue);
    }

    public T scrollXBy(int deltaValue) {
        return scrollX(mView.getScrollX(), mView.getScrollX() + deltaValue);
    }

    public T scrollY(int fromValue, int toValue) {
        hashMap.put(SCROLL_Y, new Holder(new IntEvaluator(), fromValue, toValue));
        return (T) this;
    }

    public T scrollY(int toValue) {
        return scrollY(mView.getScrollY(), toValue);
    }

    public T scrollYBy(int deltaValue) {
        return scrollY(mView.getScrollY(), mView.getScrollY() + deltaValue);
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
