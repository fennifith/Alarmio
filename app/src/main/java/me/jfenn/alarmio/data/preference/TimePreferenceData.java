package me.jfenn.alarmio.data.preference;

import java.util.concurrent.TimeUnit;

import me.jfenn.alarmio.data.PreferenceData;
import me.jfenn.alarmio.dialogs.TimeChooserDialog;
import me.jfenn.alarmio.utils.FormatUtils;

public class TimePreferenceData extends CustomPreferenceData {

    private PreferenceData preference;

    public TimePreferenceData(PreferenceData preference, int name) {
        super(name);
        this.preference = preference;
    }

    @Override
    public String getValueName(ViewHolder holder) {
        String str = FormatUtils.formatMillis((long) preference.getValue(holder.getContext()));
        return str.substring(0, str.length() - 3);
    }

    @Override
    public void onClick(final ViewHolder holder) {
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds((long) preference.getValue(holder.getContext()));
        int minutes = (int) TimeUnit.SECONDS.toMinutes(seconds);
        int hours = (int) TimeUnit.MINUTES.toHours(minutes);
        minutes %= TimeUnit.HOURS.toMinutes(1);
        seconds %= TimeUnit.MINUTES.toSeconds(1);

        TimeChooserDialog dialog = new TimeChooserDialog(holder.getContext());
        dialog.setDefault(hours, minutes, seconds);
        dialog.setListener(new TimeChooserDialog.OnTimeChosenListener() {
            @Override
            public void onTimeChosen(int hours, int minutes, int seconds) {
                seconds += TimeUnit.HOURS.toSeconds(hours);
                seconds += TimeUnit.MINUTES.toSeconds(minutes);
                preference.setValue(holder.getContext(), TimeUnit.SECONDS.toMillis(seconds));
                bindViewHolder(holder);
            }
        });
        dialog.show();
    }

}
