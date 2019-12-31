package me.jfenn.alarmio.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.activities.MainActivity;
import me.jfenn.alarmio.receivers.AlarmReceiver;
import me.jfenn.alarmio.services.SleepReminderService;

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
        sound = SoundData.fromString(PreferenceData.ALARM_SOUND.getSpecificOverriddenValue(context, PreferenceData.DEFAULT_ALARM_RINGTONE.getValue(context, ""), id));
    }

    /**
     * Moves this AlarmData's preferences to another "id".
     *
     * @param id            The new id to be assigned
     * @param context       An active context instance.
     */
    public void onIdChanged(int id, Context context) {
        PreferenceData.ALARM_NAME.setValue(context, getName(context), id);
        PreferenceData.ALARM_TIME.setValue(context, time != null ? time.getTimeInMillis() : null, id);
        PreferenceData.ALARM_ENABLED.setValue(context, isEnabled, id);
        for (int i = 0; i < 7; i++) {
            PreferenceData.ALARM_DAY_ENABLED.setValue(context, days[i], id, i);
        }
        PreferenceData.ALARM_VIBRATE.setValue(context, isVibrate, id);
        PreferenceData.ALARM_SOUND.setValue(context, sound != null ? sound.toString() : null, id);

        onRemoved(context);
        this.id = id;
        if (isEnabled)
            set(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
    }

    /**
     * Removes this AlarmData's preferences.
     *
     * @param context       An active context instance.
     */
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

    /**
     * Returns the user-defined "name" of the alarm, defaulting to
     * "Alarm (1..)" if unset.
     *
     * @param context       An active context instance.
     * @return              The alarm name, as a string.
     */
    public String getName(Context context) {
        if (name != null)
            return name;
        else return context.getString(R.string.title_alarm, id + 1);
    }

    /**
     * Returns whether the alarm should repeat on a set interval
     * or not.
     *
     * @return              If repeat is enabled for this alarm.
     */
    public boolean isRepeat() {
        for (boolean day : days) {
            if (day)
                return true;
        }

        return false;
    }

    /**
     * Sets the user-defined "name" of the alarm.
     *
     * @param context       An active context instance.
     * @param name          The new name to be set.
     */
    public void setName(Context context, String name) {
        this.name = name;
        PreferenceData.ALARM_NAME.setValue(context, name, id);
    }

    /**
     * Change the scheduled alarm time.
     *
     * @param context       An active context instance.
     * @param manager       An AlarmManager to schedule the alarm on.
     * @param timeMillis    The UNIX time (in milliseconds) that the alarm should ring at.
     *                      This is independent to days; if the time correlates to 9:30 on
     *                      a Tuesday when the alarm should only repeat on Wednesdays and
     *                      Thursdays, then the alarm will next ring at 9:30 on Wednesday.
     */
    public void setTime(Context context, AlarmManager manager, long timeMillis) {
        time.setTimeInMillis(timeMillis);
        PreferenceData.ALARM_TIME.setValue(context, timeMillis, id);
        if (isEnabled)
            set(context, manager);
    }

    /**
     * Set whether the alarm is enabled.
     *
     * @param context       An active context instance.
     * @param manager       An AlarmManager to schedule the alarm on.
     * @param isEnabled     Whether the alarm is enabled.
     */
    public void setEnabled(Context context, AlarmManager manager, boolean isEnabled) {
        this.isEnabled = isEnabled;
        PreferenceData.ALARM_ENABLED.setValue(context, isEnabled, id);
        if (isEnabled)
            set(context, manager);
        else cancel(context, manager);
    }

    /**
     * Sets the days of the week that the alarm should ring on. If
     * no days are specified, the alarm will act as a one-time alert
     * and will not repeat.
     *
     * @param context       An active context instance.
     * @param days          A boolean array, with a length of 7 (seven days of the week)
     *                      specifying whether repeat is enabled for that day.
     */
    public void setDays(Context context, boolean[] days) {
        this.days = days;

        for (int i = 0; i < 7; i++) {
            PreferenceData.ALARM_DAY_ENABLED.setValue(context, days[i], id, i);
        }
    }

    /**
     * Set whether the alarm should vibrate.
     *
     * @param context       An active context instance.
     * @param isVibrate     Whether the alarm should vibrate.
     */
    public void setVibrate(Context context, boolean isVibrate) {
        this.isVibrate = isVibrate;
        PreferenceData.ALARM_VIBRATE.setValue(context, isVibrate, id);
    }

    /**
     * Return whether the alarm has a sound or not.
     *
     * @return              A boolean defining whether a sound has been set
     *                      for the alarm.
     */
    public boolean hasSound() {
        return sound != null;
    }

    /**
     * Get the [SoundData](./SoundData) sound specified for the alarm.
     *
     * @return              An instance of SoundData describing the sound that
     *                      the alarm should make (or null).
     */
    @Nullable
    public SoundData getSound() {
        return sound;
    }


    /**
     * Set the sound that the alarm should make.
     *
     * @param context       An active context instance.
     * @param sound         A [SoundData](./SoundData) defining the sound that
     *                      the alarm should make.
     */
    public void setSound(Context context, @Nullable SoundData sound) {
        this.sound = sound;
        PreferenceData.ALARM_SOUND.setValue(context, sound != null ? sound.toString() : null, id);
    }

    /**
     * Get the next time that the alarm should wring.
     *
     * @return              A Calendar object defining the next time that the alarm should ring at.
     * @see                 [java.util.Calendar Documentation](https://developer.android.com/reference/java/util/Calendar)
     */
    @Nullable
    public Calendar getNext() {
        if (isEnabled) {
            Calendar now = Calendar.getInstance();
            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            next.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            next.set(Calendar.SECOND, 0);

            while (now.after(next))
                next.add(Calendar.DATE, 1);

            if (isRepeat()) {
                int nextDay = next.get(Calendar.DAY_OF_WEEK) - 1; // index on 0-6, rather than the 1-7 returned by Calendar

                for (int i = 0; i < 7 && !days[nextDay]; i++) {
                    nextDay++;
                    nextDay %= 7;
                }

                next.set(Calendar.DAY_OF_WEEK, nextDay + 1); // + 1 = back to 1-7 range

                while (now.after(next))
                    next.add(Calendar.DATE, 7);
            }

            return next;
        }

        return null;
    }

    /**
     * Set the next time for the alarm to ring.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager to schedule the alarm on.
     * @return              The next [Date](https://developer.android.com/reference/java/util/Date)
     *                      at which the alarm will ring.
     */
    public Date set(Context context, AlarmManager manager) {
        Calendar nextTime = getNext();
        setAlarm(context, manager, nextTime.getTimeInMillis());
        return nextTime.getTime();
    }

    /**
     * Schedule a time for the alarm to ring at.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager to schedule the alarm on.
     * @param timeMillis    A UNIX timestamp specifying the next time for the alarm to ring.
     */
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

        manager.set(AlarmManager.RTC_WAKEUP,
                timeMillis - (long) PreferenceData.SLEEP_REMINDER_TIME.getValue(context),
                PendingIntent.getService(context, 0, new Intent(context, SleepReminderService.class), 0));

        SleepReminderService.refreshSleepTime(context);
    }

    /**
     * Cancel the next time for the alarm to ring.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager that the alarm was scheduled on.
     */
    public void cancel(Context context, AlarmManager manager) {
        manager.cancel(getIntent(context));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent("android.intent.action.ALARM_CHANGED");
            intent.putExtra("alarmSet", false);
            context.sendBroadcast(intent);
        }
    }

    /**
     * The intent to fire when the alarm should ring.
     *
     * @param context       An active context instance.
     * @return              A PendingIntent that will open the alert screen.
     */
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
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeLong(time.getTimeInMillis());
        parcel.writeByte((byte) (isEnabled ? 1 : 0));
        parcel.writeBooleanArray(days);
        parcel.writeByte((byte) (isVibrate ? 1 : 0));
        parcel.writeByte((byte) (sound != null ? 1 : 0));
        if (sound != null)
            parcel.writeString(sound.toString());
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
