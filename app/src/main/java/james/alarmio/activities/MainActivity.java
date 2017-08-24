package james.alarmio.activities;

import android.os.Bundle;

import com.afollestad.aesthetic.AestheticActivity;

import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.fragments.HomeFragment;
import james.alarmio.fragments.SplashFragment;

public class MainActivity extends AestheticActivity {

    private Alarmio alarmio;

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
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new HomeFragment())
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
}
