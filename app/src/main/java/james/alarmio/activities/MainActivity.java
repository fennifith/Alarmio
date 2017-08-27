package james.alarmio.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.afollestad.aesthetic.AestheticActivity;

import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.fragments.BaseFragment;
import james.alarmio.fragments.HomeFragment;
import james.alarmio.fragments.SplashFragment;

public class MainActivity extends AestheticActivity implements FragmentManager.OnBackStackChangedListener {

    private Alarmio alarmio;
    private BaseFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmio = (Alarmio) getApplicationContext();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, new SplashFragment())
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
