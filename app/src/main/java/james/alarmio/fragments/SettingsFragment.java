package james.alarmio.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.utils.SunriseView;

public class SettingsFragment extends BasePagerFragment {

    private AppCompatCheckBox sunriseAutoSwitch;
    private SunriseView sunriseView;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sunriseAutoSwitch = view.findViewById(R.id.sunriseAutoSwitch);
        sunriseView = view.findViewById(R.id.sunriseView);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        sunriseAutoSwitch.setChecked(getAlarmio().isDayAuto());
        sunriseAutoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(Alarmio.PREF_DAY_AUTO, b).apply();
                if (b && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 954);
                    sunriseAutoSwitch.setChecked(false);
                } else sunriseView.invalidate();
                getAlarmio().onActivityResume();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        if (sunriseView != null) {
            sunriseView.unsubscribe();
        }
        super.onDestroyView();
    }

    @Override
    public String getTitle() {
        return "Settings";
    }

    @Override
    public void onTimersChanged() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 954) {
            sunriseAutoSwitch.setChecked(getAlarmio().isDayAuto());
            sunriseView.invalidate();
        }
    }
}
