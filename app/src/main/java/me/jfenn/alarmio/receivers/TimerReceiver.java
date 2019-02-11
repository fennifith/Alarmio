package me.jfenn.alarmio.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.activities.AlarmActivity;
import me.jfenn.alarmio.data.TimerData;

public class TimerReceiver extends BroadcastReceiver {

    public static final String EXTRA_TIMER_ID = "james.alarmio.EXTRA_TIMER_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Alarmio alarmio = (Alarmio) context.getApplicationContext();
        TimerData timer = alarmio.getTimers().get(intent.getIntExtra(EXTRA_TIMER_ID, 0));
        alarmio.removeTimer(timer);

        Intent ringer = new Intent(context, AlarmActivity.class);
        ringer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ringer.putExtra(AlarmActivity.EXTRA_TIMER, timer);
        context.startActivity(ringer);
    }
}
