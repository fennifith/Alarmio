package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import james.alarmio.R;
import james.alarmio.views.DigitalClockView;

public class ClockFragment extends BasePagerFragment {

    public static final String EXTRA_TIME_ZONE = "timezone";

    private DigitalClockView timeView;
    private TextView timezoneView;

    private String timezone;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clock, container, false);
        timeView = view.findViewById(R.id.timeView);
        timezoneView = view.findViewById(R.id.timezone);

        if (getArguments() != null && getArguments().containsKey(EXTRA_TIME_ZONE)) {
            timezone = getArguments().getString(EXTRA_TIME_ZONE);
            timeView.setTimezone(timezone);
            timezoneView.setText(timezone);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        timeView.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public String getTitle() {
        return timezone;
    }

    @Override
    public void notifyDataSetChanged() {

    }
}
