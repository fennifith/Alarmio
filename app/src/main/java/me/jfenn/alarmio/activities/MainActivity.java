package me.jfenn.alarmio.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;

import com.afollestad.aesthetic.AestheticActivity;

import androidx.annotation.NonNull;
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

    public static final String EXTRA_FRAGMENT = "me.jfenn.alarmio.MainActivity.EXTRA_FRAGMENT";
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
            fragment = createFragmentFor(getIntent());

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
        if (isActionableIntent(intent)) {
            FragmentManager manager = getSupportFragmentManager();
            BaseFragment fragment = createFragmentFor(intent);

            if (fragment.equals(this.fragment)) // check that fragment isn't already displayed
                return;

            if (fragment instanceof HomeFragment && manager.getBackStackEntryCount() > 0) // clear the back stack
                manager.popBackStack(manager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = manager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                    .replace(R.id.fragment, fragment);

            if (this.fragment instanceof HomeFragment && !(fragment instanceof HomeFragment))
                transaction.addToBackStack(null);

            this.fragment = fragment;

            transaction.commit();
        }
    }

    /**
     * Return a fragment to display the content provided by
     * the passed intent.
     *
     * @param intent    The intent passed to the activity.
     * @return          An instantiated fragment corresponding
     *                  to the passed intent.
     */
    @NonNull
    private BaseFragment createFragmentFor(Intent intent) {
        int fragmentId = intent.getIntExtra(EXTRA_FRAGMENT, -1);
        switch (fragmentId) {
            case FRAGMENT_STOPWATCH:
                if (fragment instanceof StopwatchFragment)
                    return fragment;

                return new StopwatchFragment();
            case FRAGMENT_TIMER:
                if (intent.hasExtra(TimerReceiver.EXTRA_TIMER_ID)) {
                    int id = intent.getIntExtra(TimerReceiver.EXTRA_TIMER_ID, 0);
                    if (alarmio.getTimers().size() <= id || id < 0)
                        return fragment;

                    Bundle args = new Bundle();
                    args.putParcelable(TimerFragment.EXTRA_TIMER, alarmio.getTimers().get(id));

                    BaseFragment fragment = new TimerFragment();
                    fragment.setArguments(args);
                    return fragment;
                }

                return fragment;
            default:
                if (Intent.ACTION_MAIN.equals(intent.getAction()) || intent.getAction() == null)
                    return new SplashFragment();

                Bundle args = new Bundle();
                args.putString(HomeFragment.INTENT_ACTION, intent.getAction());
                BaseFragment fragment = new HomeFragment();
                fragment.setArguments(args);
                return fragment;
        }
    }

    /**
     * Determine if something needs to be done as a result
     * of the intent being sent to the activity - which has
     * a higher priority than any fragment that is currently
     * open.
     *
     * @param intent    The intent passed to the activity.
     * @return          True if a fragment should be replaced
     *                  with the action that this intent entails.
     */
    private boolean isActionableIntent(Intent intent) {
        return intent.hasExtra(EXTRA_FRAGMENT)
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                        && (AlarmClock.ACTION_SHOW_ALARMS.equals(intent.getAction())
                        || AlarmClock.ACTION_SET_TIMER.equals(intent.getAction()))
                || AlarmClock.ACTION_SET_ALARM.equals(intent.getAction())
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        && (AlarmClock.ACTION_SHOW_TIMERS.equals(intent.getAction()))));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alarmio != null)
            alarmio.setListener(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState != null ? outState : new Bundle());
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
