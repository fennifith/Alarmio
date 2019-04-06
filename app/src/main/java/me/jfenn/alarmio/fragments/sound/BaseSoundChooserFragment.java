package me.jfenn.alarmio.fragments.sound;

import android.content.Context;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.fragments.BasePagerFragment;
import me.jfenn.alarmio.interfaces.ContextFragmentInstantiator;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }

    abstract static class Instantiator extends ContextFragmentInstantiator {

        private WeakReference<SoundChooserListener> listener;

        public Instantiator(Context context, SoundChooserListener listener) {
            super(context);
            this.listener = new WeakReference<>(listener);
        }

        @Nullable
        @Override
        public BasePagerFragment newInstance(int position) {
            SoundChooserListener listener = this.listener.get();
            if (listener != null)
                return newInstance(position, listener);
            else return null;
        }

        abstract BasePagerFragment newInstance(int position, SoundChooserListener listener);
    }

}
