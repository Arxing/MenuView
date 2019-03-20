package org.arxing.menuview.handler;

import android.support.annotation.Px;
import android.view.View;

import org.arxing.menuview.AnimHandler;
import org.arxing.menuview.MenuView;
import org.arxing.menuview.Orientation;

public class TranslationAnimHandler extends AnimHandler {
    private float maxMoveDistance;
    private int orientation;
    private float originX;
    private float originY;
    private boolean init;

    public TranslationAnimHandler(@Px int distance, @Orientation int orientation) {
        this.maxMoveDistance = distance;
        this.orientation = orientation;
    }

    public TranslationAnimHandler(@Px int distance) {
        this.maxMoveDistance = distance;
    }

    @Override public void syncingToRatio(MenuView menuView, int orientation, View view, float ratio) {
        if (!init) {
            originX = view.getX();
            originY = view.getY();
            init = true;
        }
        float moveDistance = maxMoveDistance * ratio;
        float newX = originX;
        float newY = originY;
        this.orientation = this.orientation == 0 ? orientation : this.orientation;

        switch (this.orientation) {
            case MenuView.ORIENTATION_LEFT:
                newX += moveDistance;
                break;
            case MenuView.ORIENTATION_TOP:
                newY -= moveDistance;
                break;
            case MenuView.ORIENTATION_RIGHT:
                newX -= moveDistance;
                break;
            case MenuView.ORIENTATION_BOTTOM:
                newY += moveDistance;
                break;
        }
        view.setX(newX);
        view.setY(newY);
    }

    private float computeDistance(float startX, float startY, float endX, float endY, @Orientation int orientation) {
        float maxMoveDistance = 0;
        switch (orientation) {
            case MenuView.ORIENTATION_LEFT:
            case MenuView.ORIENTATION_RIGHT:
                maxMoveDistance = Math.abs(startX - endX);
                break;
            case MenuView.ORIENTATION_TOP:
            case MenuView.ORIENTATION_BOTTOM:
                maxMoveDistance = Math.abs(startY - endY);
                break;
        }
        return maxMoveDistance;
    }
}
