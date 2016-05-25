package de.tu_bs.wire.simwatch.ui.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.io.File;

import de.tu_bs.wire.simwatch.ui.activities.ZoomActivity;

/**
 * Created by mw on 17.05.16.
 */
public class ImageClickListener implements View.OnClickListener {
    private File file;
    private Context context;

    public ImageClickListener(Context context, File file) {
        this.context = context;
        this.file = file;
    }

    @Override
    public void onClick(View view) {
        Intent activityIntent = new Intent(context, ZoomActivity.class);
        activityIntent.putExtra(ZoomActivity.FILE_STR, file);
        context.startActivity(activityIntent);
    }
}
