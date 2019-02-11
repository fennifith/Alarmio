package me.jfenn.alarmio.fragments;

import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.interfaces.SoundChooserListener;

public abstract class BaseSoundChooserFragment extends BasePagerFragment implements SoundChooserListener {

    private SoundChooserListener listener;

    public void setListener(SoundChooserListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSoundChosen(SoundData sound) {
        if (listener != null)
            listener.onSoundChosen(sound);
    }
}
