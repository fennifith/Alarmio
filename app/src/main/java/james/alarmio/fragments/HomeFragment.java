package james.alarmio.fragments;

import android.app.WallpaperManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.TimeZone;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.adapters.SimplePagerAdapter;
import james.alarmio.utils.ConversionUtils;
import james.alarmio.views.PageIndicatorView;

public class HomeFragment extends BaseFragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPager timePager;
    private PageIndicatorView timeIndicator;
    private View bottomSheet;
    private ImageView background;
    private View overlay;

    private BottomSheetBehavior behavior;

    private Disposable colorPrimarySubscription;

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

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setHideable(false);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            private int statusBarHeight = -1;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (statusBarHeight < 0)
                    statusBarHeight = ConversionUtils.getStatusBarHeight(getContext());

                bottomSheet.setPadding(0, newState == BottomSheetBehavior.STATE_EXPANDED ? statusBarHeight : 0, 0, 0);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (statusBarHeight < 0)
                    statusBarHeight = ConversionUtils.getStatusBarHeight(getContext());

                bottomSheet.setPadding(0, (int) (slideOffset * statusBarHeight), 0, 0);
            }
        });

        viewPager.setAdapter(new SimplePagerAdapter(getChildFragmentManager(), new AlarmsFragment(), new TimersFragment()));
        tabLayout.setupWithViewPager(viewPager);

        Bundle args = new Bundle();
        args.putString(ClockFragment.EXTRA_TIME_ZONE, TimeZone.getAvailableIDs()[0]);
        ClockFragment fragment = new ClockFragment();
        fragment.setArguments(args);
        timePager.setAdapter(new SimplePagerAdapter(getChildFragmentManager(), new ClockFragment(), fragment));
        timeIndicator.setViewPager(timePager);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                behavior.setPeekHeight(view.getMeasuredHeight() / 2);
                view.findViewById(R.id.timeContainer).setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.getMeasuredHeight() / 2));
            }
        });

        background.setImageDrawable(WallpaperManager.getInstance(getContext()).getDrawable());

        colorPrimarySubscription = Aesthetic.get()
                .colorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        bottomSheet.setBackgroundColor(integer);
                        overlay.setBackgroundColor(integer);
                    }
                });

        return view;
    }

    @Override
    public void onDestroyView() {
        timeIndicator.unsubscribe();
        colorPrimarySubscription.dispose();
        super.onDestroyView();
    }
}
