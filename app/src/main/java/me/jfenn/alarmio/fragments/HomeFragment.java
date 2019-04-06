package me.jfenn.alarmio.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.afollestad.aesthetic.Aesthetic;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import io.reactivex.disposables.Disposable;
import jahirfiquitiva.libs.fabsmenu.FABsMenu;
import jahirfiquitiva.libs.fabsmenu.FABsMenuListener;
import jahirfiquitiva.libs.fabsmenu.TitleFAB;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.adapters.SimplePagerAdapter;
import me.jfenn.alarmio.data.AlarmData;
import me.jfenn.alarmio.data.PreferenceData;
import me.jfenn.alarmio.dialogs.AestheticTimeSheetPickerDialog;
import me.jfenn.alarmio.dialogs.TimerDialog;
import me.jfenn.alarmio.interfaces.FragmentInstantiator;
import me.jfenn.alarmio.utils.ImageUtils;
import me.jfenn.alarmio.views.PageIndicatorView;
import me.jfenn.androidutils.DimenUtils;
import me.jfenn.timedatepickers.dialogs.PickerDialog;
import me.jfenn.timedatepickers.views.LinearTimePickerView;

public class HomeFragment extends BaseFragment {

    public static final String INTENT_ACTION = "me.jfenn.alarmio.HomeFragment.INTENT_ACTION";

    private View view;
    private ViewPager timePager;
    private PageIndicatorView timeIndicator;
    private View bottomSheet;
    private ImageView background;
    private View overlay;
    private FABsMenu menu;
    private TitleFAB stopwatchFab;
    private TitleFAB timerFab;
    private TitleFAB alarmFab;

    private BottomSheetBehavior behavior;
    private boolean shouldCollapseBack;

    private Disposable colorPrimarySubscription;
    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;
    private Disposable textColorPrimaryInverseSubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
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
                            statusBarHeight = DimenUtils.getStatusBarHeight(getContext());

                        bottomSheet.setPadding(0, statusBarHeight, 0, 0);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    if (statusBarHeight < 0)
                        statusBarHeight = DimenUtils.getStatusBarHeight(getContext());

                    bottomSheet.setPadding(0, (int) (slideOffset * statusBarHeight), 0, 0);
                }
            });
        }

        SimplePagerAdapter pagerAdapter = new SimplePagerAdapter(
                getContext(), getChildFragmentManager(),
                new AlarmsFragment.Instantiator(getContext()),
                new SettingsFragment.Instantiator(getContext())
        );

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() > 0) {
                    shouldCollapseBack = behavior.getState() != BottomSheetBehavior.STATE_EXPANDED;
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    menu.hide();
                } else {
                    setClockFragments();
                    menu.show();
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

        colorPrimarySubscription = Aesthetic.Companion.get()
                .colorPrimary()
                .subscribe(integer -> {
                    bottomSheet.setBackgroundColor(integer);
                    overlay.setBackgroundColor(integer);
                });

        colorAccentSubscription = Aesthetic.Companion.get()
                .colorAccent()
                .subscribe(integer -> {
                    menu.setMenuButtonColor(integer);

                    int color = ContextCompat.getColor(getContext(), getAlarmio().getActivityTheme() == Alarmio.THEME_AMOLED ? R.color.textColorPrimary : R.color.textColorPrimaryNight);
                    menu.getMenuButton().setColorFilter(color);
                    stopwatchFab.setColorFilter(color);
                    timerFab.setColorFilter(color);
                    alarmFab.setColorFilter(color);

                    stopwatchFab.setBackgroundColor(integer);
                    timerFab.setBackgroundColor(integer);
                    alarmFab.setBackgroundColor(integer);
                });

        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> {
                    stopwatchFab.setTitleTextColor(integer);
                    timerFab.setTitleTextColor(integer);
                    alarmFab.setTitleTextColor(integer);
                });

        textColorPrimaryInverseSubscription = Aesthetic.Companion.get()
                .textColorPrimaryInverse()
                .subscribe(integer -> {
                    alarmFab.setTitleBackgroundColor(integer);
                    stopwatchFab.setTitleBackgroundColor(integer);
                    timerFab.setTitleBackgroundColor(integer);
                });

        stopwatchFab.setOnClickListener(view -> {
            menu.collapseImmediately();
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                    .replace(R.id.fragment, new StopwatchFragment())
                    .addToBackStack(null)
                    .commit();
        });

        timerFab.setOnClickListener(view -> {
            invokeTimerScheduler();
            menu.collapse();
        });

        alarmFab.setOnClickListener(view -> {
            invokeAlarmScheduler();
            menu.collapse();
        });

        menu.setMenuListener(new FABsMenuListener() {
            @Override
            public void onMenuExpanded(FABsMenu fabsMenu) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String[]{Manifest.permission.FOREGROUND_SERVICE}, 0);
                    else fabsMenu.collapseImmediately();
                }
            }
        });

        // check actions passed from MainActivity; open timer/alarm schedulers if necessary
        Bundle args = getArguments();
        String action = args != null ? args.getString(INTENT_ACTION, null) : null;
        if (AlarmClock.ACTION_SET_ALARM.equals(action)) {
            view.post(() -> invokeAlarmScheduler());
        } else if (AlarmClock.ACTION_SET_TIMER.equals(action)) {
            view.post(() -> invokeTimerScheduler());
        }

        return view;
    }

    /**
     * Open the alarm scheduler dialog to allow the user to create
     * a new alarm.
     */
    private void invokeAlarmScheduler() {
        new AestheticTimeSheetPickerDialog(view.getContext())
                .setListener(new PickerDialog.OnSelectedListener<LinearTimePickerView>() {
                    @Override
                    public void onSelect(PickerDialog<LinearTimePickerView> dialog, LinearTimePickerView view) {
                        AlarmManager manager = (AlarmManager) view.getContext().getSystemService(Context.ALARM_SERVICE);
                        AlarmData alarm = getAlarmio().newAlarm();
                        alarm.time.set(Calendar.HOUR_OF_DAY, view.getHourOfDay());
                        alarm.time.set(Calendar.MINUTE, view.getMinute());
                        alarm.setTime(getAlarmio(), manager, alarm.time.getTimeInMillis());
                        alarm.setEnabled(getContext(), manager, true);

                        getAlarmio().onAlarmsChanged();
                    }

                    @Override
                    public void onCancel(PickerDialog<LinearTimePickerView> dialog) {
                    }
                })
                .show();
    }

    /**
     * Open the timer scheduler dialog to allow the user to start
     * a timer.
     */
    private void invokeTimerScheduler() {
        new TimerDialog(getContext(), getFragmentManager())
                .show();
    }

    /**
     * Update the time zones displayed in the clock fragments pager.
     */
    private void setClockFragments() {
        if (timePager != null && timeIndicator != null) {
            List<FragmentInstantiator> fragments = new ArrayList<>();
            fragments.add(new ClockFragment.Instantiator(getContext(), null));

            for (String id : TimeZone.getAvailableIDs()) {
                if (PreferenceData.TIME_ZONE_ENABLED.getSpecificValue(getContext(), id))
                    fragments.add(new ClockFragment.Instantiator(getContext(), id));
            }

            SimplePagerAdapter timeAdapter = new SimplePagerAdapter(getContext(), getChildFragmentManager(), fragments.toArray(new FragmentInstantiator[0]));
            timePager.setAdapter(timeAdapter);
            timeIndicator.setViewPager(timePager);
            timeIndicator.setVisibility(fragments.size() > 1 ? View.VISIBLE : View.GONE);
        }

        ImageUtils.getBackgroundImage(background);
    }

    @Override
    public void onDestroyView() {
        colorPrimarySubscription.dispose();
        colorAccentSubscription.dispose();
        textColorPrimarySubscription.dispose();
        textColorPrimaryInverseSubscription.dispose();
        super.onDestroyView();
    }

}
