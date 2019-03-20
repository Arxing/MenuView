package org.arxing.menuview.handler;

import android.view.View;

import org.arxing.menuview.AnimHandler;
import org.arxing.menuview.MenuView;

public class ScaleAnimHandler extends AnimHandler {
    private float minScale;
    private float maxScale;

    private float currentScale;

    public ScaleAnimHandler(float minScale, float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    public ScaleAnimHandler() {
        this(1f, 1.5f);
    }

    public float getCurrentScale() {
        return currentScale;
    }

    @Override public void syncingToRatio(MenuView menuView, int orientation, View view, float ratio) {
        currentScale = computeCurrentF(minScale, maxScale, ratio);
        view.setScaleX(currentScale);
        view.setScaleY(currentScale);
    }
}
