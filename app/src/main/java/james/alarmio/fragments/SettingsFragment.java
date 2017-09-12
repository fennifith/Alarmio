package james.alarmio.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import james.alarmio.views.SunriseView;

public class SettingsFragment extends BasePagerFragment implements SunriseView.SunriseListener {

    private AppCompatSpinner themeSpinner;
    private AppCompatCheckBox sunriseAutoSwitch;
    private FrameLayout sunriseLayout;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private SunriseView sunriseView;

    private SharedPreferences prefs;

    private Disposable colorAccentSubscription;
    private Disposable colorForegroundSubscription;
    private Disposable textColorPrimarySubscription;
    private Disposable textColorSecondarySubscription;

    private int colorAccent;
    private int colorForeground;
    private int textColorPrimary;
    private int textColorSecondary;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        themeSpinner = view.findViewById(R.id.themeSpinner);
        sunriseAutoSwitch = view.findViewById(R.id.sunriseAutoSwitch);
        sunriseLayout = view.findViewById(R.id.sunriseLayout);
        sunriseTextView = view.findViewById(R.id.sunriseTextView);
        sunsetTextView = view.findViewById(R.id.sunsetTextView);
        sunriseView = view.findViewById(R.id.sunriseView);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        themeSpinner.setAdapter(ArrayAdapter.createFromResource(getContext(), R.array.array_themes, R.layout.support_simple_spinner_dropdown_item));
        int theme = getAlarmio().getActivityTheme();
        themeSpinner.setSelection(theme);
        sunriseAutoSwitch.setVisibility(theme == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
        sunriseLayout.setVisibility(theme == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                prefs.edit().putInt(Alarmio.PREF_THEME, i).apply();
                sunriseAutoSwitch.setVisibility(i == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
                sunriseLayout.setVisibility(i == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
                getAlarmio().onActivityResume();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

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

        colorForegroundSubscription = Aesthetic.get()
                .colorCardViewBackground()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        colorForeground = integer;
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

        textColorSecondarySubscription = Aesthetic.get()
                .textColorSecondary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textColorSecondary = integer;
                        invalidate();
                    }
                });

        return view;
    }

    private void invalidate() {
        themeSpinner.setSupportBackgroundTintList(ColorStateList.valueOf(textColorSecondary));
        themeSpinner.setPopupBackgroundDrawable(new ColorDrawable(colorForeground));

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
            colorForegroundSubscription.dispose();
            textColorPrimarySubscription.dispose();
            textColorSecondarySubscription.dispose();
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
