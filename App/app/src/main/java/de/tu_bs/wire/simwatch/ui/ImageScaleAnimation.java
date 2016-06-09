package de.tu_bs.wire.simwatch.ui;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

/**
 * Created by mw on 09.06.16.
 */
public class ImageScaleAnimation extends Animation {
    private static final String TAG = "ImageScaleAnimation";
    private float[] sourceScale = new float[9];
    private float[] targetScale = new float[9];
    private ImageView imageView;

    public ImageScaleAnimation(ImageView imageView, Matrix targetScale) {
        this.imageView = imageView;
        imageView.getImageMatrix().getValues(sourceScale);
        targetScale.getValues(this.targetScale);
        setDuration(1000);
    }

    @Override
    public void cancel() {
        super.cancel();
        Matrix targetMatrix = new Matrix();
        targetMatrix.setValues(targetScale);
        imageView.setImageMatrix(targetMatrix);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        Matrix m = imageView.getImageMatrix();
        float[] interpolatedMatrix = new float[9];
        for (int i = 0; i < 9; i++) {
            interpolatedMatrix[i] = sourceScale[i] + (targetScale[i] - sourceScale[i]) * interpolatedTime;
        }
        m.setValues(interpolatedMatrix);
        imageView.setImageMatrix(m);
        imageView.invalidate();
    }
}