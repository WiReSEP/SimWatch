package de.tu_bs.wire.simwatch.ui.listeners;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Created by mw on 23.05.16.
 */
public class OpenFileListener implements View.OnLongClickListener {
    private static final String TAG = "OpenFileListener";
    private final Activity context;
    private final File file;
    private String mimeType;

    public OpenFileListener(Activity context, File file, String mimeType) {
        this.context = context;
        this.file = file;
        this.mimeType = mimeType;
    }

    @Override
    public boolean onLongClick(View view) {
        new Thread() {
            public void run() {
                final File copyDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                //todo copy file to appropriate publicly visible directory
                File targetFile = new File(copyDirectory, file.getName());
                try {
                    Files.copy(file, targetFile);

                } catch (IOException e) {
                    Log.e(TAG, "Couldn't copy file", e);
                    return;
                }
                openFile(copyDirectory, targetFile);
            }
        }.start();

        return true;
    }

    private void openFile(final File copyDirectory, File targetFile) {
        Log.d(TAG, "Mime type: " + mimeType);
        if (mimeType == null) {
            mimeType = "*/*";
        }
        Log.d(TAG, "Mime type: " + mimeType);
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(targetFile), mimeType);
        context.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Couldn't open file", e);
                    openDirectory(copyDirectory);
                }
            }
        });
    }

    private void openDirectory(File directory) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(directory), "resource/folder");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e1) {
            Log.e(TAG, "Couldn't open file directory", e1);
        }
    }
}
