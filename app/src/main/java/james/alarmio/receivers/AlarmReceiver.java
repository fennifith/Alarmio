package james.alarmio.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import james.alarmio.Alarmio;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_ALARM_ID = "james.alarmio.EXTRA_ALARM_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm received!", Toast.LENGTH_SHORT).show();
        int id = intent.getIntExtra(EXTRA_ALARM_ID, 0);
        Alarmio alarmio = (Alarmio) context.getApplicationContext();
        Toast.makeText(context, alarmio.getAlarms().get(id).getName(context), Toast.LENGTH_SHORT).show();
    }
}
