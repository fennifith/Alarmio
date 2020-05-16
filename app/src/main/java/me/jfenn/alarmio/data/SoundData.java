package me.jfenn.alarmio.data;

import android.media.Ringtone;

import io.reactivex.annotations.Nullable;

public class SoundData {

    private static final String SEPARATOR = ":AlarmioSoundData:";

    public static final String TYPE_RINGTONE = "ringtone";
    public static final String TYPE_RADIO = "radio";

    private String name;
    private String type;
    private String url;

    private Ringtone ringtone;

    public SoundData(String name, String type, String url) {
        this.name = name;
        this.type = type;
        this.url = url;
    }

    public SoundData(String name, String type, String url, Ringtone ringtone) {
        this(name, type, url);
        this.ringtone = ringtone;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Returns an identifier string that can be used to recreate this
     * SoundDate class.
     *
     * @return                  A non-null identifier string.
     */
    @Override
    public String toString() {
        return name + SEPARATOR + type + SEPARATOR + url;
    }

    /**
     * Construct a new instance of SoundData from an identifier string which was
     * (hopefully) created by [toString](#tostring).
     *
     * @param string            A non-null identifier string.
     * @return                  A recreated SoundData instance.
     */
    @Nullable
    public static SoundData fromString(String string) {
        if (string.contains(SEPARATOR)) {
            String[] data = string.split(SEPARATOR);
            if (data.length == 3
                    && data[0].length() > 0 && data[1].length() > 0 && data[2].length() > 0)
                return new SoundData(data[0], data[1], data[2]);
        }

        return null;
    }

    /**
     * Decide if two SoundDatas are equal.
     *
     * @param obj               The object to compare to.
     * @return                  True if the SoundDatas contain the same sound.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj != null && obj instanceof SoundData && ((SoundData) obj).url.equals(url));
    }
}
