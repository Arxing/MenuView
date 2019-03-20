package org.arxing.menuview.handler;

import android.view.View;

import org.arxing.menuview.AnimHandler;
import org.arxing.menuview.MenuView;

public class PushAnimHandler extends AnimHandler {
    private boolean init;
    private float originX;
    private float originY;

    @Override public void syncingToRatio(MenuView menuView, int orientation, View view, float ratio) {
        if (!init) {
            originX = view.getX();
            originY = view.getY();
            init = true;
        }
        float total = menuView.getProperties().getAbsoluteSize();
        float moveDistance = total * ratio;
        float newX = originX, newY = originY;

        switch (orientation) {
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
}
