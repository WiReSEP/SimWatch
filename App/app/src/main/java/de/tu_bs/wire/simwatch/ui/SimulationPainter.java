package de.tu_bs.wire.simwatch.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.util.Map;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.GsonUtil;
import de.tu_bs.wire.simwatch.api.models.Attachment;
import de.tu_bs.wire.simwatch.api.models.Profile;
import de.tu_bs.wire.simwatch.api.models.Snapshot;
import de.tu_bs.wire.simwatch.api.types.Matrix;
import de.tu_bs.wire.simwatch.api.types.Types;
import de.tu_bs.wire.simwatch.api.types.Vector;
import de.tu_bs.wire.simwatch.simulation.AttachmentManager;
import de.tu_bs.wire.simwatch.simulation.ImageUpdater;
import de.tu_bs.wire.simwatch.ui.listeners.ImageClickListener;
import de.tu_bs.wire.simwatch.ui.listeners.OpenFileListener;

/**
 * Draws Snapshots of simulation Instances into Android layouts
 */
public class SimulationPainter {

    private static final String TAG = "SimulationPainter";
    private Snapshot simulation;
    private Activity context;
    private Profile profile;
    private AttachmentManager attachmentManager;

    public SimulationPainter(Snapshot simulation, Activity context, Profile profile, AttachmentManager attachmentManager) {
        this.simulation = simulation;
        this.context = context;
        this.profile = profile;
        this.attachmentManager = attachmentManager;
    }

    /**
     * Draws Snapshot into the given RelativeLayout
     *
     * @param viewGroup Layout to draw into
     */
    public void draw(ViewGroup viewGroup) {
        attachmentManager.removeAllListeners();
        if (profile != null) {
            for (Map.Entry<String, String> property : profile.getProperties().entrySet()) {
                drawProperty(viewGroup, property.getKey(), simulation.getData().get(property.getKey()), property.getValue());
            }
            GraphPainter graphPainter = new GraphPainter(simulation, profile, context);
            for (String key : graphPainter.getPlottableKeys()) {
                View graphView = graphPainter.draw(key);
                viewGroup.addView(graphView);
            }
            for (Map.Entry<String, String> property : profile.getProperties().entrySet()) {
                Attachment attachment = new Attachment(simulation.getInstanceID(), property.getKey());
                drawBinaryProperty(viewGroup, property.getKey(), attachment, property.getValue());
            }
        }
    }

    private void drawProperty(ViewGroup viewGroup, String propertyName, JsonElement attribute, String type) {
        try {
            if (viewGroup != null && attribute != null) {
                Gson gson = GsonUtil.getGson();
                switch (Types.getType(type)) {
                    case NUMBER:
                        Double d = gson.fromJson(attribute, Double.class);
                        if (d == d.intValue()) {
                            drawString(viewGroup, propertyName + ": " + d.intValue());
                        } else {
                            drawString(viewGroup, propertyName + ": " + d.toString());
                        }
                        break;
                    case STRING:
                        String s = gson.fromJson(attribute, String.class);
                        drawString(viewGroup, propertyName + ": " + s);
                        break;
                    case VECTOR:
                        Vector vector = gson.fromJson(attribute, Vector.class);
                        drawString(viewGroup, propertyName + ": " + vector.toString());
                        break;
                    case MATRIX:
                        Matrix matrix = gson.fromJson(attribute, Matrix.class);
                        drawString(viewGroup, propertyName + ": " + matrix.toString());
                        break;
                    case BOOLEAN:
                        Boolean bool = gson.fromJson(attribute, Boolean.class);
                        drawString(viewGroup, propertyName + ": " + bool.toString());
                        break;
                    case PLOTTABLE:
                    case PLOT_REFERENCE:
                    case IMAGE_BINARY:
                    case NON_IMAGE_BINARY:
                    default:
                }
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Cannot draw attribute. Property has a broken syntax", e);
        }
    }

    private void drawBinaryProperty(ViewGroup viewGroup, String propertyName, Attachment attachment, String typeName) {
        if (viewGroup != null && attachment != null) {
            File file = attachmentManager.fileFromAttachmentName(attachment);
            Types.Type type = Types.getType(typeName);
            if (Types.isBinary(type)) {
                String mimeType = Types.getMimeType(typeName);
                try {
                    switch (type) {
                        case IMAGE_BINARY:
                            ImageUpdater imageUpdater = drawImageForAttachment(viewGroup, propertyName, file, mimeType);
                            attachmentManager.addAttachmentListener(attachment, imageUpdater);
                            imageUpdater.getImageView().setOnClickListener(new ImageClickListener(context, file));
                            break;
                        case NON_IMAGE_BINARY:
                            drawImageForAttachment(viewGroup, propertyName, file, mimeType);
                            break;
                        default:
                    }
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Cannot draw attribute. Property has a broken syntax", e);
                }
            }
        }
    }

    private ImageUpdater drawImageForAttachment(ViewGroup viewGroup, String name, File file, String mimeType) {
        Log.d(TAG, "Drawing image '" + name + "'");
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newLayout = inflater.inflate(R.layout.captioned_image, null);
        ImageView image = (ImageView) newLayout.findViewById(R.id.captionedImage);
        TextView caption = (TextView) newLayout.findViewById(R.id.caption);

        caption.setText(name);

        viewGroup.addView(newLayout);

        image.setOnLongClickListener(new OpenFileListener(context, file, mimeType));
        ImageUpdater imageUpdater = new ImageUpdater(context, image);

        if (file.exists() && file.canRead()) {
            imageUpdater.onDownloaded(file);
        }

        return imageUpdater;
    }

    private void drawString(ViewGroup viewGroup, String str) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newLayout = inflater.inflate(R.layout.text_layout, null);
        TextView tv = (TextView) newLayout.findViewById(R.id.simulationTextView);
        tv.setText(str);
        viewGroup.addView(newLayout);
    }
}
