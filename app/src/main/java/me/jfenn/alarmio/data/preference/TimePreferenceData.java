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
        dialog.setListener((hours1, minutes1, seconds1) -> {
            seconds1 += TimeUnit.HOURS.toSeconds(hours1);
            seconds1 += TimeUnit.MINUTES.toSeconds(minutes1);
            preference.setValue(holder.getContext(), TimeUnit.SECONDS.toMillis(seconds1));
            bindViewHolder(holder);
        });
        dialog.show();
    }

}
