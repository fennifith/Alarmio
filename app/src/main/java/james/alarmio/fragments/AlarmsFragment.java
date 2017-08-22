package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import james.alarmio.R;
import james.alarmio.adapters.AlarmsAdapter;

public class AlarmsFragment extends BasePagerFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setAdapter(new AlarmsAdapter(recyclerView, getAlarmio().getPrefs(), getAlarmio().getAlarms()));
        return view;
    }

    @Override
    public String getTitle() {
        return "Alarms";
    }

}
