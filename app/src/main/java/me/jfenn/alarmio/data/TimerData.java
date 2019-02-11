package me.jfenn.alarmio.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import me.jfenn.alarmio.receivers.TimerReceiver;

public class TimerData implements Parcelable {

    private int id;
    private long duration = 600000;
    private long endTime;
    public boolean isVibrate = true;
    public SoundData sound;

    public TimerData(int id) {
        this.id = id;
    }

    public TimerData(int id, Context context) {
        this.id = id;
        try {
            duration = PreferenceData.TIMER_DURATION.getSpecificValue(context, id);
        } catch (ClassCastException e) {
            duration = (int) PreferenceData.TIMER_DURATION.getSpecificValue(context, id);
        }
        try {
            endTime = PreferenceData.TIMER_END_TIME.getSpecificValue(context, id);
        } catch (ClassCastException e) {
            endTime = (int) PreferenceData.TIMER_END_TIME.getSpecificValue(context, id);
        }

        isVibrate = PreferenceData.TIMER_VIBRATE.getSpecificValue(context, id);
        sound = SoundData.fromString(PreferenceData.TIMER_SOUND.getSpecificOverriddenValue(context, PreferenceData.DEFAULT_TIMER_RINGTONE.getValue(context, ""), id));
    }

    /**
     * Moves this TimerData's preferences to another "id".
     *
     * @param id            The new id to be assigned
     * @param context       An active context instance.
     */
    public void onIdChanged(int id, Context context) {
        PreferenceData.TIMER_DURATION.setValue(context, duration, id);
        PreferenceData.TIMER_END_TIME.setValue(context, endTime, id);
        PreferenceData.TIMER_VIBRATE.setValue(context, isVibrate, id);
        PreferenceData.TIMER_SOUND.setValue(context, sound != null ? sound.toString() : null, id);
        onRemoved(context);
        this.id = id;
        if (isSet())
            set(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
    }

    /**
     * Removes this TimerData's preferences.
     *
     * @param context       An active context instance.
     */
    public void onRemoved(Context context) {
        cancel(context, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        PreferenceData.TIMER_DURATION.setValue(context, null, id);
        PreferenceData.TIMER_END_TIME.setValue(context, null, id);
        PreferenceData.TIMER_VIBRATE.setValue(context, null, id);
        PreferenceData.TIMER_SOUND.setValue(context, null, id);
    }

    /**
     * Decides if the Timer has been set or should be ignored.
     *
     * @return              True if the timer should go off at some time in the future.
     */
    public boolean isSet() {
        return endTime > System.currentTimeMillis();
    }

    /**
     * Get the remaining amount of milliseconds before the timer should go off. This
     * may return a negative number.
     *
     * @return              The amount of milliseconds before the timer should go off.
     */
    public long getRemainingMillis() {
        return Math.max(endTime - System.currentTimeMillis(), 0);
    }

    /**
     * The total length of the timer.
     *
     * @return              The total length of the timer, in milliseconds.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set the duration of the timer.
     *
     * @param duration      The total length of the timer, in milliseconds.
     * @param context       An active Context instance.
     */
    public void setDuration(long duration, Context context) {
        this.duration = duration;
        PreferenceData.TIMER_DURATION.setValue(context, duration, id);
    }

    /**
     * Set whether the timer should vibrate when it goes off.
     *
     * @param context       An active Context instance.
     * @param isVibrate     Whether the timer should vibrate.
     */
    public void setVibrate(Context context, boolean isVibrate) {
        this.isVibrate = isVibrate;
        PreferenceData.TIMER_VIBRATE.setValue(context, isVibrate, id);
    }

    /**
     * Return whether the timer has a sound or not.
     *
     * @return              A boolean defining whether a sound has been set
     *                      for the timer.
     */
    public boolean hasSound() {
        return sound != null;
    }

    /**
     * Get the [SoundData](./SoundData) sound specified for the timer.
     *
     * @return              An instance of SoundData describing the sound that
     *                      the timer should make (or null).
     */
    @Nullable
    public SoundData getSound() {
        return sound;
    }

    /**
     * Set the sound that the timer should make.
     *
     * @param context       An active context instance.
     * @param sound         A [SoundData](./SoundData) defining the sound that
     *                      the timer should make.
     */
    public void setSound(Context context, SoundData sound) {
        this.sound = sound;
        PreferenceData.TIMER_SOUND.setValue(context, sound != null ? sound.toString() : null, id);
    }

    /**
     * Set the next time for the timer to ring.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager to schedule the timer on.
     */
    public void set(Context context, AlarmManager manager) {
        endTime = System.currentTimeMillis() + duration;
        setAlarm(context, manager);

        PreferenceData.TIMER_END_TIME.setValue(context, endTime, id);
    }

    /**
     * Schedule a time for the alert to ring at.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager to schedule the alert on.
     */
    public void setAlarm(Context context, AlarmManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            manager.setExact(AlarmManager.RTC_WAKEUP, endTime, getIntent(context));
        else manager.set(AlarmManager.RTC_WAKEUP, endTime, getIntent(context));
    }

    /**
     * Cancel the pending alert.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager that the alert was scheduled on.
     */
    public void cancel(Context context, AlarmManager manager) {
        endTime = 0;
        manager.cancel(getIntent(context));

        PreferenceData.TIMER_END_TIME.setValue(context, endTime, id);
    }

    /**
     * The intent to fire when the alert should ring.
     *
     * @param context       An active context instance.
     * @return              A PendingIntent that will open the alert screen.
     */
    private PendingIntent getIntent(Context context) {
        Intent intent = new Intent(context, TimerReceiver.class);
        intent.putExtra(TimerReceiver.EXTRA_TIMER_ID, id);
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeLong(duration);
        parcel.writeLong(endTime);
        parcel.writeByte((byte) (isVibrate ? 1 : 0));
        parcel.writeByte((byte) (sound != null ? 1 : 0));
        if (sound != null)
            parcel.writeString(sound.toString());
    }

    protected TimerData(Parcel in) {
        id = in.readInt();
        duration = in.readLong();
        endTime = in.readLong();
        isVibrate = in.readByte() != 0;
        if (in.readByte() == 1)
            sound = SoundData.fromString(in.readString());
    }

    public static final Creator<TimerData> CREATOR = new Creator<TimerData>() {
        @Override
        public TimerData createFromParcel(Parcel in) {
            return new TimerData(in);
        }

        @Override
        public TimerData[] newArray(int size) {
            return new TimerData[size];
        }
    };
}
