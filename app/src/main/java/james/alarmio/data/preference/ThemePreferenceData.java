package james.alarmio.data.preference;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
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

import io.reactivex.functions.Consumer;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.data.PreferenceData;
import james.alarmio.dialogs.AestheticTimeSheetPickerDialog;
import james.alarmio.utils.FormatUtils;
import james.alarmio.views.SunriseView;
import me.jfenn.timedatepickers.dialogs.PickerDialog;
import me.jfenn.timedatepickers.views.LinearTimePickerView;

public class ThemePreferenceData extends BasePreferenceData<ThemePreferenceData.ViewHolder> {

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

        final SunriseView.SunriseListener listener = new SunriseView.SunriseListener() {
            @Override
            public void onSunriseChanged(int sunrise, int sunset) {
                Calendar sunriseCalendar = Calendar.getInstance();
                sunriseCalendar.set(Calendar.HOUR_OF_DAY, sunrise);
                sunriseCalendar.set(Calendar.MINUTE, 0);
                holder.sunriseTextView.setText(FormatUtils.formatShort(holder.getContext(), new Date(sunriseCalendar.getTimeInMillis())));

                Calendar sunsetCalendar = Calendar.getInstance();
                sunsetCalendar.set(Calendar.HOUR_OF_DAY, sunset);
                sunsetCalendar.set(Calendar.MINUTE, 0);
                holder.sunsetTextView.setText(FormatUtils.formatShort(holder.getContext(), new Date(sunsetCalendar.getTimeInMillis())));
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
                    holder.sunriseView.invalidate();
                    listener.onSunriseChanged(holder.getAlarmio().getDayStart(), holder.getAlarmio().getDayEnd());
                }
                holder.getAlarmio().updateTheme();
            }
        });

        holder.sunriseView.setListener(listener);
        listener.onSunriseChanged(holder.getAlarmio().getDayStart(), holder.getAlarmio().getDayEnd());

        holder.sunriseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.getAlarmio().isDayAuto()) {
                    new AestheticTimeSheetPickerDialog(view.getContext(), holder.getAlarmio().getDayStart(), 0)
                            .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                                @Override
                                public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                                    int dayEnd = holder.getAlarmio().getDayEnd();
                                    if (view.getHourOfDay() < dayEnd) {
                                        PreferenceData.DAY_START.setValue(holder.getContext(), view.getHourOfDay());
                                        holder.sunriseView.invalidate();
                                        listener.onSunriseChanged(view.getHourOfDay(), dayEnd);
                                        holder.getAlarmio().updateTheme();
                                    }
                                }

                                @Override
                                public void onCancel(PickerDialog<LinearTimePickerView> dialog) {

                                }
                            })
                            .show();
                }
            }
        });

        holder.sunsetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.getAlarmio().isDayAuto()) {
                    new AestheticTimeSheetPickerDialog(view.getContext(), holder.getAlarmio().getDayEnd(), 0)
                            .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                                @Override
                                public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                                    int dayStart = holder.getAlarmio().getDayStart();
                                    if (view.getHourOfDay() > dayStart) {
                                        PreferenceData.DAY_END.setValue(holder.getContext(), view.getHourOfDay());
                                        holder.sunriseView.invalidate();
                                        listener.onSunriseChanged(dayStart, view.getHourOfDay());
                                        holder.getAlarmio().updateTheme();
                                    }
                                }

                                @Override
                                public void onCancel(PickerDialog<LinearTimePickerView> dialog) {

                                }
                            })
                            .show();
                }
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
        private SunriseView sunriseView;
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
