package james.alarmio.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.data.TimerData;
import james.alarmio.utils.FormatUtils;
import james.alarmio.views.ProgressTextView;

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
        if (container != null)
            container.removeAllViews();

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
                        FragmentManager manager = getFragmentManager();
                        if (manager != null)
                            manager.popBackStack();
                    }
                }
            }
        };

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAlarmio().removeTimer(timer);
                getFragmentManager().popBackStack();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        handler.post(runnable);

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        back.setColorFilter(integer);
                    }
                });

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
