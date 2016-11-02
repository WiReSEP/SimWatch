package de.tu_bs.wire.simwatch.ui.activities;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.GsonUtil;
import de.tu_bs.wire.simwatch.api.models.Instance;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.api.models.Snapshot;
import de.tu_bs.wire.simwatch.simulation.AttachmentManager;
import de.tu_bs.wire.simwatch.ui.SimulationPainter;
import de.tu_bs.wire.simwatch.ui.SnapshotSelector;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link SimulationHandlerActivity} interface to handle interaction events. Use the {@link
 * SimulationFragment#newInstance} factory method to create an instance of this fragment.
 */
public class SimulationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PROFILE_STR = "profile";
    private static final String INSTANCE_STR = "snapshot";
    private static final String SNAPSHOT_ID_STR = "snapshot_id";
    private static final String TAG = "SimulationFragment";

    // TODO: Rename and change types of parameters
    private Profile profile;
    private Instance instance;
    private Snapshot snapshot;
    private int snapshotID;

    public SimulationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @param profile  The Profile of the Instance to be drawn
     * @param instance The Snapshot of the Instance to be drawn
     * @return A new instance of fragment SimulationFragment.
     */
    public static SimulationFragment newInstance(Profile profile, Instance instance) {
        return newInstance(profile, instance, instance.getNumberOfUpdates());
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @param profile  The Profile of the Instance to be drawn
     * @param instance The Snapshot of the Instance to be drawn
     * @return A new instance of fragment SimulationFragment.
     */
    public static SimulationFragment newInstance(Profile profile, Instance instance, int snapshotID) {
        SimulationFragment fragment = new SimulationFragment();
        Bundle args = new Bundle();
        Gson gson = GsonUtil.getGson();
        args.putString(PROFILE_STR, gson.toJson(profile));
        args.putString(INSTANCE_STR, gson.toJson(instance));
        args.putString(SNAPSHOT_ID_STR, gson.toJson(snapshotID));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = GsonUtil.getGson();
            try {
                profile = gson.fromJson(getArguments().getString(PROFILE_STR), Profile.class);
                instance = gson.fromJson(getArguments().getString(INSTANCE_STR), Instance.class);
                snapshotID = gson.fromJson(getArguments().getString(SNAPSHOT_ID_STR), Integer.class);
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "Simulation argument has a broken syntax", e);
            }
            snapshot = instance.getSnapshot(snapshotID);
        } else {
            Log.e(TAG, "No arguments given.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_simulation, container, false);

        ViewGroup contentView = (ViewGroup) root;
        TextView name = (TextView) contentView.findViewById(R.id.simulation_name);
        Spinner snapshotSpinner = (Spinner) contentView.findViewById(R.id.snapshot_spinner);

        name.setText(snapshot.getName());

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                new ArrayList<CharSequence>());
        SnapshotSelector snapshotSelector = new SnapshotSelector(getActivity(), instance, profile, (SimulationHandlerActivity) getActivity());
        snapshotSelector.populateAdapter(adapter);
        snapshotSpinner.setAdapter(adapter);
        snapshotSelector.setPreselection(snapshotSpinner, snapshotID);
        snapshotSpinner.setOnItemSelectedListener(snapshotSelector);

        AttachmentManager attachmentManager = ((SimulationHandlerActivity) getActivity()).getAttachmentManager();
        new SimulationPainter(snapshot, getActivity(), profile, attachmentManager).draw(contentView);


        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof SimulationHandlerActivity)) {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface SimulationHandlerActivity {

        void onSnapshotSelected(int i);

        void onNoSnapshotSelected();

        AttachmentManager getAttachmentManager();
    }
}
