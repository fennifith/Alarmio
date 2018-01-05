package james.alarmio.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.adapters.SimplePagerAdapter;
import james.alarmio.data.SoundData;
import james.alarmio.fragments.AlarmSoundChooserFragment;
import james.alarmio.fragments.RadioSoundChooserFragment;
import james.alarmio.fragments.RingtoneSoundChooserFragment;
import james.alarmio.fragments.SoundChooserFragment;

public class SoundChooserDialog extends DialogFragment implements SoundChooserFragment.Listener {

    private SoundChooserFragment.Listener listener;
    private View view;

    private Disposable colorWindowBackgroundSubscription;

    private int colorWindowBackground;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);

        colorWindowBackgroundSubscription = Aesthetic.get()
                .colorWindowBackground()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        colorWindowBackground = integer;
                        if (view != null)
                            view.setBackgroundColor(integer);
                    }
                });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                if (params != null) {
                    params.windowAnimations = R.style.SlideDialogAnimation;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_sound_chooser, container, false);
        view.setBackgroundColor(colorWindowBackground);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);

        AlarmSoundChooserFragment alarmFragment = new AlarmSoundChooserFragment();
        RingtoneSoundChooserFragment ringtoneFragment = new RingtoneSoundChooserFragment();
        RadioSoundChooserFragment radioFragment = new RadioSoundChooserFragment();

        alarmFragment.setListener(this);
        ringtoneFragment.setListener(this);
        radioFragment.setListener(this);

        viewPager.setAdapter(new SimplePagerAdapter(getChildFragmentManager(), alarmFragment, ringtoneFragment, radioFragment));
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        colorWindowBackgroundSubscription.dispose();
    }

    public void setListener(SoundChooserFragment.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onSoundChosen(SoundData sound) {
        if (listener != null)
            listener.onSoundChosen(sound);

        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (view != null)
            ((Alarmio) view.getContext().getApplicationContext()).stopCurrentSound();
    }
}
