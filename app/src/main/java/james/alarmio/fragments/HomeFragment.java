package james.alarmio.fragments;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.Calendar;

import io.multimoon.colorful.ColorfulKt;
import jahirfiquitiva.libs.fabsmenu.FABsMenu;
import jahirfiquitiva.libs.fabsmenu.TitleFAB;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.adapters.SimplePagerAdapter;
import james.alarmio.data.AlarmData;
import james.alarmio.data.PreferenceData;
import james.alarmio.dialogs.AestheticTimeSheetPickerDialog;
import james.alarmio.dialogs.TimerDialog;
import james.alarmio.utils.ConversionUtils;
import james.alarmio.utils.ImageUtils;
import james.alarmio.views.PageIndicatorView;
import me.jfenn.timedatepickers.dialogs.PickerDialog;
import me.jfenn.timedatepickers.views.LinearTimePickerView;

public class HomeFragment extends BaseFragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPager timePager;
    private PageIndicatorView timeIndicator;
    private View bottomSheet;
    private ImageView background;
    private View overlay;
    private FABsMenu menu;
    private TitleFAB stopwatchFab;
    private TitleFAB timerFab;
    private TitleFAB alarmFab;

    private SimplePagerAdapter pagerAdapter;
    private SimplePagerAdapter timeAdapter;

    private BottomSheetBehavior behavior;
    private boolean shouldCollapseBack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        timePager = view.findViewById(R.id.timePager);
        bottomSheet = view.findViewById(R.id.bottomSheet);
        timeIndicator = view.findViewById(R.id.pageIndicator);
        background = view.findViewById(R.id.background);
        overlay = view.findViewById(R.id.overlay);
        menu = view.findViewById(R.id.fabsMenu);
        stopwatchFab = view.findViewById(R.id.stopwatchFab);
        timerFab = view.findViewById(R.id.timerFab);
        alarmFab = view.findViewById(R.id.alarmFab);

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setHideable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

                private int statusBarHeight = -1;

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                        bottomSheet.setPadding(0, 0, 0, 0);
                    else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        if (statusBarHeight < 0)
                            statusBarHeight = ConversionUtils.getStatusBarHeight(getContext());

                        bottomSheet.setPadding(0, statusBarHeight, 0, 0);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    if (statusBarHeight < 0)
                        statusBarHeight = ConversionUtils.getStatusBarHeight(getContext());

                    bottomSheet.setPadding(0, (int) (slideOffset * statusBarHeight), 0, 0);
                }
            });
        }

        pagerAdapter = new SimplePagerAdapter(getChildFragmentManager(), new AlarmsFragment(), new SettingsFragment());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() > 0) {
                    shouldCollapseBack = behavior.getState() != BottomSheetBehavior.STATE_EXPANDED;
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    setClockFragments();
                    if (shouldCollapseBack) {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shouldCollapseBack = false;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        setClockFragments();

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                behavior.setPeekHeight(view.getMeasuredHeight() / 2);
                view.findViewById(R.id.timeContainer).setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.getMeasuredHeight() / 2));
            }
        });

        int colorPrimary = ColorfulKt.Colorful().getPrimaryColor().getColorPack().normal().asInt();
        bottomSheet.setBackgroundColor(colorPrimary);
        overlay.setBackgroundColor(colorPrimary);

        int colorAccent = ColorfulKt.Colorful().getAccentColor().getColorPack().normal().asInt();
        menu.setMenuButtonColor(colorAccent);

        int color = ContextCompat.getColor(getContext(), getAlarmio().getActivityTheme() == Alarmio.THEME_AMOLED ? R.color.textColorPrimary : R.color.textColorPrimaryNight);
        menu.getMenuButton().setColorFilter(color);
        stopwatchFab.setColorFilter(color);
        timerFab.setColorFilter(color);
        alarmFab.setColorFilter(color);

        stopwatchFab.setBackgroundColor(colorAccent);
        timerFab.setBackgroundColor(colorAccent);
        alarmFab.setBackgroundColor(colorAccent);

        int textColor = getAlarmio().getTextColor();
        stopwatchFab.setTitleTextColor(textColor);
        timerFab.setTitleTextColor(textColor);
        alarmFab.setTitleTextColor(textColor);

        int textColorInverse = getAlarmio().getTextColor(true, true);
        alarmFab.setTitleBackgroundColor(textColorInverse);
        stopwatchFab.setTitleBackgroundColor(textColorInverse);
        timerFab.setTitleBackgroundColor(textColorInverse);

        stopwatchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.collapseImmediately();
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                        .replace(R.id.fragment, new StopwatchFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        timerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0, false);

                new TimerDialog(getContext(), getFragmentManager()).show();
                menu.collapse();
            }
        });

        alarmFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0, false);
                new AestheticTimeSheetPickerDialog(view.getContext())
                        .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                            @Override
                            public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                                AlarmManager manager = (AlarmManager) view.getContext().getSystemService(Context.ALARM_SERVICE);
                                AlarmData alarm = getAlarmio().newAlarm();
                                alarm.time.set(Calendar.HOUR_OF_DAY, view.getHourOfDay());
                                alarm.time.set(Calendar.MINUTE, view.getMinute());
                                alarm.setEnabled(getContext(), manager, true);

                                getAlarmio().onAlarmsChanged();
                            }

                            @Override
                            public void onCancel(PickerDialog<LinearTimePickerView> dialog) {
                            }
                        })
                        .show();

                menu.collapse();
            }
        });

        return view;
    }

    private void setClockFragments() {
        if (timePager != null && timeIndicator != null) {
            String[] timeZones = PreferenceData.TIME_ZONES.getValue(getContext());
            ClockFragment[] clockFragments = new ClockFragment[timeZones.length];
            for (int i = 0; i < timeZones.length; i++) {
                Bundle args = new Bundle();
                args.putString(ClockFragment.EXTRA_TIME_ZONE, timeZones[i]);
                ClockFragment fragment = new ClockFragment();
                fragment.setArguments(args);
                clockFragments[i] = fragment;
            }

            timeAdapter = new SimplePagerAdapter(getChildFragmentManager(), clockFragments);
            timePager.setAdapter(timeAdapter);
            timeIndicator.setViewPager(timePager);
            timeIndicator.setVisibility(clockFragments.length > 1 ? View.VISIBLE : View.GONE);
        }

        ImageUtils.getBackgroundImage(background);
    }

    @Override
    public void onDestroyView() {
        timeIndicator.unsubscribe();
        super.onDestroyView();
    }

}
