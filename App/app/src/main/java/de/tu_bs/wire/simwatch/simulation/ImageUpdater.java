package de.tu_bs.wire.simwatch.simulation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by mw on 22.04.16.
 */
public class ImageUpdater implements AttachmentDownloadListener {

    private final ImageView imageView;
    private Activity context;

    public ImageUpdater(Activity context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    @Override
    public void onDownloaded(File file) {
        final Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (imageBitmap != null) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(imageBitmap);
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
