package org.arxing.menuview;

import android.animation.ValueAnimator;
import android.support.annotation.FloatRange;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import org.arxing.utils.WeakReferenceHandler;


/**
 * 負責監聽手勢並發射出ratio及同步外部設置的ratio
 */
class GestureManager extends GestureDetector.SimpleOnGestureListener {
    private GestureAdapter adapter;
    private float totalMoveDistance;
    private OnRatioListener ratioListener;
    private OnProgressListener progressListener;
    private WeakReferenceHandler<GestureManager> handler;
    private long autoAnimDuration = 300;

    GestureManager(GestureAdapter adapter, OnRatioListener listener, OnProgressListener progressListener) {
        this.adapter = adapter;
        this.ratioListener = listener;
        this.progressListener = progressListener;
        this.handler = new WeakReferenceHandler<>(this);
    }

    public void syncRatio(float ratio) {
        this.totalMoveDistance = adapter.getMaxProgress() * ratio;
        ratioListener.onSyncingToRatio(ratio);
        progressListener.onProgressChanged(adapter.getMaxProgress(), totalMoveDistance);
    }

    public void syncRatioSmooth(float ratio) {
        startInterpolatorAnim(currentRatio(), ratio);
    }

    @Override public boolean onDown(MotionEvent e) {
        return adapter.handleDown(e.getX(), e.getY());
    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
        return adapter.handleSingleTapUp(e.getX(), e.getY());
    }

    //往左 dx>0 往右 dx<0
    //往上 dy>0 往下 dy<0
    @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        totalMoveDistance = adapter.handleScroll(totalMoveDistance, distanceX, distanceY);
        float maxProgress = adapter.getMaxProgress();
        totalMoveDistance = totalMoveDistance > maxProgress ? maxProgress : totalMoveDistance < 0 ? 0 : totalMoveDistance;
        ratioListener.onSyncingToRatio(currentRatio());
        progressListener.onProgressChanged(adapter.getMaxProgress(), totalMoveDistance);

        handler.removeCallbacks(interpolatorAnimTask);
        handler.postDelayed(interpolatorAnimTask, autoAnimDuration);
        return true;
    }

    @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        handler.removeCallbacks(interpolatorAnimTask);
        handler.post(interpolatorAnimTask);
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    //補間動畫
    private Runnable interpolatorAnimTask = () -> startInterpolatorAnim(currentRatio(),
                                                                        currentRatio() > adapter.getAutoAnimThreshold() ? 1 : 0);

    void startInterpolatorAnim(float fromRatio, float toRatio) {
        ValueAnimator va = ValueAnimator.ofFloat(fromRatio, toRatio);
        va.setDuration(autoAnimDuration);
        va.setInterpolator(adapter.getAnimInterpolator());
        va.addUpdateListener(animation -> syncRatio((float) animation.getAnimatedValue()));
        va.start();
    }

    public float currentRatio() {
        return totalMoveDistance / adapter.getMaxProgress();
    }

    public interface GestureAdapter {

        float getMaxProgress();

        boolean handleDown(float x, float y);

        boolean handleSingleTapUp(float x, float y);

        float handleScroll(float currentProgress, float moveX, float moveY);

        @FloatRange(from = 0, to = 1) float getAutoAnimThreshold();

        Interpolator getAnimInterpolator();
    }
}
