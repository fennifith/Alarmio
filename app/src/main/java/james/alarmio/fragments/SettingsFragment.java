package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.aesthetic.Aesthetic;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.adapters.PreferenceAdapter;
import james.alarmio.data.PreferenceData;
import james.alarmio.data.preference.BasePreferenceData;
import james.alarmio.data.preference.BooleanPreferenceData;
import james.alarmio.data.preference.CustomPreferenceData;
import james.alarmio.data.preference.RingtonePreferenceData;
import james.alarmio.data.preference.ThemePreferenceData;
import james.alarmio.data.preference.TimePreferenceData;
import james.alarmio.data.preference.TimeZonesPreferenceData;
import me.jfenn.attribouter.Attribouter;

public class SettingsFragment extends BasePagerFragment implements Consumer {

    private RecyclerView recyclerView;

    private PreferenceAdapter preferenceAdapter;

    private Disposable colorPrimarySubscription;
    private Disposable textColorPrimarySubscription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        preferenceAdapter = new PreferenceAdapter(new ArrayList<BasePreferenceData>(Arrays.asList(
                new ThemePreferenceData(),
                new TimeZonesPreferenceData(PreferenceData.TIME_ZONES, R.string.title_time_zones),
                new RingtonePreferenceData(PreferenceData.DEFAULT_ALARM_RINGTONE, R.string.title_default_alarm_ringtone),
                new RingtonePreferenceData(PreferenceData.DEFAULT_TIMER_RINGTONE, R.string.title_default_timer_ringtone),
                new BooleanPreferenceData(PreferenceData.SLEEP_REMINDER, R.string.title_sleep_reminder, R.string.desc_sleep_reminder),
                new TimePreferenceData(PreferenceData.SLEEP_REMINDER_TIME, R.string.title_sleep_reminder_time),
                new BooleanPreferenceData(PreferenceData.SLOW_WAKE_UP, R.string.title_slow_wake_up, R.string.desc_slow_wake_up),
                new TimePreferenceData(PreferenceData.SLOW_WAKE_UP_TIME, R.string.title_slow_wake_up_time),
                new CustomPreferenceData(R.string.title_about) {
                    @Override
                    public String getValueName(ViewHolder holder) {
                        return "";
                    }

                    @Override
                    public void onClick(ViewHolder holder) {
                        Attribouter attribouter = Attribouter.from(getContext());
                        int githubAuthKey = getResources().getIdentifier("githubAuthKey", "string", getContext().getPackageName());
                        if (githubAuthKey != 0)
                            attribouter = attribouter.withGitHubToken(getString(githubAuthKey));

                        attribouter.show();
                    }
                }
        )));
        recyclerView.setAdapter(preferenceAdapter);

        colorPrimarySubscription = Aesthetic.get()
                .colorPrimary()
                .subscribe(this);

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(this);

        return recyclerView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        colorPrimarySubscription.dispose();
        textColorPrimarySubscription.dispose();
    }

    @Override
    public String getTitle() {
        return "Settings";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (preferenceAdapter != null)
            preferenceAdapter.notifyDataSetChanged();
    }

    @Override
    public void accept(Object o) throws Exception {
        if (preferenceAdapter != null)
            preferenceAdapter.notifyDataSetChanged();
    }

}
