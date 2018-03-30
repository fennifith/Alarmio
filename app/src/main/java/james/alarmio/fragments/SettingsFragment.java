package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import james.alarmio.R;
import james.alarmio.adapters.PreferenceAdapter;
import james.alarmio.data.PreferenceData;
import james.alarmio.data.preference.BasePreferenceData;
import james.alarmio.data.preference.BooleanPreferenceData;
import james.alarmio.data.preference.ThemePreferenceData;

public class SettingsFragment extends BasePagerFragment {

    private RecyclerView recyclerView;

    private PreferenceAdapter preferenceAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        preferenceAdapter = new PreferenceAdapter(new ArrayList<BasePreferenceData>(Arrays.asList(
                new ThemePreferenceData(),
                new BooleanPreferenceData(PreferenceData.SLEEP_REMINDER, R.string.title_sleep_reminder, R.string.desc_sleep_reminder),
                new BooleanPreferenceData(PreferenceData.SLOW_WAKE_UP, R.string.title_slow_wake_up, R.string.desc_slow_wake_up)
        )));
        recyclerView.setAdapter(preferenceAdapter);

        return recyclerView;
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

}
