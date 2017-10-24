package james.alarmio.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;

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

    private boolean snoozeSelected;
    private boolean dismissSelected;
    private float firstX;

    private boolean isAlarm;
    private long triggerTime;
    private AlarmData alarm;
    private TimerData timer;

    private Handler handler;
    private Runnable runnable;

    private Disposable textColorPrimarySubscription;

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
        } else if (getIntent().hasExtra(EXTRA_TIMER)) {
            timer = getIntent().getParcelableExtra(EXTRA_TIMER);
        } else finish();

        triggerTime = System.currentTimeMillis();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                String text = FormatUtils.formatMillis(System.currentTimeMillis() - triggerTime);
                time.setText(String.format("-%s", text.substring(0, text.length() - 3)));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmio.onActivityResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textColorPrimarySubscription != null) {
            textColorPrimarySubscription.dispose();
        }
        if (handler != null)
            handler.removeCallbacks(runnable);
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
