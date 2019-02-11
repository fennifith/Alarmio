package me.jfenn.alarmio.activities;

import android.content.Intent;
import android.os.Bundle;

import com.afollestad.aesthetic.AestheticActivity;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.fragments.BaseFragment;
import me.jfenn.alarmio.fragments.HomeFragment;
import me.jfenn.alarmio.fragments.SplashFragment;
import me.jfenn.alarmio.fragments.StopwatchFragment;
import me.jfenn.alarmio.fragments.TimerFragment;
import me.jfenn.alarmio.receivers.TimerReceiver;

public class MainActivity extends AestheticActivity implements FragmentManager.OnBackStackChangedListener, Alarmio.ActivityListener {

    public static final String EXTRA_FRAGMENT = "james.alarmio.MainActivity.EXTRA_FRAGMENT";
    public static final int FRAGMENT_TIMER = 0;
    public static final int FRAGMENT_STOPWATCH = 2;

    private Alarmio alarmio;
    private BaseFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmio = (Alarmio) getApplicationContext();
        alarmio.setListener(this);

        if (savedInstanceState == null) {
            fragment = new SplashFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, fragment)
                    .commit();
        } else {
            if (fragment == null)
                fragment = new HomeFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(EXTRA_FRAGMENT)) {
            boolean shouldBackStack = fragment instanceof HomeFragment;

            int fragmentId = intent.getIntExtra(EXTRA_FRAGMENT, -1);
            if (fragmentId == FRAGMENT_TIMER && intent.hasExtra(TimerReceiver.EXTRA_TIMER_ID)) {
                int id = intent.getIntExtra(TimerReceiver.EXTRA_TIMER_ID, 0);
                if (alarmio.getTimers().size() <= id || id < 0)
                    return;

                Bundle args = new Bundle();
                args.putParcelable(TimerFragment.EXTRA_TIMER, alarmio.getTimers().get(id));

                fragment = new TimerFragment();
                fragment.setArguments(args);
            } else if (fragmentId == FRAGMENT_STOPWATCH) {
                if (fragment instanceof StopwatchFragment)
                    return;
                fragment = new StopwatchFragment();
            } else return;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                    .replace(R.id.fragment, fragment);

            if (shouldBackStack)
                transaction.addToBackStack(null);

            transaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alarmio != null)
            alarmio.setListener(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(new Bundle());
    }

    @Override
    protected void onPause() {
        super.onPause();
        alarmio.stopCurrentSound();
    }

    @Override
    public void onBackStackChanged() {
        fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    @Override
    public FragmentManager gettFragmentManager() {
        return getSupportFragmentManager();
    }
}
