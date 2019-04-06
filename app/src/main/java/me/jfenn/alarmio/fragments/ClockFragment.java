package me.jfenn.alarmio.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.TimeZone;

import androidx.annotation.Nullable;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.interfaces.ContextFragmentInstantiator;
import me.jfenn.alarmio.views.DigitalClockView;

public class ClockFragment extends BasePagerFragment {

    public static final String EXTRA_TIME_ZONE = "me.jfenn.alarmio.fragments.ClockFragment.EXTRA_TIME_ZONE";

    private String timezone;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clock, container, false);
        DigitalClockView clockView = view.findViewById(R.id.timeView);
        TextView timezoneView = view.findViewById(R.id.timezone);

        if (getArguments() != null && getArguments().containsKey(EXTRA_TIME_ZONE)) {
            timezone = getArguments().getString(EXTRA_TIME_ZONE);
            if (timezone != null) {
                clockView.setTimezone(timezone);
                if (!timezone.equals(TimeZone.getDefault().getID())) {
                    timezoneView.setText(String.format(
                            "%s\n%s",
                            timezone.replaceAll("_", " "),
                            TimeZone.getTimeZone(timezone).getDisplayName()
                    ));
                }
            }
        }

        return view;
    }

    @Override
    public String getTitle(Context context) {
        return timezone;
    }

    public static class Instantiator extends ContextFragmentInstantiator {

        @Nullable
        private String timezone;

        public Instantiator(Context context, @Nullable String timezone) {
            super(context);
            this.timezone = timezone;
        }

        @Override
        public String getTitle(Context context, int position) {
            return timezone;
        }

        @Nullable
        @Override
        public BasePagerFragment newInstance(int position) {
            Bundle args = new Bundle();
            args.putString(EXTRA_TIME_ZONE, timezone);
            ClockFragment fragment = new ClockFragment();
            fragment.setArguments(args);
            return fragment;
        }
    }

}
