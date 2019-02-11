package me.jfenn.alarmio.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.jfenn.alarmio.data.preference.BasePreferenceData;

public class PreferenceAdapter extends RecyclerView.Adapter<BasePreferenceData.ViewHolder> {

    private List<BasePreferenceData> items;

    public PreferenceAdapter(List<BasePreferenceData> items) {
        this.items = items;
    }

    @Override
    public BasePreferenceData.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return items.get(viewType).getViewHolder(LayoutInflater.from(parent.getContext()), parent);
    }

    @Override
    public void onBindViewHolder(BasePreferenceData.ViewHolder holder, int position) {
        items.get(position).bindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
