package com.example.surfaceviewexample;

public class FlashlightCone {
    private int mX;
    private int mY;
    private int mRadius;

    public FlashlightCone(int viewWidth, int viewHeight) {
        this.mX = viewWidth / 2;
        this.mY = viewHeight / 2;
        // Adjust the radius for the narrowest view dimension.
        mRadius = ((viewWidth <= viewHeight) ? mX / 3 : mY / 3);
    }

    public void update(int newX, int newY) {
        mX = newX;
        mY = newY;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public int getRadius() {
        return mRadius;
    }
}
