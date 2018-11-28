package com.bytedance.scene.animation.interaction.progressanimation;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.View;

public class PathInteractionAnimation extends InteractionAnimation {
    private Path mPath;
    private View mView;
    private final PathMeasure mPathMeasure;
    private float aCoordinates[] = {0f, 0f};

    public PathInteractionAnimation(float endProgress, Path path, View view) {
        super(endProgress);
        this.mPath = path;
        this.mView = view;
        this.mPathMeasure = new PathMeasure(this.mPath, false);
    }

    @Override
    public void onProgress(float progress) {
        this.mPathMeasure.getPosTan(this.mPathMeasure.getLength() * progress, aCoordinates, null);
        this.mView.setTranslationX(aCoordinates[0]);
        this.mView.setTranslationY(aCoordinates[1]);
    }
}
