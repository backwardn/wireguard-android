package com.wireguard.android.widget.fab;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transformation.ExpandableTransformationBehavior;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

@Keep
public class EmitExpandableTransformationBehavior extends ExpandableTransformationBehavior {

    private static final float EXPAND_SCALE_X = 0.4f;
    private static final float EXPAND_SCALE_Y = 0.4f;
    private static final long EXPAND_DELAY = 60L;
    private static final long EXPAND_DURATION = 150L;
    private static final long COLLAPSE_DELAY = 60L;
    private static final long COLLAPSE_DURATION = 150L;

    public EmitExpandableTransformationBehavior() {
        super(null, null);
    }

    public EmitExpandableTransformationBehavior(final Context context) {
        super(context, null);
    }

    public EmitExpandableTransformationBehavior(final Context context, final AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final View child, final View dependency) {
        return dependency instanceof FloatingActionButton && child instanceof ViewGroup;
    }

    @NonNull
    @Override
    protected AnimatorSet onCreateExpandedStateChangeAnimation(final View dependency, final View child, final boolean expanded, final boolean isAnimating) {
        if (!(child instanceof ViewGroup)) {
            return new AnimatorSet();
        }
        final Collection<Animator> animations = new ArrayList<>();
        if (expanded) {
            createExpandAnimation((ViewGroup) child, isAnimating, animations);
        } else {
            createCollapseAnimation((ViewGroup) child, animations);
        }
        final AnimatorSet set = new AnimatorSet();
        set.playTogether(animations);
        set.addListener(new MyAnimatorListener(expanded, child));
        return set;
    }

    private static void createExpandAnimation(
            final ViewGroup child,
            final boolean currentlyAnimating,
            final Collection<Animator> animations
    ) {
        if (!currentlyAnimating) {
            for (int i = 0; i < child.getChildCount(); i++) {
                final View view = child.getChildAt(i);
                view.setAlpha(0f);
                view.setScaleX(EXPAND_SCALE_X);
                view.setScaleY(EXPAND_SCALE_Y);
            }
        }
        long[] delays = new long[child.getChildCount()];
        for (int i = 0; i < child.getChildCount(); i++) {
            delays[i] = i * EXPAND_DELAY;
        }
        delays = reverse(delays, child.getChildCount());
        final PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f);
        final PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f);
        final PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat(View.ALPHA, 1f);
        final Collection<Animator> animators = new ArrayList<>();
        for (int i = 0; i < child.getChildCount(); i++) {
            final View view = child.getChildAt(i);
            final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                    view,
                    scaleXHolder,
                    scaleYHolder,
                    alphaHolder
            );
            animator.setDuration(EXPAND_DURATION);
            animator.setStartDelay(delays[i]);
            animators.add(animator);
        }
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animations.add(animatorSet);
    }

    private static void createCollapseAnimation(final ViewGroup child, final Collection<Animator> animations) {
        final long[] delays = new long[child.getChildCount()];
        for (int i = 0; i < child.getChildCount(); i++) {
            delays[i] = i * COLLAPSE_DELAY;
        }
        final PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.4f);
        final PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.4f);
        final PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat(View.ALPHA, 0f);
        final Collection<Animator> animators = new ArrayList<>();
        for (int i = 0; i < child.getChildCount(); i++) {
            final View view = child.getChildAt(i);
            final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                    view,
                    scaleXHolder,
                    scaleYHolder,
                    alphaHolder
            );
            animator.setDuration(COLLAPSE_DURATION);
            animator.setStartDelay(delays[i]);
            animators.add(animator);
        }
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animations.add(animatorSet);
    }

    private static long[] reverse(final long[] array, final int size) {
        final long[] returnArray = new long[size];
        int j = size;
        for (int i = 0; i < size; i++) {
            returnArray[j - 1] = array[i];
            j -= 1;
        }
        return returnArray;
    }

    private static final class MyAnimatorListener implements AnimatorListener {
        private final View child;
        private final boolean expanded;

        private MyAnimatorListener(final boolean expanded, final View child) {
            this.expanded = expanded;
            this.child = child;
        }

        @Override
        public void onAnimationStart(final Animator animation) {
            if (expanded) {
                child.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            if (!expanded) {
                child.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationCancel(final Animator animation) {

        }

        @Override
        public void onAnimationRepeat(final Animator animation) {

        }
    }
}
