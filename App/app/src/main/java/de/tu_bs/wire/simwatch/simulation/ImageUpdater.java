package de.tu_bs.wire.simwatch.simulation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by mw on 22.04.16.
 */
public class ImageUpdater implements AttachmentDownloadListener {

    private static final String TAG = "ImageUpdater";
    private final ImageView imageView;
    private Activity context;

    public ImageUpdater(Activity context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    public void onDownloaded(File file) {
        Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (imageBitmap != null) {
            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();
            float maxWidth = imageView.getWidth();
            float maxHeight = imageView.getHeight();
            if (maxWidth == 0 || maxHeight == 0) {
                DisplayMetrics metrics = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                maxWidth = metrics.widthPixels;
                maxHeight = metrics.heightPixels;
            }
            Log.d(TAG, "maxWidth: " + maxWidth);
            Log.d(TAG, "maxHeight: " + maxHeight);
            float scale = Math.min(Math.min(maxWidth / width, maxHeight / height), 1);
            final Bitmap scaledBitmap;
            if (scale > 0) {
                scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (width * scale), (int) (height * scale), true);
            } else {
                scaledBitmap = imageBitmap;
            }
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(scaledBitmap);
                }
            });
        }
    }

    @Override
    public void onCouldNotDownload(File file) {
        //nothing
    }

    @Override
    public void onRetryLater(File file) {
        //nothing
    }

    public ImageView getImageView() {
        return imageView;
    }
}
