package de.tu_bs.wire.simwatch.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import de.tu_bs.wire.simwatch.R;
import de.tu_bs.wire.simwatch.api.models.Instance;

/**
 * Created by mw on 12.07.16.
 */
public class NavigationAdapter extends ArrayAdapter<NavigationItem> {

    public NavigationAdapter(Context context, int resource) {
        super(context, resource);
    }

    public NavigationAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public NavigationAdapter(Context context, int resource, NavigationItem[] objects) {
        super(context, resource, objects);
    }

    public NavigationAdapter(Context context, int resource, int textViewResourceId, NavigationItem[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public NavigationAdapter(Context context, int resource, List<NavigationItem> objects) {
        super(context, resource, objects);
    }

    public NavigationAdapter(Context context, int resource, int textViewResourceId, List<NavigationItem> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.nav_item, null);
        }

        NavigationItem item = getItem(position);

        if (item != null) {
            TextView name = (TextView) v.findViewById(R.id.name);
            TextView UUID = (TextView) v.findViewById(R.id.UUID);
            TextView newUpdates = (TextView) v.findViewById(R.id.newUpdates);
            TextView dateOfCreation = (TextView) v.findViewById(R.id.dateOfCreation);
            TextView lastUpdate = (TextView) v.findViewById(R.id.lastUpdate);
            ImageView status = (ImageView) v.findViewById(R.id.status);

            DatePrinter datePrinter = new DatePrinter();

            if (name != null && item.getName() != null) {
                name.setText(item.getName());
            }
            if (UUID != null && item.getUUID() != null) {
                UUID.setText(item.shortenedUUID());
            }
            if (newUpdates != null && item.getNewUpdates() > 0) {
                newUpdates.setText(String.format(Locale.getDefault(), "(%d)", item.getNewUpdates()));
            }
            if (dateOfCreation != null && item.getDateOfCreation() != null) {
                dateOfCreation.setText(datePrinter.print(item.getDateOfCreation()));
            }
            if (lastUpdate != null && item.getLastUpdate() != null) {
                lastUpdate.setText(String.format(getContext().getString(R.string.last_update), datePrinter.print(item.getLastUpdate(), DatePrinter.Format.MEDIUM)));
            }
            if (status != null) {
                Instance.Status s = item.getStatus();
                if (s == null) {
                    s = Instance.Status.RUNNING;
                }
                switch (s) {
                    default:
                    case RUNNING:
                        status.setVisibility(View.GONE);
                        break;
                    case STOPPED:
                        status.setVisibility(View.VISIBLE);
                        status.setImageResource(R.drawable.ic_done_black_24dp);
                        break;
                    case FAILED:
                        status.setVisibility(View.VISIBLE);
                        status.setImageResource(R.drawable.ic_error_outline_black_24dp);
                        break;
                }
            }
        }

        return v;
    }
}
