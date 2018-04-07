package james.alarmio.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import james.alarmio.R;

public class TimeZonesAdapter extends RecyclerView.Adapter<TimeZonesAdapter.ViewHolder> {

    private List<String> timeZones;
    private OnClickListener listener;

    public TimeZonesAdapter(List<String> timeZones) {
        this.timeZones = timeZones;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_zone, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZones.get(position));
        holder.time.setText(new SimpleDateFormat("ZZZZ", Locale.getDefault()).format(Calendar.getInstance(timeZone).getTime()));
        holder.title.setText(timeZone.getDisplayName(Locale.getDefault()));
        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onClick(timeZones.get(holder.getAdapterPosition()));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return timeZones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView time;
        private TextView title;

        public ViewHolder(View v) {
            super(v);
            time = v.findViewById(R.id.time);
            title = v.findViewById(R.id.title);
        }
    }

    public interface OnClickListener {
        void onClick(String timeZone);
    }

}
