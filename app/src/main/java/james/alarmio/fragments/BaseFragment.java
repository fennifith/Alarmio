package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import james.alarmio.Alarmio;

public abstract class BaseFragment extends Fragment implements Alarmio.AlarmioListener {

    private Alarmio alarmio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmio = (Alarmio) getContext().getApplicationContext();
        alarmio.addListener(this);
    }

    @Override
    public void onDestroy() {
        alarmio.removeListener(this);
        super.onDestroy();
    }

    Alarmio getAlarmio() {
        return alarmio;
    }

    public void notifyDataSetChanged() {
    }

    @Override
    public void onAlarmsChanged() {
    }

    @Override
    public void onTimersChanged() {
    }
}
