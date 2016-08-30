package de.tu_bs.wire.simwatch.ui.listeners;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import de.tu_bs.wire.simwatch.Vec2f;
import de.tu_bs.wire.simwatch.ui.ImageScaleAnimation;
import de.tu_bs.wire.simwatch.ui.views.ZoomableImageView;

/**
 * Created by mw on 13.05.16.
 */
public class ZoomableImageTouchListener implements View.OnTouchListener {

    private static final String TAG = "ZoomTouchListener";
    private static final float MIN_SPACING = 5.f;
    private static final int X_POS = 2;
    private static final int Y_POS = 5;
    private static final int SCALE = 0;
    private TouchMode touchMode = TouchMode.NONE;
    private Vec2f startingPosition = new Vec2f();
    private float startSpacing = 1;
    private Matrix startingMatrix;
    private Matrix movedMatrix;
    private int width;
    private int height;
    private float minX;
    private float minY;
    private float minScale;
    private float maxX;
    private float maxY;

    public ZoomableImageTouchListener(int width, int height, float maxX, float maxY) {
        this(width, height, 1, maxX, maxY);
    }

    public ZoomableImageTouchListener(int width, int height, float minScale, float maxX, float maxY) {
        this(width, height, minScale, 0, 0, maxX, maxY);
    }

    public ZoomableImageTouchListener(int width, int height, float minScale, float minX, float minY, float maxX, float maxY) {
        this.width = width;
        this.height = height;
        this.minX = minX;
        this.minY = minY;
        this.minScale = minScale;
        this.maxX = maxX;
        this.maxY = maxY;
        startingMatrix = new Matrix();
        startingMatrix.postScale(this.minScale, this.minScale);
        startingMatrix.postTranslate(this.minX, this.minY);
        movedMatrix = startingMatrix;
    }

    public void center(ZoomableImageView imageView) {
        startingMatrix = new Matrix();
        startingMatrix.postScale(this.minScale, this.minScale);
        startingMatrix.postTranslate((this.minX + maxX - width * minScale) / 2, (this.minY + maxY - height * minScale) / 2);
        movedMatrix = startingMatrix;
        imageView.setImageMatrix(movedMatrix);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (!(view instanceof ZoomableImageView)) {
            Log.e(TAG, String.format("Listener called for view of type %s, must be ZoomableImageView", view.getClass().getName()));
            return false;
        }

        view.bringToFront();

        ZoomableImageView imageView = (ZoomableImageView) view;

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: //one finger down
            {
                imageView.clearAnimation();

                startingMatrix = movedMatrix;
                startingPosition = getFirstFinger(motionEvent);
                touchMode = TouchMode.ONE_FINGER;
            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN: //second finger down
            {
                if (motionEvent.getPointerCount() <= 2) {
                    startingMatrix = movedMatrix;
                    startingPosition = getFingerCenter(motionEvent);
                    float spacing = getFingerSpacing(motionEvent);
                    startSpacing = Math.max(spacing, MIN_SPACING);
                    touchMode = TouchMode.TWO_FINGERS;
                }
            }
            break;
            case MotionEvent.ACTION_UP: //one finger up
            {
                rescaleToPretty(imageView);
                startingMatrix = movedMatrix;
                touchMode = TouchMode.NONE;
            }
            break;
            case MotionEvent.ACTION_POINTER_UP: //second finger up
            {
                if (motionEvent.getPointerCount() <= 2) {
                    startingMatrix = movedMatrix;
                    startingPosition = getUnchangedFinger(motionEvent);
                    touchMode = TouchMode.ONE_FINGER;
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: //finger(s) moved
                switch (touchMode) {
                    case ONE_FINGER: {
                        Vec2f motion = getFirstFinger(motionEvent).minus(startingPosition);
                        movedMatrix = new Matrix(startingMatrix);
                        movedMatrix.postTranslate(motion.x(), motion.y());
                        imageView.setImageMatrix(movedMatrix);
                        imageView.invalidate();
                        imageView.requestLayout();
                    }
                    break;
                    case TWO_FINGERS: {
                        try {
                            Vec2f motion = getFingerCenter(motionEvent).minus(startingPosition);
                            Vec2f newPosition = startingPosition.plus(motion);
                            float spacing = getFingerSpacing(motionEvent);
                            float scale = Math.max(spacing, MIN_SPACING) / startSpacing;
                            movedMatrix = new Matrix(startingMatrix);
                            movedMatrix.postTranslate(motion.x(), motion.y());
                            movedMatrix.postScale(scale, scale, newPosition.x(), newPosition.y());
                            imageView.setImageMatrix(movedMatrix);
                            imageView.invalidate();
                            imageView.requestLayout();
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "Moved with two fingers, but there was only one finger pointer");
                        }
                    }
                    break;
                    case NONE:
                        break;
                    default:
                }
                break;
            default:
                Log.d(TAG, "Unknown touch event: " + motionEvent.getAction());
        }

        return true;
    }

    private void rescaleToPretty(ZoomableImageView imageView) {
        float values[] = new float[9];
        movedMatrix.getValues(values);
        float scale = values[SCALE];
        if (scale < minScale && scale > 0) {
            float rescale = minScale / scale;
            movedMatrix.postScale(rescale, rescale, values[X_POS], values[Y_POS]);
            float scaleDiff = (1 - rescale) * scale;
            movedMatrix.postTranslate(scaleDiff * width / 2, scaleDiff * height / 2);
            movedMatrix.getValues(values);
        }
        float leftX = values[X_POS];
        float leftY = values[Y_POS];
        float rightX = leftX + values[SCALE] * width;
        float rightY = leftY + values[SCALE] * height;
        if (leftX > minX && rightX > maxX) {
            float shift = Math.max(minX - leftX, maxX - rightX);
            movedMatrix.postTranslate(shift, 0);
        } else if (rightX < maxX && leftX < minX) {
            float shift = Math.min(maxX - rightX, minX - leftX);
            movedMatrix.postTranslate(shift, 0);
        }
        if (leftY > minY && rightY > maxY) {
            float shift = Math.max(minY - leftY, maxY - rightY);
            movedMatrix.postTranslate(0, shift);
        } else if (rightY < maxY && leftY < minY) {
            float shift = Math.min(maxY - rightY, minY - leftY);
            movedMatrix.postTranslate(0, shift);
        }
        Animation animation = new ImageScaleAnimation(imageView, movedMatrix);
        animation.setDuration(300);
        imageView.startAnimation(animation);
    }

    /**
     * Gives the distance between two fingers of the motion event. Undefined behaviour if any number
     * other than two fingers are registered
     *
     * @param motionEvent The motion event in question
     * @return The distance in pixels
     */
    private float getFingerSpacing(MotionEvent motionEvent) {
        Vec2f fingerOffset = getFingerOffset(motionEvent);
        return fingerOffset.length();
    }

    /**
     * Gives the center position between two fingers of the motion event. Undefined behaviour if any
     * number other than two fingers are registered
     *
     * @param motionEvent The motion event in question
     * @return The unweighted euclidean center point of the two fingers
     */
    private Vec2f getFingerCenter(MotionEvent motionEvent) {
        return getFirstFinger(motionEvent).plus(getFingerOffset(motionEvent).times(.5f));
    }

    private Vec2f getFirstFinger(MotionEvent motionEvent) {
        return new Vec2f(motionEvent.getX(0), motionEvent.getY(0));
    }

    private Vec2f getSecondFinger(MotionEvent motionEvent) {
        return new Vec2f(motionEvent.getX(1), motionEvent.getY(1));
    }

    private Vec2f getUnchangedFinger(MotionEvent motionEvent) {
        if (motionEvent.getActionIndex() == 0) {
            return getSecondFinger(motionEvent);
        } else {
            return getFirstFinger(motionEvent);
        }
    }

    private Vec2f getFingerOffset(MotionEvent motionEvent) {
        float x;
        float y;
        try {
            x = motionEvent.getX(1) - motionEvent.getX(0);
            y = motionEvent.getY(1) - motionEvent.getY(0);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("There are less than two fingers touching", e);
        }
        return new Vec2f(x, y);
    }

    enum TouchMode {NONE, ONE_FINGER, TWO_FINGERS}
}
