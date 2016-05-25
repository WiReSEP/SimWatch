package de.tu_bs.wire.simwatch.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by mw on 13.05.16.
 */
public class ZoomableImageView extends ImageView {

    private static final String TAG = "ZoomableImageView";

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        super.setClickable(true);

        setScaleType(ScaleType.MATRIX);
    }

}
