package me.jfenn.alarmio.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.jfenn.alarmio.Alarmio;

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
        alarmio = null;
        super.onDestroy();
    }

    @Nullable
    protected Alarmio getAlarmio() {
        return alarmio;
    }

    public void notifyDataSetChanged() {
        // Update the info displayed in the fragment.
    }

    @Override
    public void onAlarmsChanged() {
        // Update any alarm-dependent data.
    }

    @Override
    public void onTimersChanged() {
        // Update any timer-dependent data.
    }
}
