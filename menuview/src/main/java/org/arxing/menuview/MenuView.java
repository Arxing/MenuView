package org.arxing.menuview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;


import com.annimon.stream.Stream;

import org.arxing.utils.Logger;
import org.arxing.utils.UnitParser;

import java.util.ArrayList;
import java.util.List;

public class MenuView extends FrameLayout {
    public final static int ORIENTATION_LEFT = 0;
    public final static int ORIENTATION_TOP = 1;
    public final static int ORIENTATION_RIGHT = 2;
    public final static int ORIENTATION_BOTTOM = 3;

    public final static int MODE_SLIDE = 1;
    public final static int MODE_PUSH = 2;

    private Logger logger = new Logger(MenuView.class.getSimpleName());
    private FrameLayout container;
    private View remoteBinder;
    private GestureDetector gestureDetector;
    private GestureManager gestureManager;
    private List<Pair<View, AnimHandler>> animHandlers = new ArrayList<>();
    private Paint touchPanPaint;
    private Interpolator interpolator;
    private Properties properties;
    private RectF touchPanRect = new RectF();
    private float ratio;
    private float progress;
    private float menuWidth;
    private float menuHeight;

    public class Properties {
        float absoluteSize;
        float percentSize;
        boolean usePercent;
        @Orientation int orientation;
        float touchPanSize;
        long animDuration;
        @ColorInt int touchPanColor;
        float autoAnimThreshold;
        boolean dragOpenEnabled;
        boolean maskEnabled;

        {
            absoluteSize = 0;
            percentSize = 0;
            usePercent = true;
            orientation = MenuView.ORIENTATION_LEFT;
            touchPanSize = UnitParser.dp2px(getContext(), 50);
            animDuration = 800;
            touchPanColor = Color.TRANSPARENT;
            autoAnimThreshold = 0.5f;
            dragOpenEnabled = true;
            maskEnabled = true;
        }

        public float getAbsoluteSize() {
            return absoluteSize;
        }

        public float getPercentSize() {
            return percentSize;
        }

        public boolean isUsePercent() {
            return usePercent;
        }

        public int getOrientation() {
            return orientation;
        }

        public float getTouchPanSize() {
            return touchPanSize;
        }

        public long getAnimDuration() {
            return animDuration;
        }

        public int getTouchPanColor() {
            return touchPanColor;
        }

        public float getAutoAnimThreshold() {
            return autoAnimThreshold;
        }

        public boolean isDragOpenEnabled() {
            return dragOpenEnabled;
        }

        public boolean isMaskEnabled() {
            return maskEnabled;
        }

        public boolean isVertical() {
            return orientation == MenuView.ORIENTATION_TOP || orientation == MenuView.ORIENTATION_BOTTOM;
        }
    }

    public MenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);

        setWillNotDraw(false);
        interpolator = new FastOutSlowInInterpolator();
        initGestureManager();
        initTouchPan();

        FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                      ViewGroup.LayoutParams.MATCH_PARENT);

        containerLayoutParams.gravity = getOrientationGravity(properties.orientation, false);
        container = new FrameLayout(getContext());
        container.setId(getContainerId());
        container.setLayoutParams(containerLayoutParams);
        //避免點到下面
        container.setFocusable(true);
        container.setClickable(true);
        addView(container);
    }

    private void initAttr(AttributeSet attrs) {
        properties = new Properties();
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MenuView);
            if (typedArray.hasValue(R.styleable.MenuView_menu_size)) {
                String strSize = typedArray.getString(R.styleable.MenuView_menu_size);
                if (strSize.endsWith("%") || strSize.endsWith("%p")) {
                    properties.usePercent = true;
                    properties.percentSize = typedArray.getFraction(R.styleable.MenuView_menu_size, 1, 1, properties.percentSize);
                } else {
                    properties.usePercent = false;
                    properties.absoluteSize = typedArray.getDimension(R.styleable.MenuView_menu_size, properties.absoluteSize);
                }
            }
            properties.orientation = typedArray.getInt(R.styleable.MenuView_menu_orientation, properties.orientation);
            properties.touchPanSize = typedArray.getDimension(R.styleable.MenuView_menu_touchPanSize, properties.touchPanSize);
            properties.touchPanColor = typedArray.getColor(R.styleable.MenuView_menu_touchPanColor, properties.touchPanColor);
            properties.animDuration = typedArray.getInt(R.styleable.MenuView_menu_appear_duration, (int) properties.animDuration);
            properties.autoAnimThreshold = typedArray.getFraction(R.styleable.MenuView_menu_auto_anim_threshold,
                                                                  1,
                                                                  1,
                                                                  properties.autoAnimThreshold);
            properties.dragOpenEnabled = typedArray.getBoolean(R.styleable.MenuView_menu_dragOpenEnabled, properties.dragOpenEnabled);
            properties.maskEnabled = typedArray.getBoolean(R.styleable.MenuView_menu_maskEnabled, properties.maskEnabled);
            typedArray.recycle();
        }
    }

    private void initGestureManager() {
        gestureManager = new GestureManager(new GestureAdapter(), ratioListener, progressListener);
        gestureDetector = new GestureDetector(getContext(), gestureManager);
    }

    private void initTouchPan() {
        touchPanPaint = new Paint();
        touchPanPaint.setColor(properties.touchPanColor);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        menuWidth = MeasureSpec.getSize(widthMeasureSpec);
        menuHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (properties.usePercent) {
            if (properties.isVertical()) {
                //直向
                properties.absoluteSize = menuHeight * properties.percentSize;
            } else {
                //橫向
                properties.absoluteSize = menuWidth * properties.percentSize;
            }
        }
        resetContainerSize();
        resetContainerLocation();
        resetTouchPanRect(menuWidth, menuHeight);
    }

    // return true: container點不到 底下的點的到
    // return false: container點的到 底下點不到
    @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (properties.dragOpenEnabled)
            return false;
        else
            return super.onInterceptTouchEvent(ev);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (properties.dragOpenEnabled)
            return gestureDetector.onTouchEvent(event);
        else
            return super.onTouchEvent(event);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(touchPanRect, touchPanPaint);
        if (properties.maskEnabled) {
            //畫遮罩
            int color = Color.argb((int) (200 * ratio), 0, 0, 0);
            canvas.drawColor(color);
        }
    }

    // private

    private void resetContainerSize() {
        FrameLayout.LayoutParams lp = getContainerLayoutParams();
        if (properties.isVertical()) {
            lp.height = (int) properties.absoluteSize;
        } else {
            lp.width = (int) properties.absoluteSize;
        }
        container.setLayoutParams(lp);
    }

    private void resetContainerLocation() {
        float distance = properties.absoluteSize;
        switch (properties.orientation) {
            case ORIENTATION_LEFT:
                container.setX(-distance);
                break;
            case ORIENTATION_TOP:
                container.setY(-distance);
                break;
            case ORIENTATION_RIGHT:
                container.setX(distance);
                break;
            case ORIENTATION_BOTTOM:
                container.setY(distance);
                break;
        }
    }

    private void resetTouchPanRect(float menuWidth, float menuHeight) {
        float l = 0, t = 0, r = 0, b = 0;
        switch (properties.orientation) {
            case ORIENTATION_LEFT:
                r = properties.touchPanSize;
                b = menuHeight;
                break;
            case ORIENTATION_TOP:
                r = menuWidth;
                b = properties.touchPanSize;
                break;
            case ORIENTATION_RIGHT:
                l = menuWidth - properties.touchPanSize;
                r = menuWidth;
                b = menuHeight;
                break;
            case ORIENTATION_BOTTOM:
                t = menuHeight - properties.touchPanSize;
                r = menuWidth;
                b = menuHeight;
                break;
        }
        touchPanRect.set(l, t, r, b);
    }

    private int getOrientationGravity(@Orientation int orientation, boolean reverse) {
        switch (orientation) {
            case ORIENTATION_LEFT:
                return !reverse ? Gravity.START : Gravity.END;
            case ORIENTATION_TOP:
                return !reverse ? Gravity.TOP : Gravity.BOTTOM;
            case ORIENTATION_RIGHT:
                return !reverse ? Gravity.END : Gravity.START;
            case ORIENTATION_BOTTOM:
                return !reverse ? Gravity.BOTTOM : Gravity.TOP;
            default:
                return Gravity.START;
        }
    }

    private void syncingTouchPan(float ratio) {
        float distance = properties.absoluteSize * ratio;

        switch (properties.orientation) {
            case ORIENTATION_LEFT:
                touchPanRect.offsetTo(distance, 0);
                logger.e("syncing %f", distance);
                break;
            case ORIENTATION_TOP:
                touchPanRect.offsetTo(0, distance);
                break;
            case ORIENTATION_RIGHT:
                touchPanRect.offsetTo(getWidth() - properties.touchPanSize - distance, 0);
                break;
            case ORIENTATION_BOTTOM:
                touchPanRect.offsetTo(0, getHeight() - properties.touchPanSize - distance);
                break;
        }
    }

    private void syncingContainer(float ratio) {
        float width = getWidth();
        float height = getHeight();
        float menuSize = properties.absoluteSize;
        float distance = menuSize * ratio;

        switch (properties.orientation) {
            case ORIENTATION_LEFT:
                container.setX(-menuSize + distance);
                break;
            case ORIENTATION_TOP:
                container.setY(-menuSize + distance);
                break;
            case ORIENTATION_RIGHT:
                container.setX(width - distance);
                break;
            case ORIENTATION_BOTTOM:
                container.setY(height - distance);
                break;
        }
    }

    private FrameLayout.LayoutParams getContainerLayoutParams() {
        FrameLayout.LayoutParams lp;
        if (container == null || container.getLayoutParams() == null) {
            lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            lp = (LayoutParams) container.getLayoutParams();
        }
        return lp;
    }

    /*
     * ===========================================================================================================
     *  Public Methods
     *
     * */

    public @IdRes int getContainerId() {
        return R.id.menuView_container;
    }

    public void syncToRatio(@FloatRange(from = 0, to = 1) float ratio) {
        gestureManager.syncRatio(ratio);
    }

    public void syncToRatioSmooth(@FloatRange(from = 0, to = 1) float ratio) {
        gestureManager.syncRatioSmooth(ratio);
    }

    public <T extends View> void registerAnimHandler(T view, AnimHandler handler) {
        animHandlers.add(new Pair<>(view, handler));
    }

    public <T extends View> void registerAnimHandler(T view, AnimHandlerGroup group) {
        for (AnimHandler handler : group.getHandlers()) {
            animHandlers.add(new Pair<>(view, handler));
        }
    }

    public void unregisterAnimHandler(View view) {
        Stream.of(animHandlers).filter(pair -> pair.first.equals(view)).forEach(pair -> animHandlers.remove(pair));
    }

    public void clearAnimHandler() {
        animHandlers.clear();
    }

    public void open() {
        gestureManager.startInterpolatorAnim(0, 1);
    }

    public void close() {
        gestureManager.startInterpolatorAnim(1, 0);
    }

    public void toggle() {
        if (isOpen())
            close();
        else
            open();
    }

    public boolean isOpen() {
        return gestureManager.currentRatio() >= 1f;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public float getRatio() {
        return ratio;
    }

    public float getProgress() {
        return progress;
    }

    public Properties getProperties() {
        return properties;
    }

    /*
     * =============================================================================================================
     * Declaring Classes and Instances
     *
     * */

    private OnRatioListener ratioListener = (ratio) -> {
        this.ratio = ratio;
        Stream.of(animHandlers).forEach(pair -> pair.second.syncingToRatio(this, properties.orientation, pair.first, ratio));
        syncingTouchPan(ratio);
        syncingContainer(ratio);
        invalidate();
    };

    private OnProgressListener progressListener = (max, progress) -> {
        this.progress = progress;
    };

    private class GestureAdapter implements GestureManager.GestureAdapter {

        @Override public float getMaxProgress() {
            return properties.absoluteSize;
        }

        @Override public boolean handleDown(float x, float y) {
            if (isOpen()) {
                return true;
            } else {
                return touchPanRect.contains(x, y);
            }
        }

        @Override public boolean handleSingleTapUp(float x, float y) {
            if (isOpen())
                close();
            return false;
        }

        @Override public float handleScroll(float currentProgress, float moveX, float moveY) {
            switch (properties.orientation) {
                case ORIENTATION_LEFT:
                    currentProgress -= moveX;
                    break;
                case ORIENTATION_TOP:
                    currentProgress -= moveY;
                    break;
                case ORIENTATION_RIGHT:
                    currentProgress += moveX;
                    break;
                case ORIENTATION_BOTTOM:
                    currentProgress += moveY;
                    break;
            }
            progress = currentProgress;
            return currentProgress;
        }

        @Override public float getAutoAnimThreshold() {
            return properties.autoAnimThreshold;
        }

        @Override public Interpolator getAnimInterpolator() {
            return interpolator;
        }
    }
}
