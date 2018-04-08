package james.alarmio.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.annotations.Nullable;
import james.alarmio.R;
import james.alarmio.activities.MainActivity;
import james.alarmio.receivers.AlarmReceiver;

public class AlarmData implements Parcelable {

    private int id;
    public String name;
    public Calendar time;
    public boolean isEnabled = true;
    public boolean[] days = new boolean[7];
    public boolean isVibrate = true;
    public SoundData sound;

    public AlarmData(int id, Calendar time) {
        this.id = id;
        this.time = time;
    }

    public AlarmData(int id, Context context) {
        this.id = id;
        name = PreferenceData.ALARM_NAME.getSpecificOverriddenValue(context, getName(context), id);
        time = Calendar.getInstance();
        time.setTimeInMillis((long) PreferenceData.ALARM_TIME.getSpecificValue(context, id));
        isEnabled = PreferenceData.ALARM_ENABLED.getSpecificValue(context, id);
        for (int i = 0; i < 7; i++) {
            days[i] = PreferenceData.ALARM_DAY_ENABLED.getSpecificValue(context, id, i);
        }
        isVibrate = PreferenceData.ALARM_VIBRATE.getSpecificValue(context, id);
        sound = SoundData.fromString(PreferenceData.ALARM_SOUND.getSpecificOverriddenValue(context, PreferenceData.DEFAULT_RINGTONE.getValue(context, ""), id));
    }

    public void onIdChanged(int id, Context context) {
        PreferenceData.ALARM_NAME.setValue(context, getName(context), id);
        PreferenceData.ALARM_TIME.setValue(context, time.getTimeInMillis(), id);
        PreferenceData.ALARM_ENABLED.setValue(context, isEnabled, id);
        for (int i = 0; i < 7; i++) {
            PreferenceData.ALARM_DAY_ENABLED.setValue(context, days[i], id, i);
        }
        PreferenceData.ALARM_VIBRATE.setValue(context, isVibrate, id);
        PreferenceData.ALARM_SOUND.setValue(context, sound.toString(), id);

        onRemoved(context);
        this.id = id;
        if (isEnabled)
            set(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
    }

    public void onRemoved(Context context) {
        cancel(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));

        PreferenceData.ALARM_NAME.setValue(context, null, id);
        PreferenceData.ALARM_TIME.setValue(context, null, id);
        PreferenceData.ALARM_ENABLED.setValue(context, null, id);
        for (int i = 0; i < 7; i++) {
            PreferenceData.ALARM_DAY_ENABLED.setValue(context, null, id, i);
        }
        PreferenceData.ALARM_VIBRATE.setValue(context, null, id);
        PreferenceData.ALARM_SOUND.setValue(context, null, id);
    }

    public String getName(Context context) {
        if (name != null)
            return name;
        else return context.getString(R.string.title_alarm, id + 1);
    }

    public boolean isRepeat() {
        for (boolean day : days) {
            if (day)
                return true;
        }

        return false;
    }

    public void setName(Context context, String name) {
        this.name = name;
        PreferenceData.ALARM_NAME.setValue(context, name, id);
    }

    public void setTime(Context context, AlarmManager manager, long timeMillis) {
        time.setTimeInMillis(timeMillis);
        PreferenceData.ALARM_TIME.setValue(context, timeMillis, id);
        if (isEnabled)
            set(context, manager);
    }

    public void setEnabled(Context context, AlarmManager manager, boolean isEnabled) {
        this.isEnabled = isEnabled;
        PreferenceData.ALARM_ENABLED.setValue(context, isEnabled, id);
        if (isEnabled)
            set(context, manager);
        else cancel(context, manager);
    }

    public void setDays(Context context, boolean[] days) {
        this.days = days;

        for (int i = 0; i < 7; i++) {
            PreferenceData.ALARM_DAY_ENABLED.setValue(context, days[i], id, i);
        }
    }

    public void setVibrate(Context context, boolean isVibrate) {
        this.isVibrate = isVibrate;
        PreferenceData.ALARM_VIBRATE.setValue(context, isVibrate, id);
    }

    public boolean hasSound() {
        return sound != null;
    }

    public SoundData getSound() {
        return sound;
    }

    public void setSound(Context context, SoundData sound) {
        this.sound = sound;
        PreferenceData.ALARM_SOUND.setValue(context, sound != null ? sound.toString() : null, id);
    }

    @Nullable
    public Calendar getNext() {
        if (isEnabled) {
            Calendar now = Calendar.getInstance();
            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            next.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            next.set(Calendar.SECOND, 0);
            if (now.after(next))
                next.add(Calendar.DATE, 1);

            if (isRepeat()) {
                int nextDay = 0;
                for (int i = 0; i < 7; i++) {
                    int day = (now.get(Calendar.DAY_OF_WEEK) + i) % 7; // Calendar returns 1-7, modulo 7 becomes 0-6 plus one (unchanged since we want the NEXT day, not the current one)
                    if (days[day]) {
                        nextDay = day + 1; // -> 1-7
                        break;
                    }
                }

                if (nextDay > 0) {
                    next.set(Calendar.DAY_OF_WEEK, nextDay);
                    if (now.after(next))
                        next.add(Calendar.DATE, 7);
                }
            }

            return next;
        }

        return null;
    }

    public Date set(Context context, AlarmManager manager) {
        Calendar nextTime = getNext();
        setAlarm(context, manager, nextTime.getTimeInMillis());
        return nextTime.getTime();
    }

    public Date snooze(Context context, AlarmManager manager, int offsetMinutes) {
        Calendar nextTime = Calendar.getInstance();
        nextTime.add(Calendar.MINUTE, offsetMinutes);
        setAlarm(context, manager, nextTime.getTimeInMillis());
        return nextTime.getTime();
    }

    private void setAlarm(Context context, AlarmManager manager, long timeMillis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(
                            timeMillis,
                            PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0)
                    ),
                    getIntent(context)
            );
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                manager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, getIntent(context));
            else
                manager.set(AlarmManager.RTC_WAKEUP, timeMillis, getIntent(context));

            Intent intent = new Intent("android.intent.action.ALARM_CHANGED");
            intent.putExtra("alarmSet", true);
            context.sendBroadcast(intent);
        }
    }

    public void cancel(Context context, AlarmManager manager) {
        manager.cancel(getIntent(context));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent("android.intent.action.ALARM_CHANGED");
            intent.putExtra("alarmSet", false);
            context.sendBroadcast(intent);
        }
    }

    private PendingIntent getIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, id);
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected AlarmData(Parcel in) {
        id = in.readInt();
        name = in.readString();
        time = Calendar.getInstance();
        time.setTimeInMillis(in.readLong());
        isEnabled = in.readByte() != 0;
        days = in.createBooleanArray();
        isVibrate = in.readByte() != 0;
        if (in.readByte() == 1)
            sound = SoundData.fromString(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(time.getTimeInMillis());
        dest.writeByte((byte) (isEnabled ? 1 : 0));
        dest.writeBooleanArray(days);
        dest.writeByte((byte) (isVibrate ? 1 : 0));
        dest.writeByte((byte) (sound != null ? 1 : 0));
        if (sound != null)
            dest.writeString(sound.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlarmData> CREATOR = new Creator<AlarmData>() {
        @Override
        public AlarmData createFromParcel(Parcel in) {
            return new AlarmData(in);
        }

        @Override
        public AlarmData[] newArray(int size) {
            return new AlarmData[size];
        }
    };
}
