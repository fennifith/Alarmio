package james.alarmio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.afollestad.aesthetic.AestheticActivity;

import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.fragments.BaseFragment;
import james.alarmio.fragments.HomeFragment;
import james.alarmio.fragments.SplashFragment;
import james.alarmio.fragments.TimerFragment;
import james.alarmio.receivers.TimerReceiver;

public class MainActivity extends AestheticActivity implements FragmentManager.OnBackStackChangedListener {

    private Alarmio alarmio;
    private BaseFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmio = (Alarmio) getApplicationContext();

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
        if (intent.hasExtra(TimerReceiver.EXTRA_TIMER_ID)) {
            Bundle args = new Bundle();
            args.putParcelable(TimerFragment.EXTRA_TIMER, alarmio.getTimers().get(intent.getIntExtra(TimerReceiver.EXTRA_TIMER_ID, 0)));

            TimerFragment fragment = new TimerFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                    .replace(R.id.fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(new Bundle());
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmio.onActivityResume();
    }

    @Override
    public void onBackStackChanged() {
        fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }
}
