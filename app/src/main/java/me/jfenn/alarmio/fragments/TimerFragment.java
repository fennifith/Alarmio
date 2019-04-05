package me.jfenn.alarmio.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.aesthetic.Aesthetic;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import io.reactivex.disposables.Disposable;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.TimerData;
import me.jfenn.alarmio.utils.FormatUtils;
import me.jfenn.alarmio.views.ProgressTextView;

public class TimerFragment extends BaseFragment {

    public static final String EXTRA_TIMER = "james.alarmio.TimerFragment.EXTRA_TIMER";

    private ImageView back;
    private ProgressTextView time;
    private FloatingActionButton stop;

    private Handler handler;
    private Runnable runnable;

    private boolean isRunning = true;

    private TimerData timer;

    private Disposable textColorPrimarySubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        back = view.findViewById(R.id.back);
        time = view.findViewById(R.id.time);
        stop = view.findViewById(R.id.stop);

        timer = getArguments().getParcelable(EXTRA_TIMER);

        time.setMaxProgress(timer.getDuration());

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    if (timer.isSet()) {
                        long remainingMillis = timer.getRemainingMillis();
                        time.setText(FormatUtils.formatMillis(remainingMillis));
                        time.setProgress(timer.getDuration() - remainingMillis);
                        handler.postDelayed(this, 10);
                    } else {
                        try {
                            FragmentManager manager = getFragmentManager();
                            if (manager != null)
                                manager.popBackStack();
                        } catch (IllegalStateException e) {
                            handler.postDelayed(this, 100);
                        }
                    }
                }
            }
        };

        stop.setOnClickListener(view1 -> {
            getAlarmio().removeTimer(timer);
            getFragmentManager().popBackStack();
        });

        back.setOnClickListener(view12 -> getFragmentManager().popBackStack());

        handler.post(runnable);

        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> back.setColorFilter(integer));

        return view;
    }

    @Override
    public void onDestroyView() {
        isRunning = false;
        textColorPrimarySubscription.dispose();
        time.unsubscribe();
        super.onDestroyView();
    }
}
