package james.alarmio.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.views.ProgressTextView;

public class StopwatchFragment extends BaseFragment {

    private ImageView back;
    private ImageView reset;
    private ImageView share;
    private TextView lap;
    private FloatingActionButton toggle;
    private ProgressTextView time;
    private LinearLayout lapsLayout;

    private Handler handler;
    private Runnable runnable;

    private long startTime, pauseTime, stopTime;
    private List<Long> laps;
    private long lastLapTime;
    private long lastLapDiff;
    private boolean isRunning;

    private int textColorPrimary;

    private Disposable textColorPrimarySubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        back = view.findViewById(R.id.back);
        reset = view.findViewById(R.id.reset);
        share = view.findViewById(R.id.share);
        lap = view.findViewById(R.id.lap);
        toggle = view.findViewById(R.id.toggle);
        time = view.findViewById(R.id.time);
        lapsLayout = view.findViewById(R.id.laps);

        laps = new ArrayList<>();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    long currentTime = System.currentTimeMillis() - startTime;
                    time.setText(formatMillis(currentTime));
                    time.setProgress(currentTime - (lastLapTime == 0 ? currentTime : lastLapTime));
                    handler.postDelayed(this, 10);
                } else time.setText(formatMillis(startTime == 0 ? 0 : stopTime - startTime));
            }
        };

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRunning) {
                    stopTime = System.currentTimeMillis();
                    isRunning = false;
                }

                startTime = 0;
                pauseTime = 0;
                handler.post(runnable);
                laps.clear();
                lapsLayout.removeAllViews();
                lastLapTime = 0;
                lastLapDiff = 0;
                time.setMaxProgress(0);
                time.setReferenceProgress(0);
                reset.setVisibility(View.INVISIBLE);
                lap.setVisibility(View.INVISIBLE);
                share.setVisibility(View.GONE);
            }
        });

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTime = System.currentTimeMillis();
                isRunning = !isRunning;
                if (isRunning) {
                    if (startTime == 0)
                        startTime = System.currentTimeMillis();
                    else if (pauseTime != 0)
                        startTime += System.currentTimeMillis() - pauseTime;
                    handler.post(runnable);

                    reset.setVisibility(View.INVISIBLE);
                    lap.setVisibility(View.VISIBLE);
                    share.setVisibility(View.GONE);
                    toggle.setImageResource(R.drawable.ic_pause);
                } else {
                    pauseTime = System.currentTimeMillis();
                    reset.setVisibility(View.VISIBLE);
                    lap.setVisibility(View.GONE);
                    share.setVisibility(View.VISIBLE);
                    toggle.setImageResource(R.drawable.ic_play);
                }
            }
        });

        lap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long lapTime = System.currentTimeMillis() - startTime;
                long lapDiff = lapTime - lastLapTime;
                laps.add(lapDiff);
                if (lastLapTime == 0)
                    time.setMaxProgress(lapDiff);
                else time.setReferenceProgress(lapDiff);
                lastLapTime = lapTime;

                LinearLayout layout = new LinearLayout(getContext());

                TextView number = new TextView(getContext());
                number.setText(String.valueOf(laps.size()));
                number.setTextColor(textColorPrimary);
                layout.addView(number);

                TextView time = new TextView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1;
                time.setLayoutParams(layoutParams);
                time.setGravity(GravityCompat.END);
                time.setText(formatMillis(lapTime));
                time.setTextColor(textColorPrimary);
                layout.addView(time);

                lapsLayout.addView(layout, 0);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textColorPrimary = integer;
                        back.setColorFilter(integer);
                        reset.setColorFilter(integer);
                        lap.setTextColor(integer);
                        share.setColorFilter(integer);

                        for (int i = 0; i < lapsLayout.getChildCount(); i++) {
                            LinearLayout layout = (LinearLayout) lapsLayout.getChildAt(i);
                            for (int i2 = 0; i2 < layout.getChildCount(); i2++) {
                                ((TextView) layout.getChildAt(i2)).setTextColor(integer);
                            }
                        }
                    }
                });

        handler.post(runnable);
        return view;
    }

    @Override
    public void onDestroyView() {
        textColorPrimarySubscription.dispose();
        time.unsubscribe();
        super.onDestroyView();
    }

    private String formatMillis(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);
        long micros = TimeUnit.MILLISECONDS.toMicros(millis) % TimeUnit.SECONDS.toMicros(1) / 10000;

        if (hours > 0)
            return String.format(Locale.getDefault(), "%dh %02dm %02ds %02d", hours, minutes, seconds, micros);
        else if (minutes > 0)
            return String.format(Locale.getDefault(), "%dm %02ds %02d", minutes, seconds, micros);
        else return String.format(Locale.getDefault(), "%ds %02d", seconds, micros);
    }
}
