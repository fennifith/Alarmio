package james.alarmio.data;

import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import io.reactivex.annotations.Nullable;
import james.alarmio.Alarmio;

public class SoundData {

    private static final String SEPARATOR = ":AlarmioSoundData:";
    private static final int PREVIEW_DURATION = 5000;

    private String name;
    private String url;

    private Ringtone ringtone;

    public SoundData(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public SoundData(String name, String url, Ringtone ringtone) {
        this(name, url);
        this.ringtone = ringtone;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void play(Alarmio alarmio) {
        if (url.startsWith("content://")) {
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(alarmio, Uri.parse(url));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build());
                }
            }

            alarmio.playRingtone(ringtone);
        }
    }

    public void stop(Alarmio alarmio) {
        if (ringtone != null)
            ringtone.stop();
    }

    public void preview(Alarmio alarmio) {
        if (url.startsWith("content://")) {
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(alarmio, Uri.parse(url));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());
                }
            }

            alarmio.playRingtone(ringtone);
        }
    }

    public boolean isPlaying() {
        if (ringtone != null)
            return ringtone.isPlaying();
        else return false;
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
