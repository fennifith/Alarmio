package james.alarmio.receivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import james.alarmio.Alarmio;
import james.alarmio.activities.AlarmActivity;
import james.alarmio.data.AlarmData;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_ALARM_ID = "james.alarmio.EXTRA_ALARM_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Alarmio alarmio = (Alarmio) context.getApplicationContext();
        AlarmData alarm = alarmio.getAlarms().get(intent.getIntExtra(EXTRA_ALARM_ID, 0));
        if (alarm.isRepeat())
            alarm.set(context, manager);
        else alarm.setEnabled(context, alarmio.getPrefs(), manager, false);
        Toast.makeText(context, alarm.getName(context), Toast.LENGTH_SHORT).show();
        alarmio.onAlarmsChanged();

        Intent ringer = new Intent(context, AlarmActivity.class);
        ringer.putExtra(AlarmActivity.EXTRA_ALARM, alarm);
        context.startActivity(ringer);
    }
}
