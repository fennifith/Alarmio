package james.alarmio.fragments;

import james.alarmio.data.SoundData;

public abstract class SoundChooserFragment extends BasePagerFragment {

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    Listener getListener() {
        return listener;
    }

    public void onSoundChosen(SoundData sound) {
        if (listener != null)
            listener.onSoundChosen(sound);
    }

    public interface Listener {
        void onSoundChosen(SoundData sound);
    }
}
