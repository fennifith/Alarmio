package james.alarmio.adapters;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
import java.util.List;

import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.data.AlarmData;
import james.alarmio.utils.FormatUtils;
import james.alarmio.views.DaySwitch;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {

    private Context context;
    private SharedPreferences prefs;
    private AlarmManager manager;
    private List<AlarmData> alarms;

    private int expandedPosition = -1;

    public AlarmsAdapter(Context context, SharedPreferences prefs, List<AlarmData> alarms) {
        this.context = context;
        this.prefs = prefs;
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.alarms = alarms;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final boolean isExpanded = position == expandedPosition;
        AlarmData alarm = alarms.get(position);

        holder.name.setFocusable(isExpanded);
        holder.name.setEnabled(isExpanded);
        holder.name.clearFocus();

        holder.name.setText(alarm.getName(context));
        holder.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                alarms.get(holder.getAdapterPosition()).setName(prefs, holder.name.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        holder.enable.setOnCheckedChangeListener(null);
        holder.enable.setChecked(alarm.isEnabled);
        holder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                alarms.get(holder.getAdapterPosition()).setEnabled(context, prefs, manager, b);
            }
        });

        holder.time.setText(FormatUtils.formatShort(context, alarm.time.getTime()));
        holder.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmData alarm = alarms.get(holder.getAdapterPosition());

                new TimePickerDialog(
                        context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                AlarmData alarm = alarms.get(holder.getAdapterPosition());
                                alarm.time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                alarm.time.set(Calendar.MINUTE, minute);
                                alarm.setTime(context, prefs, manager, alarm.time.getTimeInMillis());
                                holder.time.setText(FormatUtils.formatShort(context, alarm.time.getTime()));
                            }
                        },
                        alarm.time.get(Calendar.HOUR_OF_DAY),
                        alarm.time.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(context)
                ).show();
            }
        });

        holder.repeat.setOnCheckedChangeListener(null);
        holder.repeat.setChecked(alarm.isRepeat());
        holder.repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AlarmData alarm = alarms.get(holder.getAdapterPosition());

                for (int i = 0; i < 7; i++) {
                    alarm.days[i] = b;
                    ((DaySwitch) holder.days.getChildAt(i)).setChecked(b);
                }

                alarm.setDays(prefs, alarm.days);
                holder.days.setVisibility(b ? View.VISIBLE : View.GONE);
            }
        });

        holder.days.setVisibility(alarm.isRepeat() ? View.VISIBLE : View.GONE);

        DaySwitch.OnCheckedChangeListener listener = new DaySwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(DaySwitch daySwitch, boolean b) {
                AlarmData alarm = alarms.get(holder.getAdapterPosition());
                alarm.days[holder.days.indexOfChild(daySwitch)] = b;
                alarm.setDays(prefs, alarm.days);
            }
        };

        for (int i = 0; i < 7; i++) {
            DaySwitch daySwitch = (DaySwitch) holder.days.getChildAt(i);
            daySwitch.setChecked(alarm.days[i]);
            daySwitch.setOnCheckedChangeListener(listener);

            switch (i) {
                case 0:
                case 6:
                    daySwitch.setText("S");
                    break;
                case 1:
                    daySwitch.setText("M");
                    break;
                case 2:
                case 4:
                    daySwitch.setText("T");
                    break;
                case 3:
                    daySwitch.setText("W");
                    break;
                case 5:
                    daySwitch.setText("F");

            }
        }

        holder.ringtoneImage.setImageResource(alarm.isRingtone ? R.drawable.ic_ringtone : R.drawable.ic_ringtone_disabled);
        holder.ringtoneText.setText(alarm.getRingtoneName(context));
        holder.ringtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmData alarm = alarms.get(holder.getAdapterPosition());
            }
        });

        holder.vibrateImage.setImageResource(alarm.isVibrate ? R.drawable.ic_vibrate : R.drawable.ic_none);
        holder.vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmData alarm = alarms.get(holder.getAdapterPosition());
                alarm.setVibrate(prefs, !alarm.isVibrate);
                holder.vibrateImage.setImageResource(alarm.isVibrate ? R.drawable.ic_vibrate : R.drawable.ic_none);
            }
        });

        holder.extra.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.expandImage.animate().rotation(isExpanded ? 180 : 0);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int previousPosition = expandedPosition;
                expandedPosition = isExpanded ? -1 : holder.getAdapterPosition();

                if (previousPosition != expandedPosition && previousPosition != -1)
                    notifyItemChanged(previousPosition);
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        Aesthetic.get()
                .textColorPrimary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        holder.ringtoneImage.setColorFilter(integer);
                        holder.vibrateImage.setColorFilter(integer);
                        holder.expandImage.setColorFilter(integer);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private SwitchCompat enable;
        private TextView time;
        private View extra;
        private AppCompatCheckBox repeat;
        private LinearLayout days;
        private View ringtone;
        private ImageView ringtoneImage;
        private TextView ringtoneText;
        private View vibrate;
        private ImageView vibrateImage;
        private ImageView expandImage;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            enable = view.findViewById(R.id.enable);
            time = view.findViewById(R.id.time);
            extra = view.findViewById(R.id.extra);
            repeat = view.findViewById(R.id.repeat);
            days = view.findViewById(R.id.days);
            ringtone = view.findViewById(R.id.ringtone);
            ringtoneImage = view.findViewById(R.id.ringtoneImage);
            ringtoneText = view.findViewById(R.id.ringtoneText);
            vibrate = view.findViewById(R.id.vibrate);
            vibrateImage = view.findViewById(R.id.vibrateImage);
            expandImage = view.findViewById(R.id.expandImage);
        }
    }
}
