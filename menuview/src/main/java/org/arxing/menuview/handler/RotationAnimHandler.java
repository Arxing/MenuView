package org.arxing.menuview.handler;

import android.view.View;

import org.arxing.menuview.AnimHandler;
import org.arxing.menuview.MenuView;

public class RotationAnimHandler extends AnimHandler {
    private float startR, stopR;
    private float startRX, stopRX;
    private float startRY, stopRY;
    private boolean rEnabled, rxEnabled, ryEnabled;

    public RotationAnimHandler rotation(float start, float stop) {
        rEnabled = true;
        startR = start;
        stopR = stop;
        return this;
    }

    public RotationAnimHandler rotationX(float start, float stop) {
        rxEnabled = true;
        startRX = start;
        stopRX = stop;
        return this;
    }

    public RotationAnimHandler rotationY(float start, float stop) {
        ryEnabled = true;
        startRY = start;
        stopRY = stop;
        return this;
    }

    @Override public void syncingToRatio(MenuView menuView, int orientation, View view, float ratio) {
        if (rEnabled) {
            float val = computeCurrentF(startR, stopR, ratio);
            view.setRotation(val);
        }
        if (rxEnabled) {
            float val = computeCurrentF(startRX, stopRX, ratio);
            view.setRotationX(val);
        }
        if (ryEnabled) {
            float val = computeCurrentF(startRY, stopRY, ratio);
            view.setRotationY(val);
        }
    }
}
