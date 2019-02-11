package com.example.propertyanimation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class PulseAnimationView extends View {
    private static final int COLOR_ADJUSTER = 5;
    private static final int ANIMATION_DURATION = 4000;
    private static final int ANIMATION_DELAY = 1000;
    private final Paint mPaint = new Paint();
    private float mRadius;
    private float mX;
    private float mY;
    private AnimatorSet mPulseAnimatorSet = new AnimatorSet();

    public PulseAnimationView(Context context) {
        super(context);
    }

    public PulseAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mX = event.getX();
            mY = event.getY();
            if (mPulseAnimatorSet != null && mPulseAnimatorSet.isRunning()) {
                mPulseAnimatorSet.cancel();
            }
            mPulseAnimatorSet.start();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ObjectAnimator growAnimator = ObjectAnimator.ofFloat(this,
                "radius", 0, getWidth());
        growAnimator.setDuration(ANIMATION_DURATION);
        growAnimator.setInterpolator(new LinearInterpolator());
        ObjectAnimator shrinkAnimator = ObjectAnimator.ofFloat(this,
                "radius", getWidth(), 0);
        shrinkAnimator.setDuration(ANIMATION_DURATION);
        shrinkAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        shrinkAnimator.setStartDelay(ANIMATION_DELAY);
        ObjectAnimator repeatAnimator = ObjectAnimator.ofFloat(this,
                "radius", 0, getWidth());
        repeatAnimator.setStartDelay(ANIMATION_DELAY);
        repeatAnimator.setDuration(ANIMATION_DURATION);
        repeatAnimator.setRepeatCount(1);
        repeatAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mPulseAnimatorSet.play(growAnimator).before(shrinkAnimator);
        mPulseAnimatorSet.play(repeatAnimator).after(shrinkAnimator);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mX, mY, mRadius, mPaint);
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
        mPaint.setColor(Color.GREEN + (int) radius / COLOR_ADJUSTER);
        invalidate();
    }
}
