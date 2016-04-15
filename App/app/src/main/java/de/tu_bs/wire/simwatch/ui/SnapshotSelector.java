package de.tu_bs.wire.simwatch.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.ui.activities.SimulationFragment;

/**
 * Created by mw on 20.03.16.
 */
public class SnapshotSelector implements AdapterView.OnItemSelectedListener {

    public static final String PLOTLESS_DELIMITER = "–";
    private static final String TAG = "SnapshotSelector";
    private Context context;
    private Instance instance;
    private Profile profile;
    private SimulationFragment.OnSnapshotSelectedListener listener;
    private int noneItemPosition;
    private int snapshotItemStartingPosition;
    private int numberOfItems;

    public SnapshotSelector(Context context, Instance instance, Profile profile, SimulationFragment.OnSnapshotSelectedListener listener) {
        this.context = context;
        this.instance = instance;
        this.profile = profile;
        this.listener = listener;
    }

    public void populateAdapter(ArrayAdapter<CharSequence> adapter) {
        int posCounter = 0;
        adapter.add(context.getString(R.string.newest_snapshot));
        noneItemPosition = posCounter++;
        snapshotItemStartingPosition = posCounter;

        if (profile.hasPlotReference()) {
            populateWithPlotReference(adapter);
        } else {
            populateWithSnapshotIndex(adapter);
        }
        numberOfItems = adapter.getCount();
        Log.d(TAG, "Number of items: " + numberOfItems);
    }

    private void populateWithSnapshotIndex(ArrayAdapter<CharSequence> adapter) {
        for (int i = 0; getSnapshotIndex(i) != -1; i++) {
            int snapshotIndex = getSnapshotIndex(i);
            adapter.add(String.format(Locale.getDefault(), context.getString(R.string.update_nr_d), snapshotIndex));
        }
    }

    private void populateWithPlotReference(ArrayAdapter<CharSequence> adapter) {
        String referenceKey = profile.getPlotReferenceKey();

        List<Update> updates = instance.getUpdates();
        for (int i = 0; getSnapshotIndex(i) > 0; i++) {
            int snapshotIndex = getSnapshotIndex(i);
            JsonObject updateData = updates.get(snapshotIndex).getData();

            if (updateData.has(referenceKey)) {
                Double value = updateData.get(referenceKey).getAsDouble();
                if (value.intValue() == value) {
                    adapter.add(Integer.toString(value.intValue()));
                } else {
                    adapter.add(value.toString());
                }
            } else {
                adapter.add(PLOTLESS_DELIMITER);
            }
        }
    }

    /**
     * For better selection, Snapshots of an Instance may not be ordered the same way inside the
     * Spinner. This method determines the snapshot index inside the Instance if given a snapshot
     * index inside the Spinner, starting with 0
     *
     * @param spinnerIndex the index of the Snapshot inside the Spinner
     * @return the index of the Snapshot inside the Instance or -1 if the spinner index corresponds
     * to no snapshot index
     */
    public int getSnapshotIndex(int spinnerIndex) {
        int result = instance.getNumberOfUpdates() - 1 - spinnerIndex;
        if (result > 0) {
            return result;
        } else {
            //snapshot index 0 will not be provided by the spinner, because snapshot 0 has no data
            return -1;
        }
    }

    /**
     * For better selection, Snapshots of an Instance may not be ordered the same way inside the
     * Spinner. This method determines the snapshot index inside the Spinner, starting with 0, if
     * given a snapshot index inside the Instance
     *
     * @param snapshotIndex the index of the Snapshot inside the Spinner
     * @return the index of the Snapshot inside the Instance or -1 if the spinner index corresponds
     * to no snapshot index
     */
    public int getSpinnerIndex(int snapshotIndex) {
        int spinnerIndices = instance.getNumberOfUpdates() - 1;
        int highestSpinnerIndex = spinnerIndices - 1;
        //snapshot index 0 will not be provided by the spinner, because snapshot 0 has no data
        int effectiveSnapshotIndex = snapshotIndex - 1;
        int result = highestSpinnerIndex - effectiveSnapshotIndex;
        if (result >= 0) {
            return result;
        } else {
            return -1;
        }
    }

    public void setPreselection(Spinner spinner, int i) {
        int preselectionPosition = getSpinnerIndex(i) + snapshotItemStartingPosition;
        if (preselectionPosition < numberOfItems) {
            spinner.setSelection(preselectionPosition);
        } else {
            spinner.setSelection(noneItemPosition);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "item selected. position " + position + ", id " + id);
        if (position == noneItemPosition) {
            listener.onNoSnapshotSelected();
        } else if (position >= snapshotItemStartingPosition) {
            //start with 0th update directly. The snapshot before the 0th update cannot be selected
            int spinnerIndex = position - snapshotItemStartingPosition;
            listener.onSnapshotSelected(getSnapshotIndex(spinnerIndex));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        listener.onNoSnapshotSelected();
    }
}
