package de.tu_bs.wire.simwatch.simulation;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by mw on 22.03.16.
 */
public class FileViewMemoryStorage extends ViewMemoryStorage {

    public static final String VIEW_MEMORY_DIR_NAME = "viewMemory";
    public static final String VIEW_MEMORY_FILE_NAME = "memory";
    private static final String TAG = "FileViewMemoryStorage";
    private File viewMemoryDir;

    public FileViewMemoryStorage(Context context) {

        viewMemoryDir = new File(context.getFilesDir(), VIEW_MEMORY_DIR_NAME);
        boolean dirExists = viewMemoryDir.mkdirs();
        if (dirExists && !viewMemoryDir.isDirectory()) {
            Log.e(TAG, "Simulation directory already exists as a non-directory");
        }

    }

    @Override
    public Map<String, Integer> readStorage() {
        File file = new File(viewMemoryDir, VIEW_MEMORY_FILE_NAME);
        if (file.exists() && file.canRead()) {
            try {
                Scanner scanner = new Scanner(file);
                scanner.useDelimiter("\\A");
                String str = scanner.next();
                scanner.close();
                Type type = new TypeToken<HashMap<String, Integer>>() {
                }.getType();
                try {
                    return new Gson().fromJson(str, type);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Broken syntax in view memory file. Creating empty view memory", e);
                    return new HashMap<>();
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Couldn't find view memory file, although it exists", e);
                return new HashMap<>();
            }
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public void writeStorage(Map<String, Integer> storage) {
        if (storage != null) {
            File file = new File(viewMemoryDir, VIEW_MEMORY_FILE_NAME);
            try {
                if (file.exists() || file.createNewFile()) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write(new Gson().toJson(storage));
                    writer.flush();
                    writer.close();
                } else {
                    throw new IOException("Cannot create file " + file.getName());
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't write storage to file", e);
            }
        }
    }
}
