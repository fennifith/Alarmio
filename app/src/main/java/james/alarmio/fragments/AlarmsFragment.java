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

    private AlarmsAdapter alarmsAdapter;

    private Disposable colorAccentSubscription;
    private Disposable colorForegroundSubscription;
    private Disposable textColorPrimarySubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_alarms, container, false);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        alarmsAdapter = new AlarmsAdapter(getAlarmio(), getContext(), getFragmentManager());
        recyclerView.setAdapter(alarmsAdapter);

        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        alarmsAdapter.setColorAccent(integer);
                    }
                });

        colorForegroundSubscription = Aesthetic.get()
                .colorCardViewBackground()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        alarmsAdapter.setColorForeground(integer);
                    }
                });

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        alarmsAdapter.setTextColorPrimary(integer);
                    }
                });
        return recyclerView;
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
        if (alarmsAdapter != null)
            alarmsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTimersChanged() {
        if (alarmsAdapter != null)
            alarmsAdapter.notifyDataSetChanged();
    }
}
