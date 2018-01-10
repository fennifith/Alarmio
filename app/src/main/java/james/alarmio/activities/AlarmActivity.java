package james.alarmio.activities;

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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;

import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.data.AlarmData;
import james.alarmio.data.TimerData;
import james.alarmio.utils.FormatUtils;

public class AlarmActivity extends AestheticActivity implements View.OnTouchListener {

    public static final String EXTRA_ALARM = "james.alarmio.AlarmActivity.EXTRA_ALARM";
    public static final String EXTRA_TIMER = "james.alarmio.AlarmActivity.EXTRA_TIMER";

    private TextView date;
    private TextView time;
    private ImageView snooze;
    private ImageView dismiss;
    private FloatingActionButton fab;

    private Alarmio alarmio;
    private Vibrator vibrator;

    private boolean snoozeSelected;
    private boolean dismissSelected;
    private float firstX;

    private boolean isAlarm;
    private long triggerTime;
    private AlarmData alarm;
    private TimerData timer;

    private Handler handler;
    private Runnable runnable;
    private boolean isWoken;
    private PowerManager.WakeLock wakeLock;

    private Disposable textColorPrimarySubscription;
    private Disposable isDarkSubscription;

    private boolean isDark;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarmio = (Alarmio) getApplicationContext();

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        snooze = findViewById(R.id.snooze);
        dismiss = findViewById(R.id.dismiss);
        fab = findViewById(R.id.fab);

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        snooze.setColorFilter(integer);
                        dismiss.setColorFilter(integer);
                    }
                });

        isDarkSubscription = Aesthetic.get()
                .isDark()
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        isDark = aBoolean;
                    }
                });

        fab.setOnTouchListener(this);
        fab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                firstX = fab.getX();
                fab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        isAlarm = getIntent().hasExtra(EXTRA_ALARM);
        if (isAlarm) {
            alarm = getIntent().getParcelableExtra(EXTRA_ALARM);
            if (alarm.hasSound())
                alarm.getSound().play(alarmio);
        } else if (getIntent().hasExtra(EXTRA_TIMER)) {
            timer = getIntent().getParcelableExtra(EXTRA_TIMER);
        } else finish();

        date.setText(FormatUtils.format(new Date(), FormatUtils.FORMAT_DATE + ", " + FormatUtils.getShortFormat(this)));

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        triggerTime = System.currentTimeMillis();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                String text = FormatUtils.formatMillis(System.currentTimeMillis() - triggerTime);
                time.setText(String.format("-%s", text.substring(0, text.length() - 3)));

                if (alarm != null) {
                    if (alarm.isVibrate && !isWoken) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        else vibrator.vibrate(500);
                    }

                    if (alarm.hasSound() && !alarm.getSound().isPlaying(alarmio)) {
                        alarm.getSound().play(alarmio);
                    }
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
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
    protected void onResume() {
        super.onResume();
        alarmio.onActivityResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textColorPrimarySubscription != null && isDarkSubscription != null) {
            textColorPrimarySubscription.dispose();
            isDarkSubscription.dispose();
        }

        if (handler != null)
            handler.removeCallbacks(runnable);

        if (alarm != null && alarm.hasSound() && alarm.getSound().isPlaying(alarmio))
            alarm.getSound().stop(alarmio);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        finish();
        startActivity(new Intent(intent));
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                if (isAlarm)
                    snooze.setVisibility(View.VISIBLE);
                dismiss.setVisibility(View.VISIBLE);
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = motionEvent.getRawX() - (view.getWidth() / 2);
                view.setX(x);
                boolean snoozeSelected = x <= snooze.getX() + snooze.getWidth();
                boolean dismissSelected = x >= dismiss.getX() - dismiss.getWidth();
                if (snoozeSelected != this.snoozeSelected || dismissSelected != this.dismissSelected) {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    this.snoozeSelected = snoozeSelected;
                    this.dismissSelected = dismissSelected;
                    snooze.setPressed(snoozeSelected);
                    dismiss.setPressed(dismissSelected);
                }
                break;
            case MotionEvent.ACTION_UP:
                view.animate().x(firstX).start();
                snooze.setVisibility(View.INVISIBLE);
                dismiss.setVisibility(View.INVISIBLE);
                if (this.snoozeSelected && isAlarm && alarm != null) {
                    snooze.setPressed(false);

                    final int[] minutes = new int[]{2, 5, 10, 20, 30, 60};
                    CharSequence[] names = new CharSequence[minutes.length + 1];
                    for (int i = 0; i < minutes.length; i++) {
                        names[i] = FormatUtils.formatUnit(AlarmActivity.this, minutes[i]);
                    }

                    names[minutes.length] = getString(R.string.title_snooze_custom);

                    new AlertDialog.Builder(AlarmActivity.this, isDark ? R.style.Theme_AppCompat_Dialog_Alert : R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setItems(names, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which < minutes.length) {
                                        Date time = alarm.snooze(AlarmActivity.this, (AlarmManager) getSystemService(Context.ALARM_SERVICE), minutes[which]);
                                        Toast.makeText(AlarmActivity.this, String.format(getString(R.string.msg_snoozed_until), FormatUtils.formatShort(AlarmActivity.this, time)), Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        //TODO: select time
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

                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                } else if (this.dismissSelected) {
                    dismiss.setPressed(false);
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    finish();
                }
                break;
        }
        return false;
    }
}
