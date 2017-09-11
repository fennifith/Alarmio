package james.alarmio.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.utils.FormatUtils;
import james.alarmio.utils.SunriseView;

public class SettingsFragment extends BasePagerFragment implements SunriseView.SunriseListener {

    private AppCompatCheckBox sunriseAutoSwitch;
    private FrameLayout sunriseLayout;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private SunriseView sunriseView;

    private SharedPreferences prefs;

    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;

    private int colorAccent;
    private int textColorPrimary;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sunriseAutoSwitch = view.findViewById(R.id.sunriseAutoSwitch);
        sunriseLayout = view.findViewById(R.id.sunriseLayout);
        sunriseTextView = view.findViewById(R.id.sunriseTextView);
        sunsetTextView = view.findViewById(R.id.sunsetTextView);
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
                } else {
                    sunriseView.invalidate();
                    onSunriseChanged(getAlarmio().getDayStart(), getAlarmio().getDayEnd());
                }
                getAlarmio().onActivityResume();
            }
        });

        sunriseView.setListener(this);
        onSunriseChanged(getAlarmio().getDayStart(), getAlarmio().getDayEnd());

        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        colorAccent = integer;
                        invalidate();
                    }
                });

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textColorPrimary = integer;
                        invalidate();
                    }
                });

        return view;
    }

    private void invalidate() {
        int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};

        ColorStateList colorStateList = new ColorStateList(
                states,
                new int[]{
                        Color.argb(100, Color.red(textColorPrimary), Color.green(textColorPrimary), Color.blue(textColorPrimary)),
                        colorAccent
                }
        );

        CompoundButtonCompat.setButtonTintList(sunriseAutoSwitch, colorStateList);
        sunriseAutoSwitch.setTextColor(textColorPrimary);
    }

    @Override
    public void onDestroyView() {
        if (sunriseView != null) {
            colorAccentSubscription.dispose();
            textColorPrimarySubscription.dispose();
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

    @Override
    public void onSunriseChanged(int sunrise, int sunset) {
        Calendar sunriseCalendar = Calendar.getInstance();
        sunriseCalendar.set(Calendar.HOUR_OF_DAY, sunrise);
        sunriseCalendar.set(Calendar.MINUTE, 0);
        sunriseTextView.setText(FormatUtils.formatShort(getContext(), new Date(sunriseCalendar.getTimeInMillis())));

        Calendar sunsetCalendar = Calendar.getInstance();
        sunsetCalendar.set(Calendar.HOUR_OF_DAY, sunset);
        sunsetCalendar.set(Calendar.MINUTE, 0);
        sunsetTextView.setText(FormatUtils.formatShort(getContext(), new Date(sunsetCalendar.getTimeInMillis())));
    }
}
