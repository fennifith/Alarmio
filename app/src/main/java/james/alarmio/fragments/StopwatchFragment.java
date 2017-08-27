package james.alarmio.fragments;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.views.ProgressTextView;

public class StopwatchFragment extends BaseFragment {

    private static final int NOTIFICATION_ID = 247;
    private static final String ACTION_RESET = "james.alarmio.StopwatchFragment.ACTION_RESET";
    private static final String ACTION_TOGGLE = "james.alarmio.StopwatchFragment.ACTION_TOGGLE";
    private static final String ACTION_LAP = "james.alarmio.StopwatchFragment.ACTION_LAP";

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
    private boolean isRunning;

    private String notificationText;
    private NotificationManager notificationManager;
    private NotificationReceiver receiver;

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

        notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        receiver = new NotificationReceiver(this);
        laps = new ArrayList<>();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    long currentTime = System.currentTimeMillis() - startTime;
                    String text = formatMillis(currentTime);
                    time.setText(text);
                    time.setProgress(currentTime - (lastLapTime == 0 ? currentTime : lastLapTime));
                    text = text.substring(0, text.length() - 3);
                    if (notificationText == null || !notificationText.equals(text)) {
                        notificationManager.notify(NOTIFICATION_ID, getNotification(text));
                        notificationText = text;
                    }
                    handler.postDelayed(this, 10);
                } else time.setText(formatMillis(startTime == 0 ? 0 : stopTime - startTime));
            }
        };

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        lap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lap();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = formatMillis(stopTime - startTime);
                StringBuilder content = new StringBuilder().append(getString(R.string.title_time, time)).append("\n");
                long total = 0;
                for (int i = laps.size() - 1; i >= 0; i--) {
                    long lapTime = laps.get(i);
                    total += lapTime;

                    content.append(getString(R.string.title_lap_number, laps.size() - i))
                            .append("    \t")
                            .append(getString(R.string.title_lap_time, formatMillis(lapTime)))
                            .append("    \t")
                            .append(getString(R.string.title_total_time, formatMillis(total)));

                    if (i > 0)
                        content.append("\n");
                }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.title_stopwatch_share, getString(R.string.app_name), time));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, content.toString());
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.title_share_results)));
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESET);
        filter.addAction(ACTION_TOGGLE);
        filter.addAction(ACTION_LAP);
        getContext().registerReceiver(receiver, filter);

        return view;
    }

    private void reset() {
        if (isRunning)
            toggle();

        startTime = 0;
        pauseTime = 0;
        handler.post(runnable);
        laps.clear();
        lapsLayout.removeAllViews();
        lastLapTime = 0;
        time.setMaxProgress(0);
        time.setReferenceProgress(0);
        reset.setVisibility(View.INVISIBLE);
        lap.setVisibility(View.INVISIBLE);
        share.setVisibility(View.GONE);

        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void toggle() {
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

        notificationText = formatMillis(System.currentTimeMillis() - startTime);
        notificationManager.notify(NOTIFICATION_ID, getNotification(notificationText));
    }

    private void lap() {
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

    @Override
    public void onDestroyView() {
        textColorPrimarySubscription.dispose();
        time.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (notificationManager != null && receiver != null) {
            notificationManager.cancel(NOTIFICATION_ID);
            getContext().unregisterReceiver(receiver);
        }
        super.onDestroy();
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

    private Notification getNotification(String time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(new NotificationChannel("stopwatch", getContext().getString(R.string.title_stopwatch), NotificationManager.IMPORTANCE_DEFAULT));

        return new NotificationCompat.Builder(getContext(), "stopwatch")
                .setSmallIcon(R.drawable.ic_stopwatch_notification)
                .setContentTitle(getString(R.string.title_stopwatch))
                .setContentText(time)
                .setDeleteIntent(PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_RESET), 0))
                .addAction(
                        isRunning ? R.drawable.ic_pause_notification : R.drawable.ic_play_notification,
                        isRunning ? "Pause" : "Play",
                        PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_TOGGLE), 0)
                )
                .addAction(
                        R.drawable.ic_lap_notification,
                        "Lap",
                        PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_LAP), 0)
                )
                .build();
    }

    private static class NotificationReceiver extends BroadcastReceiver {

        private WeakReference<StopwatchFragment> fragmentReference;

        public NotificationReceiver(StopwatchFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            StopwatchFragment fragment = fragmentReference.get();
            if (intent != null && intent.getAction() != null && fragment != null) {
                switch (intent.getAction()) {
                    case ACTION_RESET:
                        fragment.reset();
                        break;
                    case ACTION_TOGGLE:
                        fragment.toggle();
                        break;
                    case ACTION_LAP:
                        fragment.lap();
                        break;
                }
            }
        }
    }
}
