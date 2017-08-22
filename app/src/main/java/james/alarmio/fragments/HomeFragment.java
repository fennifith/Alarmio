package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.adapters.SimplePagerAdapter;
import james.alarmio.views.DigitalTimeView;

public class HomeFragment extends BaseFragment {

    private View view;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private DigitalTimeView digitalTime;

    private Disposable colorPrimarySubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        digitalTime = view.findViewById(R.id.digitalTime);

        viewPager.setAdapter(new SimplePagerAdapter(getChildFragmentManager(), new AlarmsFragment(), new TimersFragment()));
        tabLayout.setupWithViewPager(viewPager);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                BottomSheetBehavior behavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheet));
                behavior.setHideable(false);
                behavior.setPeekHeight(view.getMeasuredHeight() / 2);

                digitalTime.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.getMeasuredHeight() / 2));
            }
        });

        colorPrimarySubscription = Aesthetic.get()
                .colorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        view.findViewById(R.id.bottomSheet).setBackgroundColor(integer);
                    }
                });

        return view;
    }

    @Override
    public void onDestroyView() {
        colorPrimarySubscription.dispose();
        super.onDestroyView();
    }
}
