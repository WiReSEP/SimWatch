package de.tu_bs.wire.simwatch.ui.listeners;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;

/**
 * Created by mw on 23.05.16.
 */
public class OpenFileListener implements View.OnLongClickListener {
    private static final String TAG = "OpenFileListener";
    private final Activity context;
    private final File file;

    public OpenFileListener(Activity context, File file) {
        this.context = context;
        this.file = file;
    }

    @Override
    public boolean onLongClick(View view) {
        new Thread() {
            public void run() {
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                final File copyDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                //todo copy file to appropriate publicly visible directory
                File targetFile = new File(copyDirectory, file.getName());
                try {
                    Files.copy(file, targetFile);

                } catch (IOException e) {
                    Log.e(TAG, "Couldn't copy file", e);
                    return;
                }
                openFile(mimeTypeMap, copyDirectory, targetFile);
            }
        }.start();

        return true;
    }

    private void openFile(MimeTypeMap mimeTypeMap, final File copyDirectory, File targetFile) {
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(getFileExtension(targetFile));
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

    private String getFileExtension(File file) {
        String type = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't open file for reading mime type", e);
        }
        try {
            type = URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading mime type", e);
        }
        if (type == null) {
            ContentResolver contentResolver = context.getContentResolver();
            type = contentResolver.getType(Uri.fromFile(file));
        }
        if (type == null) {
            type = Files.getFileExtension(file.getName());
        }
        return type;
    }
}
