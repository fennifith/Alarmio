package me.jfenn.alarmio.adapters;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.AlarmData;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.data.TimerData;
import me.jfenn.alarmio.dialogs.AestheticTimeSheetPickerDialog;
import me.jfenn.alarmio.dialogs.AlertDialog;
import me.jfenn.alarmio.dialogs.SoundChooserDialog;
import me.jfenn.alarmio.interfaces.SoundChooserListener;
import me.jfenn.alarmio.utils.FormatUtils;
import me.jfenn.alarmio.views.DaySwitch;
import me.jfenn.alarmio.views.ProgressLineView;
import me.jfenn.androidutils.DimenUtils;
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

            alarmHolder.name.setFocusableInTouchMode(isExpanded);
            alarmHolder.name.setCursorVisible(false);
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

            alarmHolder.name.setOnClickListener(isExpanded ? null : new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alarmHolder.itemView.callOnClick();
                }
            });

            alarmHolder.name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    alarmHolder.name.setCursorVisible(hasFocus && alarmHolder.getAdapterPosition() == expandedPosition);
                }
            });

            alarmHolder.enable.setOnCheckedChangeListener(null);
            alarmHolder.enable.setChecked(alarm.isEnabled);
            alarmHolder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    getAlarm(alarmHolder.getAdapterPosition()).setEnabled(alarmio, alarmManager, b);

                    Transition transition = new AutoTransition();
                    transition.setDuration(200);
                    TransitionManager.beginDelayedTransition(recycler, transition);

                    notifyDataSetChanged();
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

                                    notifyItemChanged(alarmHolder.getAdapterPosition());
                                }

                                @Override
                                public void onCancel(PickerDialog<LinearTimePickerView> dialog) {
                                }
                            })
                            .show();
                }
            });

            alarmHolder.nextTime.setVisibility(alarm.isEnabled ? View.VISIBLE : View.GONE);

            Calendar nextAlarm = alarm.getNext();
            if (alarm.isEnabled && nextAlarm != null) {
                Date nextAlarmTime = alarm.getNext().getTime();

                // minutes in a week: 10080
                // maximum value of an integer: 2147483647
                // we do not need to check this int cast
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(nextAlarm.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());

                alarmHolder.nextTime.setText(String.format(alarmio.getString(R.string.title_alarm_next),
                        FormatUtils.format(nextAlarmTime, "MMMM d"), FormatUtils.formatUnit(alarmio, minutes)));
            }

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

                        if (!alarm.isRepeat()) {
                            notifyItemChanged(alarmHolder.getAdapterPosition());
                        } else {
                            // if the view isn't going to change size in the recycler,
                            //   then I can just do this (prevents the background flickering as
                            //   the recyclerview attempts to smooth the transition)
                            bindViewHolder(alarmHolder, alarmHolder.getAdapterPosition());
                        }
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

                ValueAnimator animator = ValueAnimator.ofFloat(isExpanded ? 0 : DimenUtils.dpToPx(2), isExpanded ? DimenUtils.dpToPx(2) : 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ViewCompat.setElevation(alarmHolder.itemView, (float) animation.getAnimatedValue());
                    }
                });
                animator.start();
            } else {
                alarmHolder.itemView.setBackgroundColor(isExpanded ? colorForeground : Color.TRANSPARENT);
                ViewCompat.setElevation(alarmHolder.itemView, isExpanded ? DimenUtils.dpToPx(2) : 0);
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

        private View nameContainer;
        private EditText name;
        private View nameUnderline;
        private SwitchCompat enable;
        private TextView time;
        private TextView nextTime;
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

        public AlarmViewHolder(View v) {
            super(v);
            nameContainer = v.findViewById(R.id.nameContainer);
            name = v.findViewById(R.id.name);
            nameUnderline = v.findViewById(R.id.underline);
            enable = v.findViewById(R.id.enable);
            time = v.findViewById(R.id.time);
            nextTime = v.findViewById(R.id.nextTime);
            extra = v.findViewById(R.id.extra);
            repeat = v.findViewById(R.id.repeat);
            days = v.findViewById(R.id.days);
            ringtone = v.findViewById(R.id.ringtone);
            ringtoneImage = v.findViewById(R.id.ringtoneImage);
            ringtoneText = v.findViewById(R.id.ringtoneText);
            vibrate = v.findViewById(R.id.vibrate);
            vibrateImage = v.findViewById(R.id.vibrateImage);
            expandImage = v.findViewById(R.id.expandImage);
            delete = v.findViewById(R.id.delete);
            indicators = v.findViewById(R.id.indicators);
            repeatIndicator = v.findViewById(R.id.repeatIndicator);
            soundIndicator = v.findViewById(R.id.soundIndicator);
            vibrateIndicator = v.findViewById(R.id.vibrateIndicator);
        }
    }
}
