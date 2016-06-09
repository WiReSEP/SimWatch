package de.tu_bs.wire.simwatch.ui.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;

import java.io.File;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.ui.listeners.ZoomableImageTouchListener;
import de.tu_bs.wire.simwatch.ui.views.ZoomableImageView;

public class ZoomActivity extends AppCompatActivity {

    public static final String FILE_STR = "file";
    private static final String TAG = "ZoomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ZoomableImageView imageView = (ZoomableImageView) findViewById(R.id.zoomableImage);
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        File imageFile = (File) getIntent().getSerializableExtra(FILE_STR);
        Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        Point imageSize = new Point(imageBitmap.getWidth(), imageBitmap.getHeight());
        double scale = Math.max(Math.min((double) displaySize.x / imageSize.x, (double) displaySize.y / imageSize.y), 1);
        Bitmap displayBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (imageSize.x * scale), (int) (imageSize.y * scale), true);
        //noinspection ConstantConditions
        imageView.setImageBitmap(displayBitmap);
        int width = displayBitmap.getWidth();
        int height = displayBitmap.getHeight();
        float scaleMin = Math.min((float) displaySize.x / width, (float) displaySize.y / height);

        ZoomableImageTouchListener touchListener = new ZoomableImageTouchListener(width, height, scaleMin, displaySize.x, displaySize.y);
        imageView.setOnTouchListener(touchListener);
        touchListener.center(imageView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "unhandled Touch event: " + event.getAction());
        return super.onTouchEvent(event);
    }
}
