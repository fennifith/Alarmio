package me.jfenn.alarmio.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import me.jfenn.alarmio.Alarmio;

public enum PreferenceData {
    INFO_BACKGROUND_PERMISSIONS(false),
    THEME(Alarmio.THEME_DAY_NIGHT),
    BACKGROUND_IMAGE("https://jfenn.me/images/headers/snowytrees.jpg"),
    RINGING_BACKGROUND_IMAGE(true),
    DAY_AUTO(true),
    DAY_START(6), //hours TODO: change to minutes
    DAY_END(18), //hours
    ALARM_LENGTH(0),
    TIMER_LENGTH(0),
    DEFAULT_ALARM_RINGTONE(null),
    DEFAULT_TIMER_RINGTONE(null),
    SLEEP_REMINDER(true),
    SLEEP_REMINDER_TIME(25200000L), //milliseconds
    SLOW_WAKE_UP(true),
    SLOW_WAKE_UP_TIME(300000L), //milliseconds
    ALARM_NAME("%d/ALARM_NAME", null),
    ALARM_TIME("%d/ALARM_TIME", (long) 0),
    ALARM_ENABLED("%d/ALARM_ENABLED", true),
    ALARM_DAY_ENABLED("%1$d/ALARM_DAY/%2$d/ENABLED", false),
    ALARM_VIBRATE("%d/ALARM_VIBRATE", true),
    ALARM_SOUND("%d/ALARM_SOUND", ""),
    TIMER_DURATION("%d/TIMER_DURATION", 600000),
    TIMER_END_TIME("%d/TIMER_END_TIME", 0),
    TIMER_VIBRATE("%d/TIMER_VIBRATE", true),
    TIMER_SOUND("%d/TIMER_SOUND", ""),
    TIME_ZONE_ENABLED("%s/TIME_ZONE_ENABLED", false);

    private String name;
    private Object defaultValue;

    PreferenceData(Object value) {
        name = name();
        defaultValue = value;
    }

    PreferenceData(String name, Object value) {
        this.name = name;
        defaultValue = value;
    }

    public String getName(@Nullable Object... args) {
        if (args != null && args.length > 0)
            return String.format(name, (Object[]) args);
        else return name;
    }

    public <T> T getDefaultValue() {
        try {
            return (T) defaultValue;
        } catch (ClassCastException e) {
            throw new TypeMismatchException(this);
        }
    }

    public <T> T getValue(Context context) {
        return getSpecificOverriddenValue(context, (T) getDefaultValue(), (Object[]) null);
    }

    public <T> T getValue(Context context, @Nullable T defaultValue) {
        return getSpecificOverriddenValue(context, defaultValue, (Object[]) null);
    }

    public <T> T getSpecificValue(Context context, @Nullable Object... args) {
        return getSpecificOverriddenValue(context, (T) getDefaultValue(), args);
    }

    public <T> T getSpecificOverriddenValue(Context context, @Nullable T defaultValue, @Nullable Object... args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String name = getName(args);
        T type = defaultValue != null ? defaultValue : (T) getDefaultValue();

        if (type instanceof Object[] && prefs.contains(name + "-length")) {
            try {
                int length = prefs.getInt(name + "-length", 0);

                Object[] array;
                if (type instanceof Boolean[])
                    array = new Boolean[length];
                else if (type instanceof Integer[])
                    array = new Integer[length];
                else if (type instanceof String[])
                    array = new String[length];
                else throw new TypeMismatchException(this);

                for (int i = 0; i < array.length; i++) {
                    if (array instanceof Boolean[])
                        array[i] = prefs.contains(name + "-" + i) ? prefs.getBoolean(name + "-" + i, false) : null;
                    else if (array instanceof Integer[])
                        array[i] = prefs.contains(name + "-" + i) ? prefs.getInt(name + "-" + i, 0) : null;
                    else if (array instanceof String[])
                        array[i] = prefs.getString(name + "-" + i, "");
                    else throw new TypeMismatchException(this);
                }

                return (T) array;
            } catch (ClassCastException e) {
                throw new TypeMismatchException(this, type.getClass());
            }
        } else if (prefs.contains(name)) {
            try {
                if (type instanceof Boolean)
                    return (T) new Boolean(prefs.getBoolean(name, (Boolean) defaultValue));
                else if (type instanceof Long)
                    return (T) new Long(prefs.getLong(name, (Long) defaultValue));
                else if (type instanceof Integer)
                    return (T) new Integer(prefs.getInt(name, (Integer) defaultValue));
                else if (type instanceof String)
                    return (T) prefs.getString(name, (String) defaultValue);
            } catch (ClassCastException e) {
                // error prevention
                if (type instanceof Long) {
                    try {
                        return (T) new Long(prefs.getInt(name, ((Long) defaultValue).intValue()));
                    } catch (ClassCastException ignored) {
                    }
                } else if (type instanceof Integer) {
                    try {
                        return (T) new Integer((int) prefs.getLong(name, (Integer) defaultValue));
                    } catch (ClassCastException ignored) {
                    }
                }

                throw new TypeMismatchException(this, type.getClass());
            }
        }

        return defaultValue;
    }

    public <T> void setValue(Context context, @Nullable T value) {
        setValue(context, value, (Object[]) null);
    }

    public <T> void setValue(Context context, @Nullable T value, @Nullable Object... args) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String name = getName(args);

        if (value == null)
            editor.remove(name + (defaultValue != null && defaultValue instanceof Object[] ? "-length" : ""));
        else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;

            for (int i = 0; i < array.length; i++) {
                Object item = array[i];
                if (item instanceof Boolean)
                    editor.putBoolean(name + "-" + i, (boolean) item);
                else if (item instanceof Integer)
                    editor.putInt(name + "-" + i, (int) item);
                else if (item instanceof String)
                    editor.putString(name + "-" + i, (String) item);
                else throw new TypeMismatchException(this);
            }

            editor.putInt(name + "-length", array.length);
        } else {
            if (value instanceof Boolean)
                editor.putBoolean(name, (Boolean) value);
            else if (value instanceof Long)
                editor.putLong(name, (Long) value);
            else if (value instanceof Integer)
                editor.putInt(name, (Integer) value);
            else if (value instanceof String)
                editor.putString(name, (String) value);
            else throw new TypeMismatchException(this);
        }

        editor.apply();
    }

    public static class TypeMismatchException extends RuntimeException {

        public TypeMismatchException(PreferenceData data) {
            this(data, null);
        }

        public TypeMismatchException(PreferenceData data, Class expectedType) {
            super("Wrong type used for \"" + data.name() + "\""
                    + (data.defaultValue != null ? ": expected " + data.defaultValue.getClass().getName()
                    + (expectedType != null ? ", got " + expectedType.getName() : "") : ""));
        }

    }

}
