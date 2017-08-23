package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import james.alarmio.Alarmio;

public abstract class BaseFragment extends Fragment {

    private Alarmio alarmio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmio = (Alarmio) getContext().getApplicationContext();
    }

    Alarmio getAlarmio() {
        return alarmio;
    }

    public abstract void notifyDataSetChanged();

}
