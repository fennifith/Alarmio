package james.alarmio.activities;

import android.os.Bundle;

import com.afollestad.aesthetic.AestheticActivity;

import james.alarmio.Alarmio;
import james.alarmio.R;

public class MainActivity extends AestheticActivity {

    private Alarmio alarmio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmio = (Alarmio) getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmio.onActivityResume();
    }
}
