package james.alarmio.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.R;
import james.alarmio.adapters.AlarmsAdapter;

public class AlarmsFragment extends BasePagerFragment {

    private RecyclerView recyclerView;

    private AlarmsAdapter adapter;

    private Disposable colorAccentSubscription;
    private Disposable colorForegroundSubscription;
    private Disposable textColorPrimarySubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter = new AlarmsAdapter(getAlarmio(), getContext(), getAlarmio().getPrefs(), getAlarmio().getAlarms());
        recyclerView.setAdapter(adapter);

        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        adapter.setColorAccent(integer);
                    }
                });

        colorForegroundSubscription = Aesthetic.get()
                .colorCardViewBackground()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        adapter.setColorForeground(integer);
                    }
                });

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        adapter.setTextColorPrimary(integer);
                    }
                });
        return view;
    }

    @Override
    public void onDestroyView() {
        colorAccentSubscription.dispose();
        colorForegroundSubscription.dispose();
        textColorPrimarySubscription.dispose();
        super.onDestroyView();
    }

    @Override
    public String getTitle() {
        return "Alarms";
    }

    @Override
    public void onAlarmsChanged() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }
}
