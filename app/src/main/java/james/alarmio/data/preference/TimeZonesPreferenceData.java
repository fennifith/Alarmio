package james.alarmio.data.preference;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import james.alarmio.adapters.TimeZonesAdapter;
import james.alarmio.data.PreferenceData;
import james.alarmio.dialogs.TimeZoneChooserDialog;

public class TimeZonesPreferenceData extends ListPreferenceData {

    public TimeZonesPreferenceData(PreferenceData preference, int title) {
        super(preference, title);
    }

    @Override
    RecyclerView.Adapter getAdapter(Context context, String[] items) {
        return new TimeZonesAdapter(new ArrayList<>(Arrays.asList(items)));
    }

    @Override
    void requestAddItem(final ViewHolder holder) {
        TimeZoneChooserDialog dialog = new TimeZoneChooserDialog(holder.getContext());
        dialog.excludeTimeZones(getItems(holder.getContext()));
        dialog.setListener(new TimeZoneChooserDialog.OnTimeZoneListener() {
            @Override
            public void onTimeZoneChosen(String timeZone) {
                addItem(holder, timeZone);
            }
        });
        dialog.show();
    }
}
