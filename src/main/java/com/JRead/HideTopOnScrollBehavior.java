package com.JRead;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

/**
 * The {@link Behavior} for a View within a {@link CoordinatorLayout} to hide the view off the
 * bottom of the screen when scrolling down, and show it when scrolling up.
 * 滚动时隐藏顶部
 */
public class HideTopOnScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    protected static final int ENTER_ANIMATION_DURATION = 225;
    protected static final int EXIT_ANIMATION_DURATION = 175;

    private static final int STATE_SCROLLED_DOWN = 1;
    private static final int STATE_SCROLLED_UP = 2;

    private int height = 0;
    private int currentState = STATE_SCROLLED_UP;
    private int additionalHiddenOffsetY = 0;
    @Nullable private ViewPropertyAnimator currentAnimator;

    public HideTopOnScrollBehavior() {}

    public HideTopOnScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(
            @NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
        ViewGroup.MarginLayoutParams paramsCompat =
                (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        // bottom 改 top, 再减 500. 玄学生效。
        height = child.getMeasuredHeight() + paramsCompat.topMargin - 500;
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    /**
     * Sets an additional offset for the y position used to hide the view.
     *
     * @param child the child view that is hidden by this behavior
     * @param offset the additional offset in pixels that should be added when the view slides away
     */
    public void setAdditionalHiddenOffsetY(@NonNull V child, @Dimension int offset) {
        additionalHiddenOffsetY = offset;

        if (currentState == STATE_SCROLLED_DOWN) {
            child.setTranslationY(height + additionalHiddenOffsetY);
        }
    }

    @Override
    public boolean onStartNestedScroll(
            @NonNull CoordinatorLayout coordinatorLayout,
            @NonNull V child,
            @NonNull View directTargetChild,
            @NonNull View target,
            int nestedScrollAxes,
            int type) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(
            @NonNull CoordinatorLayout coordinatorLayout,
            @NonNull V child,
            @NonNull View target,
            int dxConsumed,
            int dyConsumed,
            int dxUnconsumed,
            int dyUnconsumed,
            int type,
            @NonNull int[] consumed) {
        if (dyConsumed > 0) {
            slideDown(child);
        } else if (dyConsumed < 0) {
            slideUp(child);
        }
    }

    /**
     * Perform an animation that will slide the child from it's current position to be totally on the
     * screen.
     */
    public void slideUp(@NonNull V child) {
        if (currentState == STATE_SCROLLED_UP) {
            return;
        }

        if (currentAnimator != null) {
            currentAnimator.cancel();
            child.clearAnimation();
        }
        currentState = STATE_SCROLLED_UP;
        animateChildTo(
                child, 0, ENTER_ANIMATION_DURATION, LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    }

    /**
     * Perform an animation that will slide the child from it's current position to be totally off the
     * screen.
     */
    public void slideDown(@NonNull V child) {
        if (currentState == STATE_SCROLLED_DOWN) {
            return;
        }

        if (currentAnimator != null) {
            currentAnimator.cancel();
            child.clearAnimation();
        }
        currentState = STATE_SCROLLED_DOWN;
        animateChildTo(
                child,
                height + additionalHiddenOffsetY,
                EXIT_ANIMATION_DURATION,
                FAST_OUT_LINEAR_IN_INTERPOLATOR);
    }

    private void animateChildTo(
            @NonNull V child, int targetY, long duration, TimeInterpolator interpolator) {
        currentAnimator =
                child
                        .animate()
                        .translationY(targetY)
                        .setInterpolator(interpolator)
                        .setDuration(duration)
                        .setListener(
                                new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        currentAnimator = null;
                                    }
                                });
    }

    public static final TimeInterpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR =
            new LinearOutSlowInInterpolator();
    public static final TimeInterpolator FAST_OUT_LINEAR_IN_INTERPOLATOR =
            new FastOutLinearInInterpolator();
}