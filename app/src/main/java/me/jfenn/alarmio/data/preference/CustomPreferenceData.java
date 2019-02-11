package me.jfenn.alarmio.data.preference;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.jfenn.alarmio.R;

public abstract class CustomPreferenceData extends BasePreferenceData<CustomPreferenceData.ViewHolder> {

    private int name;

    public CustomPreferenceData(int name) {
        this.name = name;
    }

    public abstract String getValueName(ViewHolder holder);

    public abstract void onClick(ViewHolder holder);

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_custom, parent, false));
    }

    @Override
    public void bindViewHolder(final ViewHolder holder) {
        holder.nameView.setText(name);
        holder.valueNameView.setText(getValueName(holder));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomPreferenceData.this.onClick(holder);
            }
        });
    }

    public static class ViewHolder extends BasePreferenceData.ViewHolder {

        private TextView nameView;
        private TextView valueNameView;

        public ViewHolder(View v) {
            super(v);
            nameView = v.findViewById(R.id.name);
            valueNameView = v.findViewById(R.id.value);
        }

    }

}
