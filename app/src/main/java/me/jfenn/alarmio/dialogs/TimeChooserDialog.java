package me.jfenn.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.jfenn.alarmio.R;

public class TimeChooserDialog extends AestheticDialog implements View.OnClickListener {

    private TextView time;
    private ImageView backspace;

    private String input = "000000";

    private OnTimeChosenListener listener;

    public TimeChooserDialog(Context context) {
        super(context);
    }

    public void setDefault(int hours, int minutes, int seconds) {
        hours += TimeUnit.MINUTES.toHours(minutes);
        minutes = (int) ((minutes % TimeUnit.HOURS.toMinutes(1)) + TimeUnit.SECONDS.toMinutes(seconds));
        seconds %= TimeUnit.MINUTES.toSeconds(1);

        input = String.format(Locale.getDefault(), "%02d%02d%02d", hours, minutes, seconds);
    }

    public void setListener(OnTimeChosenListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_time_chooser);

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

        TextView startButton = findViewById(R.id.start);
        startButton.setText(android.R.string.ok);
        startButton.setOnClickListener(view -> {
            if (Integer.parseInt(input) > 0) {
                if (listener != null) {
                    listener.onTimeChosen(
                            Integer.parseInt(input.substring(0, 2)),
                            Integer.parseInt(input.substring(2, 4)),
                            Integer.parseInt(input.substring(4, 6))
                    );
                }

                dismiss();
            }
        });

        findViewById(R.id.cancel).setOnClickListener(view -> dismiss());

        Aesthetic.Companion.get()
                .textColorPrimary()
                .take(1)
                .subscribe(integer -> backspace.setColorFilter(integer));
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

    public interface OnTimeChosenListener {
        void onTimeChosen(int hours, int minutes, int seconds);
    }
}
