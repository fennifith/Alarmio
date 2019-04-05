package me.jfenn.alarmio.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.PreferenceData;

public class TimeZonesAdapter extends RecyclerView.Adapter<TimeZonesAdapter.ViewHolder> {

    private List<String> timeZones;

    public TimeZonesAdapter(List<String> timeZones) {
        this.timeZones = timeZones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_zone, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZones.get(position));

        int offsetMillis = timeZone.getRawOffset();
        holder.time.setText(String.format(
                Locale.getDefault(),
                "GMT%s%02d:%02d",
                offsetMillis >= 0 ? "+" : "",
                TimeUnit.MILLISECONDS.toHours(offsetMillis),
                TimeUnit.MILLISECONDS.toMinutes(Math.abs(offsetMillis)) % TimeUnit.HOURS.toMinutes(1)
        ));

        holder.title.setText(timeZone.getDisplayName(Locale.getDefault()));

        holder.itemView.setOnClickListener(v -> holder.checkBox.toggle());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked((boolean) PreferenceData.TIME_ZONE_ENABLED.getSpecificValue(holder.itemView.getContext(), timeZone.getID()));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TimeZone timeZone1 = TimeZone.getTimeZone(timeZones.get(holder.getAdapterPosition()));
            PreferenceData.TIME_ZONE_ENABLED.setValue(holder.itemView.getContext(), isChecked, timeZone1.getID());
        });
    }

    @Override
    public int getItemCount() {
        return timeZones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView time;
        private TextView title;
        private AppCompatCheckBox checkBox;

        public ViewHolder(View v) {
            super(v);
            time = v.findViewById(R.id.time);
            title = v.findViewById(R.id.title);
            checkBox = v.findViewById(R.id.checkbox);
        }
    }

}
