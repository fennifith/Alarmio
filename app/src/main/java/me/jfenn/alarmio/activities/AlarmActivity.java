package me.jfenn.alarmio.activities;

import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.AlarmData;
import me.jfenn.alarmio.data.PreferenceData;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.data.TimerData;
import me.jfenn.alarmio.dialogs.TimeChooserDialog;
import me.jfenn.alarmio.services.SleepReminderService;
import me.jfenn.alarmio.utils.FormatUtils;
import me.jfenn.alarmio.utils.ImageUtils;
import me.jfenn.slideactionview.SlideActionListener;
import me.jfenn.slideactionview.SlideActionView;

public class AlarmActivity extends AestheticActivity implements SlideActionListener {

    public static final String EXTRA_ALARM = "james.alarmio.AlarmActivity.EXTRA_ALARM";
    public static final String EXTRA_TIMER = "james.alarmio.AlarmActivity.EXTRA_TIMER";

    private View overlay;
    private TextView date;
    private TextView time;
    private SlideActionView actionView;

    private Alarmio alarmio;
    private Vibrator vibrator;

    private boolean isAlarm;
    private long triggerMillis;
    private AlarmData alarm;
    private TimerData timer;
    private SoundData sound;
    private boolean isVibrate;

    private boolean isSlowWake;
    private long slowWakeMillis;

    private Handler handler;
    private Runnable runnable;
    private boolean isWoken;
    private PowerManager.WakeLock wakeLock;

    private Disposable textColorPrimaryInverseSubscription;
    private Disposable isDarkSubscription;

    private boolean isDark;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarmio = (Alarmio) getApplicationContext();

        overlay = findViewById(R.id.overlay);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        actionView = findViewById(R.id.slideView);

        textColorPrimaryInverseSubscription = Aesthetic.Companion.get()
                .textColorPrimaryInverse()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        overlay.setBackgroundColor(integer);
                    }
                });

        isDarkSubscription = Aesthetic.Companion.get()
                .isDark()
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        isDark = aBoolean;
                    }
                });

        actionView.setLeftIcon(VectorDrawableCompat.create(getResources(), R.drawable.ic_snooze, getTheme()));
        actionView.setRightIcon(VectorDrawableCompat.create(getResources(), R.drawable.ic_close, getTheme()));
        actionView.setListener(this);

        isSlowWake = PreferenceData.SLOW_WAKE_UP.getValue(this);
        slowWakeMillis = PreferenceData.SLOW_WAKE_UP_TIME.getValue(this);

        isAlarm = getIntent().hasExtra(EXTRA_ALARM);
        if (isAlarm) {
            alarm = getIntent().getParcelableExtra(EXTRA_ALARM);
            isVibrate = alarm.isVibrate;
            if (alarm.hasSound())
                sound = alarm.getSound();
        } else if (getIntent().hasExtra(EXTRA_TIMER)) {
            timer = getIntent().getParcelableExtra(EXTRA_TIMER);
            isVibrate = timer.isVibrate;
            if (timer.hasSound())
                sound = timer.getSound();
        } else finish();

        date.setText(FormatUtils.format(new Date(), FormatUtils.FORMAT_DATE + ", " + FormatUtils.getShortFormat(this)));

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        triggerMillis = System.currentTimeMillis();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - triggerMillis;
                String text = FormatUtils.formatMillis(elapsedMillis);
                time.setText(String.format("-%s", text.substring(0, text.length() - 3)));

                if (isVibrate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    else vibrator.vibrate(500);
                }

                if (sound != null && !sound.isPlaying(alarmio))
                    sound.play(alarmio);

                if (alarm != null && isSlowWake) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = Math.max(0.01f, Math.min(1f, (float) elapsedMillis / slowWakeMillis));
                    getWindow().setAttributes(params);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAGS_CHANGED);
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        if (sound != null)
            sound.play(alarmio);

        SleepReminderService.refreshSleepTime(alarmio);

        if (PreferenceData.RINGING_BACKGROUND_IMAGE.getValue(this))
            ImageUtils.getBackgroundImage((ImageView) findViewById(R.id.background));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textColorPrimaryInverseSubscription != null && isDarkSubscription != null) {
            textColorPrimaryInverseSubscription.dispose();
            isDarkSubscription.dispose();
        }

        stopAnnoyingness();
    }

    private void stopAnnoyingness() {
        if (handler != null)
            handler.removeCallbacks(runnable);

        if (sound != null && sound.isPlaying(alarmio))
            sound.stop(alarmio);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        finish();
        startActivity(new Intent(intent));
    }

    @Override
    public void onSlideLeft() {
        final int[] minutes = new int[]{2, 5, 10, 20, 30, 60};
        CharSequence[] names = new CharSequence[minutes.length + 1];
        for (int i = 0; i < minutes.length; i++) {
            names[i] = FormatUtils.formatUnit(AlarmActivity.this, minutes[i]);
        }

        names[minutes.length] = getString(R.string.title_snooze_custom);

        stopAnnoyingness();
        new AlertDialog.Builder(AlarmActivity.this, isDark ? R.style.Theme_AppCompat_Dialog_Alert : R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < minutes.length) {
                            TimerData timer = alarmio.newTimer();
                            timer.setDuration(TimeUnit.MINUTES.toMillis(minutes[which]), alarmio);
                            timer.setVibrate(AlarmActivity.this, isVibrate);
                            timer.setSound(AlarmActivity.this, sound);
                            timer.set(alarmio, ((AlarmManager) AlarmActivity.this.getSystemService(Context.ALARM_SERVICE)));
                            alarmio.onTimerStarted();

                            finish();
                        } else {
                            TimeChooserDialog timerDialog = new TimeChooserDialog(AlarmActivity.this);
                            timerDialog.setListener(new TimeChooserDialog.OnTimeChosenListener() {
                                @Override
                                public void onTimeChosen(int hours, int minutes, int seconds) {
                                    TimerData timer = alarmio.newTimer();
                                    timer.setVibrate(AlarmActivity.this, isVibrate);
                                    timer.setSound(AlarmActivity.this, sound);
                                    timer.setDuration(TimeUnit.HOURS.toMillis(hours)
                                                    + TimeUnit.MINUTES.toMillis(minutes)
                                                    + TimeUnit.SECONDS.toMillis(seconds),
                                            alarmio);

                                    timer.set(alarmio, ((AlarmManager) getSystemService(Context.ALARM_SERVICE)));
                                    alarmio.onTimerStarted();
                                    finish();
                                }
                            });
                            timerDialog.show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

        overlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    @Override
    public void onSlideRight() {
        overlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        finish();
    }
}
