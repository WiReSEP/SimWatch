package de.tu_bs.wire.simwatch.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.JsonElement;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.api.models.Snapshot;
import de.tu_bs.wire.simwatch.api.models.Update;
import de.tu_bs.wire.simwatch.api.types.Types;

/**
 * Draws Snapshots of simulation Instances into Android layouts
 */
public class GraphPainter {

    private static final String TAG = "GraphPainter";
    final private Snapshot sim;
    private Profile profile;
    private List<Double> plotReference;
    private Map<String, List<Double>> plottables;
    private int lowerBound;
    private int upperBound;
    private Context context;

    public GraphPainter(Snapshot sim, Profile profile, Context context) {
        this(sim, profile, context, 0, sim.getNumberOfUpdates());
    }

    public GraphPainter(Snapshot sim, Profile profile, Context context, int updatesLowerBound, int updatesUpperBound) {
        this.sim = sim;
        this.profile = profile;
        this.context = context;
        lowerBound = updatesLowerBound;
        upperBound = updatesUpperBound;
        readPlotReference();
        readPlottables();
    }

    private void readPlotReference() {
        if (profile.hasPlotReference()) {
            String referenceKey = profile.getPlotReferenceKey();
            if (referenceKey != null) {
                plotReference = new ArrayList<>();
                List<Update> updates = sim.getUpdates();
                for (int i = lowerBound; i < updates.size() && i <= upperBound; i++) {
                    Update update = updates.get(i);
                    JsonElement referenceValue = update.getData().get(referenceKey);
                    if (referenceValue == null) {
                        plotReference.add(null);
                    } else {
                        plotReference.add(referenceValue.getAsDouble());
                    }
                }
            }
        } else {
            plotReference = new ArrayList<>();
            List<Update> updates = sim.getUpdates();
            for (int i = lowerBound; i < updates.size() && i <= upperBound; i++) {
                plotReference.add((double) i);
            }
        }
    }

    private void readPlottables() {
        plottables = new HashMap<>();
        for (Map.Entry<String, String> property : profile.getProperties().entrySet()) {
            if (Types.getType(property.getValue()) == Types.Type.PLOTTABLE) {
                readPlottable(property.getKey());
            }
        }
    }

    private void readPlottable(String key) {
        List<Double> plottable = new ArrayList<>();
        List<Update> updates = sim.getUpdates();
        for (int i = lowerBound; i < updates.size() && i <= upperBound; i++) {
            Update update = updates.get(i);
            JsonElement referenceValue = update.getData().get(key);
            if (referenceValue == null) {
                plottable.add(null);
            } else {
                plottable.add(referenceValue.getAsDouble());
            }
        }
        plottables.put(key, plottable);
    }

    public Collection<String> getPlottableKeys() {
        return plottables.keySet();
    }

    public View draw(String plottableKey) {
        Collection<String> keys = new ArrayList<>();
        keys.add(plottableKey);
        return draw(keys);
    }

    public View draw(Collection<String> plottableKeys) {
        Log.d(TAG, "Drawing graph. Plottable reference points: " + plotReference.size());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newLayout = inflater.inflate(R.layout.graph_layout, null);
        GraphView graph = (GraphView) newLayout.findViewById(R.id.graph);
        for (String key : plottableKeys) {
            if (plottables.containsKey(key)) {
                List<Double> plottable = plottables.get(key);
                List<DataPoint> dataPoints = new ArrayList<>(plottable.size());
                for (int i = 0; i < plottable.size(); i++) {
                    Double plotValue = plottable.get(i);
                    Double plotRef = plotReference.get(i);
                    if (plotValue != null && plotRef != null) {
                        dataPoints.add(new DataPoint(plotRef, plotValue));
                    }
                }
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new
                        DataPoint[dataPoints.size()]));
                series.setTitle(key);
                graph.addSeries(series);
            }
        }
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        return newLayout;
    }
}
