package de.tu_bs.wire.simwatch.simulation;

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
import de.tu_bs.wire.simwatch.api.models.Instance;

/**
 * Implementation of the SimulationStorage Interface using files in the private app directory to
 * store simulation Instances locally
 */
public class FileSimulationStorage implements SimulationStorage {

    public static final String SIMULATION_DIR_NAME = "simulations";
    private static final String TAG = "FileSimulationStorage";
    private File simulationDir;

    public FileSimulationStorage(Context context) {

        simulationDir = new File(context.getFilesDir(), SIMULATION_DIR_NAME);
        boolean dirExists = simulationDir.mkdirs();
        if (dirExists && !simulationDir.isDirectory()) {
            Log.e(TAG, "Simulation directory already exists as a non-directory");
        }

    }

    @Override
    public void writeInstance(Instance sim) {
        if (sim != null) {
            File file = new File(simulationDir, id2fileName(sim.getID()));
            try {
                if (file.exists() || file.createNewFile()) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write(GsonUtil.getGson().toJson(sim));
                    writer.flush();
                    writer.close();
                } else {
                    throw new IOException("Cannot create file " + file.getName());
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't write Instance to file", e);
            }
        }
    }

    @Override
    public Instance readInstance(String id) {
        File file = new File(simulationDir, id2fileName(id));
        try {
            return readFromFile(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public Collection<Instance> readAllInstances() {
        Collection<Instance> instances = new ArrayList<>();
        File simulationList[] = simulationDir.listFiles();
        for (File file : simulationList) {
            try {
                Instance newSim = readFromFile(file);
                if (newSim == null) {
                    Log.e(TAG, "Instance read from file " + file.getName() + " is null");
                } else {
                    instances.add(newSim);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File " + file.getName() + " not found, even though it was retrieved by File.listFiles()");
            }
        }
        return instances;
    }

    @Override
    public boolean deleteInstance(String id) {
        File file = new File(simulationDir, id2fileName(id));
        return file.exists() && file.canWrite() && file.delete();
    }

    private String id2fileName(String id) {
        return id;
    }

    private Instance readFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\A");
        String str = scanner.next();
        scanner.close();
        Log.d(TAG, "Read instance. JSON: " + str);
        return Instance.fromString(str);
    }

}
