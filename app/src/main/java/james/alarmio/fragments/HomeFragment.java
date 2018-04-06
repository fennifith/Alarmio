package james.alarmio.fragments;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.afollestad.aesthetic.Aesthetic;

import java.util.Calendar;
import java.util.TimeZone;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import jahirfiquitiva.libs.fabsmenu.FABsMenu;
import jahirfiquitiva.libs.fabsmenu.TitleFAB;
import james.alarmio.R;
import james.alarmio.adapters.SimplePagerAdapter;
import james.alarmio.data.AlarmData;
import james.alarmio.dialogs.TimerDialog;
import james.alarmio.utils.ConversionUtils;
import james.alarmio.views.PageIndicatorView;

public class HomeFragment extends BaseFragment implements FABsMenu.OnFABsMenuUpdateListener {

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

    private Disposable colorPrimarySubscription;
    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;

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

        menu.setMenuUpdateListener(this);

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
                } else if (shouldCollapseBack) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    shouldCollapseBack = false;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        Bundle args = new Bundle();
        args.putString(ClockFragment.EXTRA_TIME_ZONE, TimeZone.getAvailableIDs()[0]);
        ClockFragment fragment = new ClockFragment();
        fragment.setArguments(args);
        timeAdapter = new SimplePagerAdapter(getChildFragmentManager(), new ClockFragment(), fragment);
        timePager.setAdapter(timeAdapter);
        timeIndicator.setViewPager(timePager);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                behavior.setPeekHeight(view.getMeasuredHeight() / 2);
                view.findViewById(R.id.timeContainer).setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.getMeasuredHeight() / 2));
            }
        });

        background.setImageDrawable(WallpaperManager.getInstance(getContext()).getFastDrawable());

        colorPrimarySubscription = Aesthetic.get()
                .colorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        bottomSheet.setBackgroundColor(integer);
                        overlay.setBackgroundColor(integer);
                    }
                });

        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        menu.getMenuButton().setBackgroundColor(integer);
                        stopwatchFab.setBackgroundColor(integer);
                        timerFab.setBackgroundColor(integer);
                        alarmFab.setBackgroundColor(integer);
                    }
                });

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        stopwatchFab.setTitleTextColor(integer);
                        timerFab.setTitleTextColor(integer);
                        alarmFab.setTitleTextColor(integer);
                    }
                });

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

                Calendar time = Calendar.getInstance();
                new TimePickerDialog(
                        getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                AlarmManager manager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                                AlarmData alarm = getAlarmio().newAlarm();
                                alarm.time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                alarm.time.set(Calendar.MINUTE, minute);
                                alarm.setTime(getContext(), manager, alarm.time.getTimeInMillis());
                                alarm.setEnabled(getContext(), manager, true);

                                getAlarmio().onAlarmsChanged();
                            }
                        },
                        time.get(Calendar.HOUR_OF_DAY),
                        time.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(getContext())
                ).show();

                menu.collapse();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        timeIndicator.unsubscribe();
        colorPrimarySubscription.dispose();
        colorAccentSubscription.dispose();
        super.onDestroyView();
    }

    @Override
    public void onMenuClicked() {
        menu.toggle();
    }

    @Override
    public void onMenuExpanded() {

    }

    @Override
    public void onMenuCollapsed() {

    }

}
