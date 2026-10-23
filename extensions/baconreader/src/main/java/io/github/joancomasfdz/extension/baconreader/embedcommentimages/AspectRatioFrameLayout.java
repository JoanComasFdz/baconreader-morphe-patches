package io.github.joancomasfdz.extension.baconreader.embedcommentimages;

import android.content.Context;
import android.widget.FrameLayout;

/**
 * A FrameLayout whose height follows its width (height = width * ratio), capped at maxHeight.
 * <p>
 * Ported from ReVanced Patches (GPLv3):
 * https://gitlab.com/ReVanced/revanced-patches
 */
final class AspectRatioFrameLayout extends FrameLayout {
    private float ratio;
    private final int maxHeight;

    AspectRatioFrameLayout(Context context, float ratio, int maxHeight) {
        super(context);
        this.ratio = ratio;
        this.maxHeight = maxHeight;
    }

    void setRatio(float ratio) {
        if (this.ratio == ratio) return;
        this.ratio = ratio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.min(Math.round(width * ratio), maxHeight);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
