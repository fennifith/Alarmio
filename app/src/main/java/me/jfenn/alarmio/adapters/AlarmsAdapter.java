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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
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
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.AlarmData;
import me.jfenn.alarmio.data.TimerData;
import me.jfenn.alarmio.dialogs.AestheticTimeSheetPickerDialog;
import me.jfenn.alarmio.dialogs.AlertDialog;
import me.jfenn.alarmio.dialogs.SoundChooserDialog;
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
        recycler.post(() -> notifyDataSetChanged());
    }

    public void setColorForeground(int colorForeground) {
        this.colorForeground = colorForeground;
        if (expandedPosition > 0) {
            recycler.post(() -> notifyItemChanged(expandedPosition));
        }
    }

    public void setTextColorPrimary(int colorTextPrimary) {
        this.textColorPrimary = colorTextPrimary;
        recycler.post(() -> notifyDataSetChanged());
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
            timerHolder.stop.setOnClickListener(view -> {
                TimerData timer = getTimer(timerHolder.getAdapterPosition());
                alarmio.removeTimer(timer);
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

            alarmHolder.name.setOnClickListener(isExpanded ? null : (View.OnClickListener) v -> alarmHolder.itemView.callOnClick());

            alarmHolder.name.setOnFocusChangeListener((v, hasFocus) -> alarmHolder.name.setCursorVisible(hasFocus && alarmHolder.getAdapterPosition() == expandedPosition));

            alarmHolder.enable.setOnCheckedChangeListener(null);
            alarmHolder.enable.setChecked(alarm.isEnabled);
            alarmHolder.enable.setOnCheckedChangeListener((compoundButton, b) -> {
                getAlarm(alarmHolder.getAdapterPosition()).setEnabled(alarmio, alarmManager, b);

                Transition transition = new AutoTransition();
                transition.setDuration(200);
                TransitionManager.beginDelayedTransition(recycler, transition);

                recycler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            });

            alarmHolder.time.setText(FormatUtils.formatShort(alarmio, alarm.time.getTime()));
            alarmHolder.time.setOnClickListener(view -> {
                AlarmData alarm1 = getAlarm(alarmHolder.getAdapterPosition());

                new AestheticTimeSheetPickerDialog(view.getContext(), alarm1.time.get(Calendar.HOUR_OF_DAY), alarm1.time.get(Calendar.MINUTE))
                        .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                            @Override
                            public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                                AlarmData alarm1 = getAlarm(alarmHolder.getAdapterPosition());
                                alarm1.time.set(Calendar.HOUR_OF_DAY, view.getHourOfDay());
                                alarm1.time.set(Calendar.MINUTE, view.getMinute());
                                alarm1.setTime(alarmio, alarmManager, alarm1.time.getTimeInMillis());

                                notifyItemChanged(alarmHolder.getAdapterPosition());
                            }

                            @Override
                            public void onCancel(PickerDialog<LinearTimePickerView> dialog) {
                            }
                        })
                        .show();
            });

            alarmHolder.nextTime.setVisibility(alarm.isEnabled ? View.VISIBLE : View.GONE);

            Calendar nextAlarm = alarm.getNext();
            if (alarm.isEnabled && nextAlarm != null) {
                // minutes in a week: 10080
                // maximum value of an integer: 2147483647
                // we do not need to check this int cast
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(nextAlarm.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());

                alarmHolder.nextTime.setText(String.format(alarmio.getString(R.string.title_alarm_next), FormatUtils.formatUnit(alarmio, minutes)));
            }

            alarmHolder.indicators.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            if (isExpanded) {
                alarmHolder.repeat.setOnCheckedChangeListener(null);
                alarmHolder.repeat.setChecked(alarm.isRepeat());
                alarmHolder.repeat.setOnCheckedChangeListener((compoundButton, b) -> {
                    AlarmData alarm12 = getAlarm(alarmHolder.getAdapterPosition());
                    for (int i = 0; i < 7; i++) {
                        alarm12.days[i] = b;
                    }
                    alarm12.setDays(alarmio, alarm12.days);

                    Transition transition = new AutoTransition();
                    transition.setDuration(150);
                    TransitionManager.beginDelayedTransition(recycler, transition);

                    recycler.post(() -> notifyDataSetChanged());
                });

                alarmHolder.days.setVisibility(alarm.isRepeat() ? View.VISIBLE : View.GONE);

                DaySwitch.OnCheckedChangeListener listener = (daySwitch, b) -> {
                    AlarmData alarm13 = getAlarm(alarmHolder.getAdapterPosition());
                    alarm13.days[alarmHolder.days.indexOfChild(daySwitch)] = b;
                    alarm13.setDays(alarmio, alarm13.days);

                    if (!alarm13.isRepeat()) {
                        notifyItemChanged(alarmHolder.getAdapterPosition());
                    } else {
                        // if the view isn't going to change size in the recycler,
                        //   then I can just do this (prevents the background flickering as
                        //   the recyclerview attempts to smooth the transition)
                        bindViewHolder(alarmHolder, alarmHolder.getAdapterPosition());
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
                alarmHolder.ringtone.setOnClickListener(view -> {
                    SoundChooserDialog dialog = new SoundChooserDialog();
                    dialog.setListener(sound -> {
                        int position1 = alarmHolder.getAdapterPosition();
                        AlarmData alarm14 = getAlarm(position1);
                        alarm14.setSound(alarmio, sound);
                        notifyItemChanged(position1);
                    });
                    dialog.show(fragmentManager, null);
                });

                AnimatedVectorDrawableCompat vibrateDrawable = AnimatedVectorDrawableCompat.create(alarmio, alarm.isVibrate ? R.drawable.ic_vibrate_to_none : R.drawable.ic_none_to_vibrate);
                alarmHolder.vibrateImage.setImageDrawable(vibrateDrawable);
                alarmHolder.vibrateImage.setAlpha(alarm.isVibrate ? 1 : 0.333f);
                alarmHolder.vibrate.setOnClickListener(view -> {
                    AlarmData alarm15 = getAlarm(alarmHolder.getAdapterPosition());
                    alarm15.setVibrate(alarmio, !alarm15.isVibrate);

                    AnimatedVectorDrawableCompat vibrateDrawable1 = AnimatedVectorDrawableCompat.create(alarmio, alarm15.isVibrate ? R.drawable.ic_none_to_vibrate : R.drawable.ic_vibrate_to_none);
                    if (vibrateDrawable1 != null) {
                        alarmHolder.vibrateImage.setImageDrawable(vibrateDrawable1);
                        vibrateDrawable1.start();
                    } else
                        alarmHolder.vibrateImage.setImageResource(alarm15.isVibrate ? R.drawable.ic_vibrate : R.drawable.ic_vibrate_none);

                    alarmHolder.vibrateImage.animate().alpha(alarm15.isVibrate ? 1 : 0.333f).setDuration(250).start();
                    if (alarm15.isVibrate)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                });
            } else {
                alarmHolder.repeatIndicator.setAlpha(alarm.isRepeat() ? 1 : 0.333f);
                alarmHolder.soundIndicator.setAlpha(alarm.hasSound() ? 1 : 0.333f);
                alarmHolder.vibrateIndicator.setAlpha(alarm.isVibrate ? 1 : 0.333f);
            }

            alarmHolder.expandImage.animate().rotationX(isExpanded ? 180 : 0).start();
            alarmHolder.delete.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            alarmHolder.delete.setOnClickListener(view -> {
                AlarmData alarm16 = getAlarm(alarmHolder.getAdapterPosition());
                new AlertDialog(view.getContext())
                        .setContent(alarmio.getString(R.string.msg_delete_confirmation, alarm16.getName(alarmio)))
                        .setListener((dialog, ok) -> {
                            if (ok)
                                alarmio.removeAlarm(getAlarm(alarmHolder.getAdapterPosition()));
                        })
                        .show();
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
                        .subscribe(integer -> {
                            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), isExpanded ? integer : colorForeground, isExpanded ? colorForeground : integer);
                            animator.addUpdateListener(animation -> alarmHolder.itemView.setBackgroundColor((int) animation.getAnimatedValue()));
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
                        });

                ValueAnimator animator = ValueAnimator.ofFloat(isExpanded ? 0 : DimenUtils.dpToPx(2), isExpanded ? DimenUtils.dpToPx(2) : 0);
                animator.addUpdateListener(animation -> ViewCompat.setElevation(alarmHolder.itemView, (float) animation.getAnimatedValue()));
                animator.start();
            } else {
                alarmHolder.itemView.setBackgroundColor(isExpanded ? colorForeground : Color.TRANSPARENT);
                ViewCompat.setElevation(alarmHolder.itemView, isExpanded ? DimenUtils.dpToPx(2) : 0);
            }

            alarmHolder.itemView.setOnClickListener(view -> {
                expandedPosition = isExpanded ? -1 : alarmHolder.getAdapterPosition();

                Transition transition = new AutoTransition();
                transition.setDuration(250);
                TransitionManager.beginDelayedTransition(recycler, transition);

                recycler.post(() -> notifyDataSetChanged());
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
