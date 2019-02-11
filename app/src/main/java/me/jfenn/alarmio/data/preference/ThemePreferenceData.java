package me.jfenn.alarmio.data.preference;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.PreferenceData;
import me.jfenn.alarmio.dialogs.AestheticTimeSheetPickerDialog;
import me.jfenn.alarmio.utils.FormatUtils;
import me.jfenn.sunrisesunsetview.SunriseSunsetView;
import me.jfenn.timedatepickers.dialogs.PickerDialog;
import me.jfenn.timedatepickers.views.LinearTimePickerView;

public class ThemePreferenceData extends BasePreferenceData<ThemePreferenceData.ViewHolder> {

    private static final long HOUR_LENGTH = 3600000L;

    public ThemePreferenceData() {
    }

    @Override
    public BasePreferenceData.ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_theme, parent, false));
    }

    @Override
    public void bindViewHolder(final ViewHolder holder) {
        holder.themeSpinner.setAdapter(ArrayAdapter.createFromResource(holder.itemView.getContext(), R.array.array_themes, R.layout.support_simple_spinner_dropdown_item));
        int theme = ((Alarmio) holder.itemView.getContext().getApplicationContext()).getActivityTheme();
        holder.themeSpinner.setOnItemSelectedListener(null);
        holder.themeSpinner.setSelection(theme);
        holder.sunriseAutoSwitch.setVisibility(theme == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
        holder.sunriseLayout.setVisibility(theme == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
        holder.themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            Integer selection = null;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (selection != null) {
                    PreferenceData.THEME.setValue(adapterView.getContext(), i);
                    holder.sunriseAutoSwitch.setVisibility(i == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
                    holder.sunriseLayout.setVisibility(i == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
                    ((Alarmio) holder.itemView.getContext().getApplicationContext()).updateTheme();
                } else selection = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final SunriseSunsetView.SunriseListener listener = new SunriseSunsetView.SunriseListener() {
            @Override
            public void onSunriseChanged(SunriseSunsetView sunriseSunsetView, long l) {
                int hour = Math.round((float) l / HOUR_LENGTH);
                holder.sunriseTextView.setText(getText(hour));
                sunriseSunsetView.setSunrise(hour * HOUR_LENGTH, true);
                PreferenceData.DAY_START.setValue(holder.getContext(), hour);
                holder.getAlarmio().updateTheme();
            }

            @Override
            public void onSunsetChanged(SunriseSunsetView sunriseSunsetView, long l) {
                int hour = Math.round((float) l / HOUR_LENGTH);
                holder.sunsetTextView.setText(getText(hour));
                sunriseSunsetView.setSunset(hour * HOUR_LENGTH, true);
                PreferenceData.DAY_END.setValue(holder.getContext(), hour);
                holder.getAlarmio().updateTheme();
            }

            private String getText(int hour) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, 0);
                return FormatUtils.formatShort(holder.getContext(), new Date(cal.getTimeInMillis()));
            }
        };

        holder.sunriseAutoSwitch.setOnCheckedChangeListener(null);
        holder.sunriseAutoSwitch.setChecked(holder.getAlarmio().isDayAuto());
        holder.sunriseAutoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceData.DAY_AUTO.setValue(holder.getContext(), b);
                if (b && ContextCompat.checkSelfPermission(holder.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    holder.getAlarmio().requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
                    holder.sunriseAutoSwitch.setChecked(false);
                } else {
                    listener.onSunriseChanged(holder.sunriseView, holder.getAlarmio().getDayStart() * HOUR_LENGTH);
                    listener.onSunsetChanged(holder.sunriseView, holder.getAlarmio().getDayEnd() * HOUR_LENGTH);
                }
            }
        });

        listener.onSunriseChanged(holder.sunriseView, holder.getAlarmio().getDayStart() * HOUR_LENGTH);
        listener.onSunsetChanged(holder.sunriseView, holder.getAlarmio().getDayEnd() * HOUR_LENGTH);
        holder.sunriseView.setListener(new SunriseSunsetView.SunriseListener() {
            @Override
            public void onSunriseChanged(SunriseSunsetView sunriseSunsetView, long l) {
                holder.sunriseAutoSwitch.setChecked(false);
                listener.onSunriseChanged(sunriseSunsetView, l);
            }

            @Override
            public void onSunsetChanged(SunriseSunsetView sunriseSunsetView, long l) {
                holder.sunriseAutoSwitch.setChecked(false);
                listener.onSunsetChanged(sunriseSunsetView, l);
            }
        });

        holder.sunriseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AestheticTimeSheetPickerDialog(view.getContext(), holder.getAlarmio().getDayStart(), 0)
                        .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                            @Override
                            public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                                holder.sunriseAutoSwitch.setChecked(false);
                                if (view.getHourOfDay() < holder.getAlarmio().getDayEnd())
                                    listener.onSunriseChanged(holder.sunriseView, view.getHourOfDay() * HOUR_LENGTH);
                            }

                            @Override
                            public void onCancel(PickerDialog<LinearTimePickerView> dialog) {
                            }
                        })
                        .show();
            }
        });

        holder.sunsetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AestheticTimeSheetPickerDialog(view.getContext(), holder.getAlarmio().getDayEnd(), 0)
                        .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                            @Override
                            public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                                holder.sunriseAutoSwitch.setChecked(false);
                                if (view.getHourOfDay() > holder.getAlarmio().getDayStart())
                                    listener.onSunsetChanged(holder.sunriseView, view.getHourOfDay() * HOUR_LENGTH);
                            }

                            @Override
                            public void onCancel(PickerDialog<LinearTimePickerView> dialog) {
                            }
                        }).show();
            }
        });

        Aesthetic.Companion.get()
                .textColorSecondary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer textColorSecondary) throws Exception {
                        holder.themeSpinner.setSupportBackgroundTintList(ColorStateList.valueOf(textColorSecondary));
                    }
                });

        Aesthetic.Companion.get()
                .colorCardViewBackground()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer colorForeground) throws Exception {
                        holder.themeSpinner.setPopupBackgroundDrawable(new ColorDrawable(colorForeground));
                    }
                });
    }

    public static class ViewHolder extends BasePreferenceData.ViewHolder {

        private AppCompatSpinner themeSpinner;
        private AppCompatCheckBox sunriseAutoSwitch;
        private View sunriseLayout;
        private SunriseSunsetView sunriseView;
        private TextView sunriseTextView;
        private TextView sunsetTextView;

        public ViewHolder(View v) {
            super(v);
            themeSpinner = v.findViewById(R.id.themeSpinner);
            sunriseAutoSwitch = v.findViewById(R.id.sunriseAutoSwitch);
            sunriseLayout = v.findViewById(R.id.sunriseLayout);
            sunriseView = v.findViewById(R.id.sunriseView);
            sunriseTextView = v.findViewById(R.id.sunriseTextView);
            sunsetTextView = v.findViewById(R.id.sunsetTextView);
        }
    }

}
