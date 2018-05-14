package james.alarmio.dialogs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;

import james.alarmio.R;

public class TimePickerDialog extends AestheticDialog implements TimePicker.OnTimeChangedListener {

    private int hourOfDay, minute;
    private OnTimeChosenListener listener;

    public TimePickerDialog(Context context) {
        super(context);
        Calendar now = Calendar.getInstance();
        hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        minute = now.get(Calendar.MINUTE);
    }

    public TimePickerDialog setListener(OnTimeChosenListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_time_picker);

        TimePicker timePicker = findViewById(R.id.timePicker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hourOfDay);
            timePicker.setMinute(minute);
        } else {
            timePicker.setCurrentHour(hourOfDay);
            timePicker.setCurrentMinute(minute);
        }

        timePicker.setOnTimeChangedListener(this);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onTimeChosen(hourOfDay, minute);

                dismiss();
            }
        });
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    public interface OnTimeChosenListener {
        void onTimeChosen(int hourOfDay, int minute);
    }

}
