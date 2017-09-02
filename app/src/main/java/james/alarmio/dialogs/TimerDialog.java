package james.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import james.alarmio.Alarmio;
import james.alarmio.R;

public class TimerDialog extends AppCompatDialog implements View.OnClickListener {

    private TextView time;
    private ImageView backspace;

    private String input = "000000";

    private Alarmio alarmio;

    public TimerDialog(Context context) {
        super(context);
        alarmio = (Alarmio) context.getApplicationContext();
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
                //TODO: start timer
                dismiss();
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
        Log.d(getClass().getName(), String.valueOf(hours));
        int minutes = Integer.parseInt(input.substring(2, 4));
        int seconds = Integer.parseInt(input.substring(4, 6));

        backspace.setVisibility(hours == 0 && minutes == 0 && seconds == 0 ? View.GONE : View.VISIBLE);

        if (hours > 0)
            return String.format(Locale.getDefault(), "%dh %02dm %02ds", hours, minutes, seconds);
        else return String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds);
    }

    @Override
    public void onClick(View view) {
        if (view instanceof TextView)
            input(((TextView) view).getText().toString());
        else backspace();
    }
}
