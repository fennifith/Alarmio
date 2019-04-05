package me.jfenn.alarmio.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import io.reactivex.disposables.Disposable;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.services.StopwatchService;
import me.jfenn.alarmio.utils.FormatUtils;
import me.jfenn.alarmio.views.ProgressTextView;

public class StopwatchFragment extends BaseFragment implements StopwatchService.Listener, ServiceConnection {

    private ImageView back;
    private ImageView reset;
    private ImageView share;
    private TextView lap;
    private FloatingActionButton toggle;
    private ProgressTextView time;
    private LinearLayout lapsLayout;

    private int textColorPrimary;
    private Disposable textColorPrimarySubscription;

    private StopwatchService service;

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

        reset.setOnClickListener(v -> {
            if (service != null)
                service.reset();
        });
        reset.setClickable(false);

        toggle.setOnClickListener(v -> {
            if (service != null)
                service.toggle();
        });

        lap.setOnClickListener(v -> {
            if (service != null)
                service.lap();
        });

        share.setOnClickListener(v -> {
            if (service != null) {
                String time = FormatUtils.formatMillis(service.getElapsedTime());
                StringBuilder content = new StringBuilder().append(getContext().getString(R.string.title_time, time)).append("\n");
                long total = 0;
                List<Long> laps = service.getLaps();
                for (int i = 0; i < laps.size(); i++) {
                    long lapTime = laps.get(i);
                    total += lapTime;

                    content.append(getContext().getString(R.string.title_lap_number, laps.size() - i))
                            .append("    \t")
                            .append(getContext().getString(R.string.title_lap_time, FormatUtils.formatMillis(lapTime)))
                            .append("    \t")
                            .append(getContext().getString(R.string.title_total_time, FormatUtils.formatMillis(total)));

                    if (i < laps.size() - 1)
                        content.append("\n");
                }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.title_stopwatch_share, getContext().getString(R.string.app_name), time));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, content.toString());
                startActivity(Intent.createChooser(sharingIntent, getContext().getString(R.string.title_share_results)));
            }
        });

        back.setOnClickListener(v -> getFragmentManager().popBackStack());

        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> {
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
                });

        Intent intent = new Intent(getContext(), StopwatchService.class);
        getContext().startService(intent);
        getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        return view;
    }

    @Override
    public void onDestroyView() {
        textColorPrimarySubscription.dispose();
        time.unsubscribe();
        if (service != null) {
            service.setListener(null);
            boolean isRunning = service.isRunning();
            getContext().unbindService(this);
            if (!isRunning)
                getContext().stopService(new Intent(getContext(), StopwatchService.class));
        }
        super.onDestroyView();
    }

    @Override
    public void onStateChanged(boolean isRunning) {
        if (isRunning) {
            reset.setClickable(false);
            reset.animate().alpha(0).start();
            lap.setVisibility(View.VISIBLE);
            share.setVisibility(View.GONE);

            AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_play_to_pause);
            if (drawable != null) {
                toggle.setImageDrawable(drawable);
                drawable.start();
            } else toggle.setImageResource(R.drawable.ic_pause);
        } else {
            if (service.getElapsedTime() > 0) {
                reset.setClickable(true);
                reset.animate().alpha(1).start();
                share.setVisibility(View.VISIBLE);
            } else share.setVisibility(View.INVISIBLE);

            lap.setVisibility(View.GONE);

            AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.ic_pause_to_play);
            if (drawable != null) {
                toggle.setImageDrawable(drawable);
                drawable.start();
            } else toggle.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public void onReset() {
        lapsLayout.removeAllViews();
        time.setMaxProgress(0);
        time.setReferenceProgress(0);
        reset.setClickable(false);
        reset.setAlpha(0f);
        lap.setVisibility(View.INVISIBLE);
        share.setVisibility(View.GONE);
    }

    @Override
    public void onTick(long currentTime, String text) {
        if (service != null) {
            time.setText(text);
            time.setProgress(currentTime - (service.getLastLapTime() == 0 ? currentTime : service.getLastLapTime()));
        }
    }

    @Override
    public void onLap(int lapNum, long lapTime, long lastLapTime, long lapDiff) {
        if (lastLapTime == 0)
            time.setMaxProgress(lapDiff);
        else time.setReferenceProgress(lapDiff);

        LinearLayout layout = new LinearLayout(getContext());

        TextView number = new TextView(getContext());
        number.setText(getString(R.string.title_lap_number, lapNum));
        number.setTextColor(textColorPrimary);
        layout.addView(number);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;

        TextView lap = new TextView(getContext());
        lap.setLayoutParams(layoutParams);
        lap.setGravity(GravityCompat.END);
        lap.setText(getString(R.string.title_lap_time, FormatUtils.formatMillis(lapDiff)));
        lap.setTextColor(textColorPrimary);
        layout.addView(lap);

        TextView total = new TextView(getContext());
        total.setLayoutParams(layoutParams);
        total.setGravity(GravityCompat.END);
        total.setText(getString(R.string.title_total_time, FormatUtils.formatMillis(lapTime)));
        total.setTextColor(textColorPrimary);
        layout.addView(total);

        lapsLayout.addView(layout, 0);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder != null && iBinder instanceof StopwatchService.LocalBinder) {
            service = ((StopwatchService.LocalBinder) iBinder).getService();
            onStateChanged(service.isRunning());
            onTick(0, "0s 00");
            service.setListener(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

}
