package james.alarmio.data.preference;

import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.functions.Consumer;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.data.PreferenceData;
import james.alarmio.utils.FormatUtils;
import james.alarmio.views.SunriseView;

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
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PreferenceData.THEME.setValue(adapterView.getContext(), i);
                holder.sunriseAutoSwitch.setVisibility(i == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
                holder.sunriseLayout.setVisibility(i == Alarmio.THEME_DAY_NIGHT ? View.VISIBLE : View.GONE);
                ((Alarmio) holder.itemView.getContext().getApplicationContext()).onActivityResume();
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
        holder.sunriseView.setListener(listener);
        listener.onSunriseChanged(holder.getAlarmio().getDayStart(), holder.getAlarmio().getDayEnd());

        holder.sunriseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(
                        view.getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                int dayEnd = holder.getAlarmio().getDayEnd();
                                if (hourOfDay < dayEnd) {
                                    PreferenceData.DAY_START.setValue(holder.getContext(), hourOfDay);
                                    holder.sunriseView.invalidate();
                                    listener.onSunriseChanged(hourOfDay, dayEnd);
                                    holder.getAlarmio().onActivityResume();
                                }
                            }
                        },
                        holder.getAlarmio().getDayStart(),
                        0,
                        DateFormat.is24HourFormat(holder.getContext())
                ).show();
            }
        });

        holder.sunsetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(
                        view.getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                int dayStart = holder.getAlarmio().getDayStart();
                                if (hourOfDay > dayStart) {
                                    PreferenceData.DAY_END.setValue(holder.getContext(), hourOfDay);
                                    holder.sunriseView.invalidate();
                                    listener.onSunriseChanged(dayStart, hourOfDay);
                                    holder.getAlarmio().onActivityResume();
                                }
                            }
                        },
                        holder.getAlarmio().getDayEnd(),
                        0,
                        DateFormat.is24HourFormat(holder.getContext())
                ).show();
            }
        });

        Aesthetic.get()
                .colorAccent()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(final Integer colorAccent) throws Exception {
                        Aesthetic.get()
                                .textColorPrimary()
                                .take(1)
                                .subscribe(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer textColorPrimary) throws Exception {
                                        ColorStateList colorStateList = new ColorStateList(
                                                new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                                                new int[]{
                                                        Color.argb(100, Color.red(textColorPrimary), Color.green(textColorPrimary), Color.blue(textColorPrimary)),
                                                        colorAccent
                                                }
                                        );

                                        CompoundButtonCompat.setButtonTintList(holder.sunriseAutoSwitch, colorStateList);
                                        holder.sunriseAutoSwitch.setTextColor(textColorPrimary);
                                    }
                                });
                    }
                });

        Aesthetic.get()
                .textColorSecondary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer textColorSecondary) throws Exception {
                        holder.themeSpinner.setSupportBackgroundTintList(ColorStateList.valueOf(textColorSecondary));
                    }
                });

        Aesthetic.get()
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
