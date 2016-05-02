package de.tu_bs.wire.simwatch.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collection;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.simulation.InstanceAcquisitionListener;
import de.tu_bs.wire.simwatch.simulation.SimulationManager;
import de.tu_bs.wire.simwatch.simulation.UpdateListener;
import de.tu_bs.wire.simwatch.simulation.ViewMemory;
import de.tu_bs.wire.simwatch.simulation.profile.ProfileManager;
import de.tu_bs.wire.simwatch.ui.MenuCreator;

public class MainActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener, UpdateListener, InstanceAcquisitionListener, SimulationFragment.OnSnapshotSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String CURRENT_SIMULATION = "active_instance";
    private static final String CURRENT_SNAPSHOT = "current_snapshot";
    private SimulationManager simulationManager;
    private Instance currentSimulation;
    private int currentSnapshotIndex = -1;
    private MenuCreator menuCreator;
    private ProfileManager profileManager;
    private Toast mostRecentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection ConstantConditions
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //noinspection ConstantConditions
        navigationView.setNavigationItemSelectedListener(this);

        profileManager = new ProfileManager(this);
        try {
            setupSimulationManager();
        } catch (IOException e) {
            Log.e(TAG, "Could not open simulation manager", e);
        }

        if (savedInstanceState != null) {
            String currentSimulationID = savedInstanceState.getString(CURRENT_SIMULATION);
            if (currentSimulationID != null) {
                int snapshotIndex = savedInstanceState.getInt(CURRENT_SNAPSHOT);
                setCurrentSimulation(simulationManager.getInstance(currentSimulationID), snapshotIndex);
            }
        }

        buildSimulationMenu(navigationView.getMenu());

        //set onClick listeners
        ImageView updateImgMain = (ImageView) findViewById(R.id.updateImgMain);
        //noinspection ConstantConditions
        updateImgMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSimulations();
            }
        });
        View headerLayout = navigationView.getHeaderView(0);
        ImageView updateImgNav = (ImageView) headerLayout.findViewById(R.id.updateImgNav);
        updateImgNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSimulations();
            }
        });
        simulationManager.autoUpdate();
    }

    private void buildSimulationMenu(final Menu menu) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                menuCreator = new MenuCreator();
                menuCreator.populateSimulationMenu(menu, simulationManager.getInstances(), simulationManager.getViewMemory());
            }
        });
    }

    private void setupSimulationManager() throws IOException {
        simulationManager = new SimulationManager(this, profileManager);
        simulationManager.addInstanceAcquisitionListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //noinspection ConstantConditions
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(settingIntent);
                return true;
            case R.id.action_update:
                updateSimulations();
                return true;
            case R.id.action_delete:
                openDeleteDialog();
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDeleteDialog() {
        if (currentSimulation != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage(String.format(getString(R.string.rly_delete_message), currentSimulation.getName()));
            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "Ordered to delete simulation");
                    deleteCurrentSimulation();
                }
            });
            dialogBuilder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //literally nothing
                }
            });
            dialogBuilder.create().show();
        }
    }

    private void deleteCurrentSimulation() {
        if (currentSimulation != null) {
            simulationManager.deleteInstance(currentSimulation.getID());
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        if (menuCreator.hasItem(item)) {
            setCurrentSimulation(menuCreator.getInstanceChosen(item));
        } else {
            return false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //noinspection ConstantConditions
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setCurrentSimulation(Instance sim) {
        setCurrentSimulation(sim, -1);
    }

    private void setCurrentSimulation(Instance sim, int snapshotIndex) {
        if (sim != currentSimulation || snapshotIndex != currentSnapshotIndex) {
            if (sim != currentSimulation) {
                currentSimulation = sim;
                invalidateOptionsMenu();
            }
            currentSnapshotIndex = snapshotIndex;
            simulationManager.removeUpdateListener(this);
            if (currentSimulation != null) {
                Log.d(TAG, new Gson().toJson(currentSimulation));
                simulationManager.addUpdateListener(sim.getID(), this);
                drawSimulation();
            }
        }
    }

    public void updateSimulations() {
        Log.d(TAG, "Ordered to update");
        makeToast(getString(R.string.updating), Toast.LENGTH_LONG);
        simulationManager.updateAllInstances();
    }

    private void makeToast(final String text, final int duration) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (mostRecentToast != null && mostRecentToast.getView().getWindowVisibility() ==
                        View.VISIBLE) {
                    mostRecentToast.cancel();
                }
                mostRecentToast = Toast.makeText(MainActivity.this, text, duration);
                mostRecentToast.show();
            }
        });
    }

    public void drawSimulation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentSimulation != null) {
                    Profile profile = profileManager.getProfile(currentSimulation.getProfileID());
                    if (profile == null) {
                        Log.d(TAG, "Profile of current simulation is null");
                    } else {
                        // Create a new fragment
                        Fragment fragment;
                        if (currentSnapshotIndex < 0) {
                            fragment = SimulationFragment.newInstance(profile, currentSimulation);
                        } else {
                            fragment = SimulationFragment.newInstance(profile, currentSimulation, currentSnapshotIndex);
                        }

                        // Insert the fragment by replacing any existing fragment
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.snapshot_content,
                                fragment).commit();

                    }
                }
            }
        });
        ViewMemory viewMemory = simulationManager.getViewMemory();
        if (currentSimulation != null && viewMemory.viewInstance(currentSimulation, currentSnapshotIndex) > 0) {
            //noinspection ConstantConditions
            buildSimulationMenu(((NavigationView) findViewById(R.id.nav_view)).getMenu());
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentSimulation == null) {
            menu.removeItem(R.id.action_delete);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onUpdate(Collection<String> instanceIDs) {
        makeToast(getString(R.string.update_complete), Toast.LENGTH_SHORT);
        drawSimulation();
    }

    @Override
    public void onNoUpdates() {
        makeToast(getString(R.string.no_new_updates), Toast.LENGTH_SHORT);
    }

    @Override
    public void onUpdateFailed(Collection<String> instanceIDs) {
        if (currentSimulation != null && instanceIDs.contains(currentSimulation.getID())) {
            makeToast(getString(R.string.updating_failed), Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentSimulation != null) {
            outState.putString(CURRENT_SIMULATION, currentSimulation.getID());
            outState.putInt(CURRENT_SNAPSHOT, currentSnapshotIndex);
        }
    }

    @Override
    public void onInstanceListAcquired(Collection<String> instanceIDs) {
        //noinspection ConstantConditions
        buildSimulationMenu(((NavigationView) findViewById(R.id.nav_view)).getMenu());
        if (currentSimulation == null) {
            if (simulationManager.getInstanceList().size() == simulationManager.getInstances().size()) {
                makeToast(getString(R.string.update_complete), Toast.LENGTH_SHORT);
            }
        } else if (!instanceIDs.contains(currentSimulation.getID())) {
            setCurrentSimulation(null, -1);
        }
    }

    @Override
    public void onInstanceListAcquisitionFailed() {
        if (currentSimulation == null) {
            makeToast(getString(R.string.updating_failed), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onInstanceAcquired(Instance sim) {
        //noinspection ConstantConditions
        buildSimulationMenu(((NavigationView) findViewById(R.id.nav_view)).getMenu());
        if (currentSimulation == null) {
            if (simulationManager.getInstanceList().size() == simulationManager.getInstances().size()) {
                makeToast(getString(R.string.update_complete), Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onInstanceDeleted(String id) {

    }

    @Override
    public void onSnapshotSelected(int i) {
        Log.d(TAG, "Snapshot " + i + " selected");
        setCurrentSimulation(currentSimulation, i);
    }

    @Override
    public void onNoSnapshotSelected() {
        Log.d(TAG, "Newest snapshot selected");
        setCurrentSimulation(currentSimulation);
    }
}
