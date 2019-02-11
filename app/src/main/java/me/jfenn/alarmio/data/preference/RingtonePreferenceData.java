package me.jfenn.alarmio.data.preference;

import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.PreferenceData;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.dialogs.SoundChooserDialog;
import me.jfenn.alarmio.interfaces.SoundChooserListener;

public class RingtonePreferenceData extends CustomPreferenceData {

    private PreferenceData preference;

    public RingtonePreferenceData(PreferenceData preference, int name) {
        super(name);
        this.preference = preference;
    }

    @Override
    public String getValueName(ViewHolder holder) {
        String sound = preference.getValue(holder.getContext(), "");
        return sound != null && sound.length() > 0 ? SoundData.fromString(sound).getName() : holder.getContext().getString(R.string.title_sound_none);
    }

    @Override
    public void onClick(final ViewHolder holder) {
        SoundChooserDialog dialog = new SoundChooserDialog();
        dialog.setListener(new SoundChooserListener() {
            @Override
            public void onSoundChosen(SoundData sound) {
                preference.setValue(holder.getContext(), sound != null ? sound.toString() : null);
                bindViewHolder(holder);
            }
        });
        dialog.show(holder.getAlarmio().getFragmentManager(), null);
    }
}
