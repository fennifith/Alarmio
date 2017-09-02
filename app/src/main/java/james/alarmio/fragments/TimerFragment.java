package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TimerFragment extends BaseFragment {

    public static final String EXTRA_TIMER = "james.alarmio.TimerFragment.EXTRA_TIMER";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_TIMER)) {

        } else {

        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
