package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.multimoon.colorful.ColorfulKt;
import james.alarmio.R;
import james.alarmio.adapters.AlarmsAdapter;

public class AlarmsFragment extends BasePagerFragment {

    private RecyclerView recyclerView;
    private View empty;

    private AlarmsAdapter alarmsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        empty = v.findViewById(R.id.empty);
        ((TextView) v.findViewById(R.id.emptyText)).setText(R.string.msg_alarms_empty);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        alarmsAdapter = new AlarmsAdapter(getAlarmio(), recyclerView, getFragmentManager());
        recyclerView.setAdapter(alarmsAdapter);

        alarmsAdapter.setColorAccent(ColorfulKt.Colorful().getAccentColor().getColorPack().normal().asInt());
        alarmsAdapter.setColorForeground(ColorfulKt.Colorful().getPrimaryColor().getColorPack().normal().asInt());
        alarmsAdapter.setTextColorPrimary(getAlarmio().getTextColor());

        onChanged();
        return v;
    }

    @Override
    public String getTitle() {
        return "Alarms";
    }

    @Override
    public void onAlarmsChanged() {
        if (alarmsAdapter != null) {
            alarmsAdapter.notifyDataSetChanged();
            onChanged();
        }
    }

    @Override
    public void onTimersChanged() {
        if (alarmsAdapter != null) {
            alarmsAdapter.notifyDataSetChanged();
            onChanged();
        }
    }

    private void onChanged() {
        if (empty != null && alarmsAdapter != null)
            empty.setVisibility(alarmsAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

}
