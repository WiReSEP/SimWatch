package de.tu_bs.wire.simwatch.simulation.profile;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import de.tu_bs.wire.simwatch.api.GsonUtil;
import de.tu_bs.wire.simwatch.api.models.Profile;

/**
 * Implements a local storage for Profiles by saving them into files in the private app directory.
 * Each Profile has its own file
 */
public class FileProfileStorage implements ProfileStorage {

    public static final String PROFILE_DIR_NAME = "profiles";
    private static final String TAG = "FileProfileStorage";
    private File profileDir;

    public FileProfileStorage(Context context) {

        profileDir = new File(context.getFilesDir(), PROFILE_DIR_NAME);
        boolean dirExists = profileDir.mkdirs();
        if (dirExists && !profileDir.isDirectory()) {
            Log.e(TAG, "Profile directory already exists as a non-directory");
        }

    }

    @Override
    public void writeProfile(Profile profile) {
        if (profile == null) {
            throw new NullPointerException("profile is null");
        }
        File file = new File(profileDir, id2fileName(profile.getID()));
        try {
            if (file.exists() || file.createNewFile()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(GsonUtil.getGson().toJson(profile));
                writer.flush();
                writer.close();
            } else {
                throw new IOException("Cannot create file " + file.getName());
            }
        } catch (IOException e) {
            Log.e(TAG, "Couldn't write Instance to file", e);
        }
    }

    @Override
    public Profile readProfile(String id) {
        File file = new File(profileDir, id2fileName(id));
        try {
            return readFromFile(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public Collection<Profile> readAllProfiles() {
        Collection<Profile> instances = new ArrayList<>();
        File simulationList[] = profileDir.listFiles();
        for (File file : simulationList) {
            try {
                Profile newProfile = readFromFile(file);
                if (newProfile == null) {
                    Log.e(TAG, "Profile read from file " + file.getName() + " is null");
                } else {
                    instances.add(newProfile);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File " + file.getName() + " not found, even though it was retrieved by File.listFiles()");
            }
        }
        return instances;
    }

    @Override
    public boolean deleteProfile(String id) {
        File file = new File(profileDir, id2fileName(id));
        return file.exists() && file.canRead() && file.delete();
    }

    private String id2fileName(String id) {
        if (id == null) {
            throw new NullPointerException("profile id is null");
        }
        return id;
    }

    private Profile readFromFile(File file) throws FileNotFoundException {
        String str = new Scanner(file).useDelimiter("\\A").next();
        return Profile.fromString(str);
    }

}
