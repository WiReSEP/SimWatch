package de.tu_bs.wire.simwatch.ui;

import android.content.Context;
import android.util.Log;
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

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int HEADER_COUNT = 1;
    private static final int HEADER_TYPE = 0;
    private static final int ITEM_TYPE = 1;
    private static final String TAG = "NavigationAdapter";
    private UpdateButtonListener updateButtonListener;

    public NavigationAdapter(Context context, UpdateButtonListener updateButtonListener, int resource) {
        super(context, resource);
        this.updateButtonListener = updateButtonListener;
    }

    public NavigationAdapter(Context context, UpdateButtonListener updateButtonListener, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        this.updateButtonListener = updateButtonListener;
    }

    public NavigationAdapter(Context context, UpdateButtonListener updateButtonListener, int resource, NavigationItem[] objects) {
        super(context, resource, objects);
        this.updateButtonListener = updateButtonListener;
    }

    public NavigationAdapter(Context context, UpdateButtonListener updateButtonListener, int resource, int textViewResourceId, NavigationItem[] objects) {
        super(context, resource, textViewResourceId, objects);
        this.updateButtonListener = updateButtonListener;
    }

    public NavigationAdapter(Context context, UpdateButtonListener updateButtonListener, int resource, List<NavigationItem> objects) {
        super(context, resource, objects);
        this.updateButtonListener = updateButtonListener;
    }

    public NavigationAdapter(Context context, UpdateButtonListener updateButtonListener, int resource, int textViewResourceId, List<NavigationItem> objects) {
        super(context, resource, textViewResourceId, objects);
        this.updateButtonListener = updateButtonListener;
    }

    @Override
    public int getCount() {
        return super.getCount() + HEADER_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int viewType = getItemViewType(position);

        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            switch (viewType) {
                case HEADER_TYPE:
                    v = inflater.inflate(R.layout.nav_header_main, null);
                    View headerLayout = v;
                    ImageView updateImgNav = (ImageView) headerLayout.findViewById(R.id.updateImgNav);
                    updateImgNav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UpdateButtonListener listener = NavigationAdapter.this.getUpdateButtonListener();
                            if (listener != null) {
                                listener.onUpdateButtonPressed();
                            }
                        }
                    });

                    break;
                default:
                case ITEM_TYPE:
                    v = inflater.inflate(R.layout.nav_item, null);
                    break;
            }
        }

        switch (viewType) {
            case HEADER_TYPE:
                populateNavHeader(v);
                break;
            default:
                Log.e(TAG, "Cannot determine ViewType of navigation item at position " + position);
            case ITEM_TYPE:
                NavigationItem item = getItem(listPosition2itemPosition(position));
                populateNavItem(v, item);
                break;
        }

        return v;
    }

    private UpdateButtonListener getUpdateButtonListener() {
        return updateButtonListener;
    }

    @SuppressWarnings("UnusedParameters")
    private void populateNavHeader(View v) {
        //nothing to do here
    }

    private void populateNavItem(View v, NavigationItem item) {
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
                dateOfCreation.setText(datePrinter.print(item.getDateOfCreation(), DatePrinter.Format.MEDIUM));
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
    }

    private int listPosition2itemPosition(int listPosition) {
        return listPosition - HEADER_COUNT;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_TYPE;
        } else {
            return ITEM_TYPE;
        }
    }

    public int getOffset() {
        return HEADER_COUNT;
    }
}
