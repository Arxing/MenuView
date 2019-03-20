package org.arxing.menuview;

import android.view.View;

public abstract class AnimHandler<T extends View> {

    public abstract void syncingToRatio(MenuView menuView, @Orientation int orientation, T view, float ratio);

    public float computeCurrentF(float min, float max, float ratio) {
        return (max - min) * ratio + min;
    }

    public int computeCurrent(int min, int max, float ratio) {
        return (int) ((max - min) * ratio + min);
    }
}
