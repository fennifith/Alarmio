package me.jfenn.alarmio.dialogs;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.fragment.app.FragmentManager;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.PreferenceData;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.data.TimerData;
import me.jfenn.alarmio.fragments.TimerFragment;

public class TimerDialog extends AestheticDialog implements View.OnClickListener {

    private ImageView ringtoneImage;
    private TextView ringtoneText;
    private ImageView vibrateImage;

    private SoundData ringtone;
    private boolean isVibrate = true;

    private TextView time;
    private ImageView backspace;

    private String input = "000000";

    private Alarmio alarmio;
    private FragmentManager manager;

    public TimerDialog(Context context, FragmentManager manager) {
        super(context);
        alarmio = (Alarmio) context.getApplicationContext();
        this.manager = manager;
        ringtone = SoundData.fromString(PreferenceData.DEFAULT_TIMER_RINGTONE.getValue(context, ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_timer);

        ringtoneImage = findViewById(R.id.ringtoneImage);
        ringtoneText = findViewById(R.id.ringtoneText);
        vibrateImage = findViewById(R.id.vibrateImage);

        time = findViewById(R.id.time);
        backspace = findViewById(R.id.backspace);

        time.setText(getTime());

        backspace.setOnClickListener(this);
        findViewById(R.id.one).setOnClickListener(this);
        findViewById(R.id.two).setOnClickListener(this);
        findViewById(R.id.three).setOnClickListener(this);
        findViewById(R.id.four).setOnClickListener(this);
        findViewById(R.id.five).setOnClickListener(this);
        findViewById(R.id.six).setOnClickListener(this);
        findViewById(R.id.seven).setOnClickListener(this);
        findViewById(R.id.eight).setOnClickListener(this);
        findViewById(R.id.nine).setOnClickListener(this);
        findViewById(R.id.zero).setOnClickListener(this);

        ringtoneImage.setImageResource(ringtone != null ? R.drawable.ic_ringtone : R.drawable.ic_ringtone_disabled);
        ringtoneImage.setAlpha(ringtone != null ? 1f : 0.333f);

        if (ringtone != null)
            ringtoneText.setText(ringtone.getName());
        else ringtoneText.setText(R.string.title_sound_none);

        findViewById(R.id.ringtone).setOnClickListener(v -> {
            SoundChooserDialog dialog = new SoundChooserDialog();
            dialog.setListener(sound -> {
                ringtone = sound;
                ringtoneImage.setImageResource(sound != null ? R.drawable.ic_ringtone : R.drawable.ic_ringtone_disabled);
                ringtoneImage.setAlpha(sound != null ? 1f : 0.333f);

                if (sound != null)
                    ringtoneText.setText(sound.getName());
                else ringtoneText.setText(R.string.title_sound_none);
            });
            dialog.show(manager, "");
        });

        findViewById(R.id.vibrate).setOnClickListener(v -> {
            isVibrate = !isVibrate;

            AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(v.getContext(), isVibrate ? R.drawable.ic_none_to_vibrate : R.drawable.ic_vibrate_to_none);
            if (drawable != null) {
                vibrateImage.setImageDrawable(drawable);
                drawable.start();
            } else
                vibrateImage.setImageResource(isVibrate ? R.drawable.ic_vibrate : R.drawable.ic_none);

            vibrateImage.animate().alpha(isVibrate ? 1f : 0.333f).start();

            if (isVibrate)
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        });

        findViewById(R.id.start).setOnClickListener(view -> {
            if (Integer.parseInt(input) > 0) {
                TimerData timer = alarmio.newTimer();
                timer.setDuration(getMillis(), alarmio);
                timer.setVibrate(view.getContext(), isVibrate);
                timer.setSound(view.getContext(), ringtone);
                timer.set(alarmio, ((AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE)));
                alarmio.onTimerStarted();

                Bundle args = new Bundle();
                args.putParcelable(TimerFragment.EXTRA_TIMER, timer);
                TimerFragment fragment = new TimerFragment();
                fragment.setArguments(args);

                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                        .replace(R.id.fragment, fragment)
                        .addToBackStack(null)
                        .commit();

                dismiss();
            }
        });

        findViewById(R.id.cancel).setOnClickListener(view -> dismiss());

        Aesthetic.Companion.get()
                .textColorPrimary()
                .take(1)
                .subscribe(integer -> {
                    ringtoneImage.setColorFilter(integer);
                    vibrateImage.setColorFilter(integer);
                    backspace.setColorFilter(integer);
                });
    }

    private void input(String character) {
        input = input.substring(character.length()) + character;
        time.setText(getTime());
    }

    private void backspace() {
        input = "0" + input.substring(0, input.length() - 1);
        time.setText(getTime());
    }

    private String getTime() {
        int hours = Integer.parseInt(input.substring(0, 2));
        int minutes = Integer.parseInt(input.substring(2, 4));
        int seconds = Integer.parseInt(input.substring(4, 6));

        backspace.setVisibility(hours == 0 && minutes == 0 && seconds == 0 ? View.GONE : View.VISIBLE);

        if (hours > 0)
            return String.format(Locale.getDefault(), "%dh %02dm %02ds", hours, minutes, seconds);
        else return String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds);
    }

    private long getMillis() {
        long millis = 0;

        int hours = Integer.parseInt(input.substring(0, 2));
        int minutes = Integer.parseInt(input.substring(2, 4));
        int seconds = Integer.parseInt(input.substring(4, 6));

        millis += TimeUnit.HOURS.toMillis(hours);
        millis += TimeUnit.MINUTES.toMillis(minutes);
        millis += TimeUnit.SECONDS.toMillis(seconds);

        return millis;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof TextView)
            input(((TextView) view).getText().toString());
        else backspace();
    }
}
