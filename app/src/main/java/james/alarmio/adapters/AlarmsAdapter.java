package james.alarmio.adapters;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
import java.util.List;

import io.reactivex.functions.Consumer;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.activities.MainActivity;
import james.alarmio.data.AlarmData;
import james.alarmio.data.SoundData;
import james.alarmio.data.TimerData;
import james.alarmio.dialogs.AestheticTimeSheetPickerDialog;
import james.alarmio.dialogs.AlertDialog;
import james.alarmio.dialogs.SoundChooserDialog;
import james.alarmio.interfaces.SoundChooserListener;
import james.alarmio.receivers.TimerReceiver;
import james.alarmio.utils.ConversionUtils;
import james.alarmio.utils.FormatUtils;
import james.alarmio.views.DaySwitch;
import james.alarmio.views.ProgressLineView;
import me.jfenn.timedatepickers.dialogs.PickerDialog;
import me.jfenn.timedatepickers.views.LinearTimePickerView;

public class AlarmsAdapter extends RecyclerView.Adapter {

    private Alarmio alarmio;
    private RecyclerView recycler;
    private SharedPreferences prefs;
    private AlarmManager alarmManager;
    private FragmentManager fragmentManager;
    private List<TimerData> timers;
    private List<AlarmData> alarms;
    private int colorAccent = Color.WHITE;
    private int colorForeground = Color.TRANSPARENT;
    private int textColorPrimary = Color.WHITE;

    private int expandedPosition = -1;

    public AlarmsAdapter(Alarmio alarmio, RecyclerView recycler, FragmentManager fragmentManager) {
        this.alarmio = alarmio;
        this.recycler = recycler;
        this.prefs = alarmio.getPrefs();
        this.fragmentManager = fragmentManager;
        alarmManager = (AlarmManager) alarmio.getSystemService(Context.ALARM_SERVICE);
        timers = alarmio.getTimers();
        alarms = alarmio.getAlarms();
    }

    public void setColorAccent(int colorAccent) {
        this.colorAccent = colorAccent;
        notifyDataSetChanged();
    }

    public void setColorForeground(int colorForeground) {
        this.colorForeground = colorForeground;
        if (expandedPosition > 0)
            notifyItemChanged(expandedPosition);
    }

    public void setTextColorPrimary(int colorTextPrimary) {
        this.textColorPrimary = colorTextPrimary;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new TimerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timer, parent, false));
        else
            return new AlarmViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            final TimerViewHolder timerHolder = (TimerViewHolder) holder;

            if (timerHolder.runnable != null)
                timerHolder.handler.removeCallbacks(timerHolder.runnable);

            timerHolder.runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        TimerData timer = getTimer(timerHolder.getAdapterPosition());
                        String text = FormatUtils.formatMillis(timer.getRemainingMillis());
                        timerHolder.time.setText(text.substring(0, text.length() - 3));
                        timerHolder.progress.update(1 - ((float) timer.getRemainingMillis() / timer.getDuration()));
                        timerHolder.handler.postDelayed(this, 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            timerHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(alarmio, MainActivity.class);
                    intent.putExtra(TimerReceiver.EXTRA_TIMER_ID, timerHolder.getAdapterPosition());
                    alarmio.startActivity(intent);
                }
            });

            timerHolder.stop.setColorFilter(textColorPrimary);
            timerHolder.stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TimerData timer = getTimer(timerHolder.getAdapterPosition());
                    alarmio.removeTimer(timer);
                }
            });

            timerHolder.handler.post(timerHolder.runnable);
        } else {
            final AlarmViewHolder alarmHolder = (AlarmViewHolder) holder;
            final boolean isExpanded = position == expandedPosition;
            AlarmData alarm = getAlarm(position);

            alarmHolder.name.setFocusable(isExpanded);
            alarmHolder.name.setEnabled(isExpanded);
            alarmHolder.name.clearFocus();
            alarmHolder.nameUnderline.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            alarmHolder.name.setText(alarm.getName(alarmio));
            alarmHolder.name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    getAlarm(alarmHolder.getAdapterPosition()).setName(alarmio, alarmHolder.name.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            alarmHolder.enable.setOnCheckedChangeListener(null);
            alarmHolder.enable.setChecked(alarm.isEnabled);
            alarmHolder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    getAlarm(alarmHolder.getAdapterPosition()).setEnabled(alarmio, alarmManager, b);
                }
            });

            alarmHolder.time.setText(FormatUtils.formatShort(alarmio, alarm.time.getTime()));
            alarmHolder.time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlarmData alarm = getAlarm(alarmHolder.getAdapterPosition());

                    new AestheticTimeSheetPickerDialog(view.getContext(), alarm.time.get(Calendar.HOUR_OF_DAY), alarm.time.get(Calendar.MINUTE))
                            .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                                @Override
                                public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                                    AlarmData alarm = getAlarm(alarmHolder.getAdapterPosition());
                                    alarm.time.set(Calendar.HOUR_OF_DAY, view.getHourOfDay());
                                    alarm.time.set(Calendar.MINUTE, view.getMinute());
                                    alarm.setTime(alarmio, alarmManager, alarm.time.getTimeInMillis());
                                    alarmHolder.time.setText(FormatUtils.formatShort(alarmio, alarm.time.getTime()));
                                }

                                @Override
                                public void onCancel(PickerDialog<LinearTimePickerView> dialog) {
                                }
                            })
                            .show();
                }
            });

            alarmHolder.indicators.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            if (isExpanded) {
                alarmHolder.repeat.setOnCheckedChangeListener(null);
                alarmHolder.repeat.setChecked(alarm.isRepeat());
                alarmHolder.repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        AlarmData alarm = getAlarm(alarmHolder.getAdapterPosition());
                        for (int i = 0; i < 7; i++) {
                            alarm.days[i] = b;
                        }
                        alarm.setDays(alarmio, alarm.days);

                        Transition transition = new AutoTransition();
                        transition.setDuration(150);
                        TransitionManager.beginDelayedTransition(recycler, transition);

                        notifyDataSetChanged();
                    }
                });

                alarmHolder.days.setVisibility(alarm.isRepeat() ? View.VISIBLE : View.GONE);

                DaySwitch.OnCheckedChangeListener listener = new DaySwitch.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(DaySwitch daySwitch, boolean b) {
                        AlarmData alarm = getAlarm(alarmHolder.getAdapterPosition());
                        alarm.days[alarmHolder.days.indexOfChild(daySwitch)] = b;
                        alarm.setDays(alarmio, alarm.days);
                        if (!alarm.isRepeat())
                            notifyItemChanged(alarmHolder.getAdapterPosition());
                    }
                };

                for (int i = 0; i < 7; i++) {
                    DaySwitch daySwitch = (DaySwitch) alarmHolder.days.getChildAt(i);
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

                alarmHolder.ringtoneImage.setImageResource(alarm.hasSound() ? R.drawable.ic_ringtone : R.drawable.ic_ringtone_disabled);
                alarmHolder.ringtoneImage.setAlpha(alarm.hasSound() ? 1 : 0.333f);
                alarmHolder.ringtoneText.setText(alarm.hasSound() ? alarm.getSound().getName() : alarmio.getString(R.string.title_sound_none));
                alarmHolder.ringtone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SoundChooserDialog dialog = new SoundChooserDialog();
                        dialog.setListener(new SoundChooserListener() {
                            @Override
                            public void onSoundChosen(SoundData sound) {
                                int position = alarmHolder.getAdapterPosition();
                                AlarmData alarm = getAlarm(position);
                                alarm.setSound(alarmio, sound);
                                notifyItemChanged(position);
                            }
                        });
                        dialog.show(fragmentManager, null);
                    }
                });

                AnimatedVectorDrawableCompat vibrateDrawable = AnimatedVectorDrawableCompat.create(alarmio, alarm.isVibrate ? R.drawable.ic_vibrate_to_none : R.drawable.ic_none_to_vibrate);
                alarmHolder.vibrateImage.setImageDrawable(vibrateDrawable);
                alarmHolder.vibrateImage.setAlpha(alarm.isVibrate ? 1 : 0.333f);
                alarmHolder.vibrate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlarmData alarm = getAlarm(alarmHolder.getAdapterPosition());
                        alarm.setVibrate(alarmio, !alarm.isVibrate);

                        AnimatedVectorDrawableCompat vibrateDrawable = AnimatedVectorDrawableCompat.create(alarmio, alarm.isVibrate ? R.drawable.ic_none_to_vibrate : R.drawable.ic_vibrate_to_none);
                        if (vibrateDrawable != null) {
                            alarmHolder.vibrateImage.setImageDrawable(vibrateDrawable);
                            vibrateDrawable.start();
                        } else
                            alarmHolder.vibrateImage.setImageResource(alarm.isVibrate ? R.drawable.ic_vibrate : R.drawable.ic_vibrate_none);

                        alarmHolder.vibrateImage.animate().alpha(alarm.isVibrate ? 1 : 0.333f).setDuration(250).start();
                        if (alarm.isVibrate)
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    }
                });
            } else {
                alarmHolder.repeatIndicator.setAlpha(alarm.isRepeat() ? 1 : 0.333f);
                alarmHolder.soundIndicator.setAlpha(alarm.hasSound() ? 1 : 0.333f);
                alarmHolder.vibrateIndicator.setAlpha(alarm.isVibrate ? 1 : 0.333f);
            }

            alarmHolder.expandImage.animate().rotationX(isExpanded ? 180 : 0).start();
            alarmHolder.delete.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            alarmHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlarmData alarm = getAlarm(alarmHolder.getAdapterPosition());
                    new AlertDialog(view.getContext())
                            .setContent(alarmio.getString(R.string.msg_delete_confirmation, alarm.getName(alarmio)))
                            .setListener(new AlertDialog.Listener() {
                                @Override
                                public void onDismiss(AlertDialog dialog, boolean ok) {
                                    if (ok)
                                        alarmio.removeAlarm(getAlarm(alarmHolder.getAdapterPosition()));
                                }
                            })
                            .show();
                }
            });

            alarmHolder.repeat.setTextColor(textColorPrimary);
            alarmHolder.delete.setTextColor(textColorPrimary);
            alarmHolder.ringtoneImage.setColorFilter(textColorPrimary);
            alarmHolder.vibrateImage.setColorFilter(textColorPrimary);
            alarmHolder.expandImage.setColorFilter(textColorPrimary);
            alarmHolder.repeatIndicator.setColorFilter(textColorPrimary);
            alarmHolder.soundIndicator.setColorFilter(textColorPrimary);
            alarmHolder.vibrateIndicator.setColorFilter(textColorPrimary);
            alarmHolder.nameUnderline.setBackgroundColor(textColorPrimary);

            int visibility = isExpanded ? View.VISIBLE : View.GONE;
            if (visibility != alarmHolder.extra.getVisibility()) {
                alarmHolder.extra.setVisibility(visibility);
                Aesthetic.Companion.get()
                        .colorPrimary()
                        .take(1)
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) throws Exception {
                                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), isExpanded ? integer : colorForeground, isExpanded ? colorForeground : integer);
                                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        alarmHolder.itemView.setBackgroundColor((int) animation.getAnimatedValue());
                                    }
                                });
                                animator.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        alarmHolder.itemView.setBackgroundColor(isExpanded ? colorForeground : Color.TRANSPARENT);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                    }
                                });
                                animator.start();
                            }
                        });

                ValueAnimator animator = ValueAnimator.ofFloat(isExpanded ? 0 : ConversionUtils.dpToPx(2), isExpanded ? ConversionUtils.dpToPx(2) : 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ViewCompat.setElevation(alarmHolder.itemView, (float) animation.getAnimatedValue());
                    }
                });
                animator.start();
            } else {
                alarmHolder.itemView.setBackgroundColor(isExpanded ? colorForeground : Color.TRANSPARENT);
                ViewCompat.setElevation(alarmHolder.itemView, isExpanded ? ConversionUtils.dpToPx(2) : 0);
            }

            alarmHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expandedPosition = isExpanded ? -1 : alarmHolder.getAdapterPosition();

                    Transition transition = new AutoTransition();
                    transition.setDuration(250);
                    TransitionManager.beginDelayedTransition(recycler, transition);

                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position < timers.size() ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return timers.size() + alarms.size();
    }

    private TimerData getTimer(int position) {
        return timers.get(position);
    }

    private AlarmData getAlarm(int position) {
        return alarms.get(position - timers.size());
    }

    public static class TimerViewHolder extends RecyclerView.ViewHolder {

        private final Handler handler = new Handler();
        private Runnable runnable;

        private TextView time;
        private ImageView stop;
        private ProgressLineView progress;

        public TimerViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            stop = itemView.findViewById(R.id.stop);
            progress = itemView.findViewById(R.id.progress);
        }
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private View nameUnderline;
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
        private TextView delete;
        private View indicators;
        private ImageView repeatIndicator;
        private ImageView soundIndicator;
        private ImageView vibrateIndicator;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            nameUnderline = itemView.findViewById(R.id.underline);
            enable = itemView.findViewById(R.id.enable);
            time = itemView.findViewById(R.id.time);
            extra = itemView.findViewById(R.id.extra);
            repeat = itemView.findViewById(R.id.repeat);
            days = itemView.findViewById(R.id.days);
            ringtone = itemView.findViewById(R.id.ringtone);
            ringtoneImage = itemView.findViewById(R.id.ringtoneImage);
            ringtoneText = itemView.findViewById(R.id.ringtoneText);
            vibrate = itemView.findViewById(R.id.vibrate);
            vibrateImage = itemView.findViewById(R.id.vibrateImage);
            expandImage = itemView.findViewById(R.id.expandImage);
            delete = itemView.findViewById(R.id.delete);
            indicators = itemView.findViewById(R.id.indicators);
            repeatIndicator = itemView.findViewById(R.id.repeatIndicator);
            soundIndicator = itemView.findViewById(R.id.soundIndicator);
            vibrateIndicator = itemView.findViewById(R.id.vibrateIndicator);
        }
    }
}
