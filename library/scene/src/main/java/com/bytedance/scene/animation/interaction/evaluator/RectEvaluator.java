package com.bytedance.scene.animation.interaction.evaluator;

import android.animation.TypeEvaluator;
import android.graphics.Rect;

public class RectEvaluator implements TypeEvaluator<Rect> {

    /**
     * When null, a new Rect is returned on every evaluate call. When non-null,
     * mRect will be modified and returned on every evaluate.
     */
    private Rect mRect;

    /**
     * Construct a RectEvaluator that returns a new Rect on every evaluate call.
     * To avoid creating an object for each evaluate call,
     * {@link RectEvaluator#RectEvaluator(android.graphics.Rect)} should be used
     * whenever possible.
     */
    public RectEvaluator() {
    }

    /**
     * Constructs a RectEvaluator that modifies and returns <code>reuseRect</code>
     * in {@link #evaluate(float, android.graphics.Rect, android.graphics.Rect)} calls.
     * The value returned from
     * {@link #evaluate(float, android.graphics.Rect, android.graphics.Rect)} should
     * not be cached because it will change over time as the object is reused on each
     * call.
     *
     * @param reuseRect A Rect to be modified and returned by evaluate.
     */
    public RectEvaluator(Rect reuseRect) {
        mRect = reuseRect;
    }

    /**
     * This function returns the result of linearly interpolating the start and
     * end Rect values, with <code>fraction</code> representing the proportion
     * between the start and end values. The calculation is a simple parametric
     * calculation on each of the separate components in the Rect objects
     * (left, top, right, and bottom).
     *
     * <p>If {@link #RectEvaluator(android.graphics.Rect)} was used to construct
     * this RectEvaluator, the object returned will be the <code>reuseRect</code>
     * passed into the constructor.</p>
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue The start Rect
     * @param endValue   The end Rect
     * @return A linear interpolation between the start and end values, given the
     *         <code>fraction</code> parameter.
     */
    @Override
    public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
        int left = startValue.left + (int) ((endValue.left - startValue.left) * fraction);
        int top = startValue.top + (int) ((endValue.top - startValue.top) * fraction);
        int right = startValue.right + (int) ((endValue.right - startValue.right) * fraction);
        int bottom = startValue.bottom + (int) ((endValue.bottom - startValue.bottom) * fraction);
        if (mRect == null) {
            return new Rect(left, top, right, bottom);
        } else {
            mRect.set(left, top, right, bottom);
            return mRect;
        }
    }
}