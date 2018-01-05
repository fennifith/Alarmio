package james.alarmio.data;

import io.reactivex.annotations.Nullable;
import james.alarmio.Alarmio;

public class SoundData {

    private static final String SEPARATOR = ":AlarmioSoundData:";
    private static final int PREVIEW_DURATION = 5000;

    private String name;
    private String url;

    public SoundData(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void play(Alarmio alarmio) {

    }

    public void pause(Alarmio alarmio) {

    }

    public void preview(Alarmio alarmio) {

    }

    public void isPlaying() {

    }

    @Override
    public String toString() {
        return name + SEPARATOR + url;
    }

    @Nullable
    public static SoundData fromString(String string) {
        if (string.contains(SEPARATOR)) {
            String[] data = string.split(SEPARATOR);
            if (data.length == 2 && data[0].length() > 0 && data[1].length() > 0)
                return new SoundData(data[0], data[1]);
        }

        return null;
    }
}
