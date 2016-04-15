package de.tu_bs.wire.simwatch.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.api.models.Snapshot;
import de.tu_bs.wire.simwatch.api.types.Matrix;
import de.tu_bs.wire.simwatch.api.types.Types;
import de.tu_bs.wire.simwatch.api.types.Vector;

/**
 * Draws Snapshots of simulation Instances into Android layouts
 */
public class SimulationPainter {

    private static final String TAG = "SimulationPainter";
    private Snapshot simulation;
    private Context context;
    private Profile profile;

    public SimulationPainter(Snapshot simulation, Context context, Profile profile) {
        this.simulation = simulation;
        this.context = context;
        this.profile = profile;
    }

    /**
     * Draws Snapshot into the given RelativeLayout
     *
     * @param viewGroup Layout to draw into
     */
    public void draw(ViewGroup viewGroup) {
        if (profile != null) {
            for (Map.Entry<String, String> property : profile.getProperties().entrySet()) {
                drawProperty(viewGroup, property.getKey(), simulation.getData().get(property.getKey()), property.getValue());
            }
            GraphPainter graphPainter = new GraphPainter(simulation, profile, context);
            for (String key : graphPainter.getPlottableKeys()) {
                View graphView = graphPainter.draw(key);
                viewGroup.addView(graphView);
            }
        }
    }

    private void drawProperty(ViewGroup viewGroup, String name, JsonElement attribute, String type) {
        try {
            if (viewGroup != null && attribute != null) {
                switch (Types.getType(type)) {
                    case NUMBER:
                        Double d = new Gson().fromJson(attribute.getAsString(), Double.class);
                        if (d == d.intValue()) {
                            drawString(viewGroup, name + ": " + d.intValue());
                        } else {
                            drawString(viewGroup, name + ": " + d.toString());
                        }
                        break;
                    case STRING:
                        String s = new Gson().fromJson(attribute.getAsString(), String.class);
                        drawString(viewGroup, name + ": " + s);
                        break;
                    case VECTOR:
                        Vector vector = new Gson().fromJson(attribute.getAsString(), Vector.class);
                        drawString(viewGroup, name + ": " + vector.toString());
                        break;
                    case MATRIX:
                        Matrix matrix = new Gson().fromJson(attribute.getAsString(), Matrix.class);
                        drawString(viewGroup, name + ": " + matrix.toString());
                        break;
                    case BOOLEAN:
                        Boolean bool = new Gson().fromJson(attribute.getAsString(), Boolean.class);
                        drawString(viewGroup, name + ": " + bool.toString());
                        break;
                    case PLOTTABLE:
                    case PLOT_REFERENCE:
                    default:
                }
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Cannot draw attribute. Property has a broken syntax", e);
        }
    }

    private void drawString(ViewGroup viewGroup, String str) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newLayout = inflater.inflate(R.layout.text_layout, null);
        TextView tv = (TextView) newLayout.findViewById(R.id.simulationTextView);
        tv.setText(str);
        viewGroup.addView(newLayout);
    }
}
