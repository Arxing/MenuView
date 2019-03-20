package org.arxing.menuview.handler;

import android.support.annotation.FloatRange;
import android.view.View;

import org.arxing.menuview.AnimHandler;
import org.arxing.menuview.MenuView;


public class AlphaAnimHandler extends AnimHandler {
    private float start;
    private float stop;

    public AlphaAnimHandler(@FloatRange(from = 0, to = 1) float start, @FloatRange(from = 0, to = 1) float stop) {
        this.start = start;
        this.stop = stop;
    }

    @Override public void syncingToRatio(MenuView menuView, int orientation, View view, float ratio) {
        float val = computeCurrentF(start, stop, ratio);
        view.setAlpha(val);
    }
}
