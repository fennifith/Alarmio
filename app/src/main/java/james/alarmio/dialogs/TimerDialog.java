package james.alarmio.dialogs;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.data.TimerData;
import james.alarmio.fragments.TimerFragment;

public class TimerDialog extends AppCompatDialog implements View.OnClickListener {

    private TextView time;
    private ImageView backspace;

    private String input = "000000";

    private Alarmio alarmio;
    private FragmentManager manager;

    public TimerDialog(Context context, FragmentManager manager) {
        super(context);
        alarmio = (Alarmio) context.getApplicationContext();
        this.manager = manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_timer);

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

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Integer.parseInt(input) > 0) {
                    TimerData timer = alarmio.newTimer();
                    timer.setDuration(alarmio.getPrefs(), getMillis());
                    timer.set(getContext(), alarmio.getPrefs(), ((AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE)));
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
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
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
