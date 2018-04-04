package james.alarmio.data.preference;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import james.alarmio.data.PreferenceData;
import james.alarmio.dialogs.TimeChooserDialog;

public class TimePreferenceData extends CustomPreferenceData {

    private PreferenceData preference;

    public TimePreferenceData(PreferenceData preference, int name) {
        super(name);
        this.preference = preference;
    }

    @Override
    String getValueName(ViewHolder holder) {
        int minutes = preference.getValue(holder.getContext());
        int hours = (int) TimeUnit.MINUTES.toHours(minutes);
        minutes %= TimeUnit.HOURS.toMinutes(1);

        if (hours > 0)
            return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        else return String.format(Locale.getDefault(), "%dm", minutes);
    }

    @Override
    void onClick(final ViewHolder holder) {
        int minutes = preference.getValue(holder.getContext());
        int hours = (int) TimeUnit.MINUTES.toHours(minutes);
        minutes %= TimeUnit.HOURS.toMinutes(1);

        TimeChooserDialog dialog = new TimeChooserDialog(holder.getContext());
        dialog.setDefault(hours, minutes, 0);
        dialog.setListener(new TimeChooserDialog.OnTimeChosenListener() {
            @Override
            public void onTimeChosen(int hours, int minutes, int seconds) {
                minutes += TimeUnit.HOURS.toMinutes(hours);
                preference.setValue(holder.getContext(), minutes);
            }
        });
        dialog.show();
    }

}
