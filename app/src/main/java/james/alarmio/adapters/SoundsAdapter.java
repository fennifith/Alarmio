package james.alarmio.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.data.SoundData;
import james.alarmio.interfaces.SoundChooserListener;

public class SoundsAdapter extends RecyclerView.Adapter<SoundsAdapter.ViewHolder> {

    private Alarmio alarmio;
    private List<SoundData> sounds;
    private int currentlyPlaying = -1;

    private SoundChooserListener listener;

    public SoundsAdapter(Alarmio alarmio, List<SoundData> sounds) {
        this.alarmio = alarmio;
        this.sounds = sounds;
    }

    public void setListener(SoundChooserListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Observable<Integer> textColor;
        Observable<Integer> backgroundColor;

        if (position == 0) {
            holder.title.setText(R.string.title_sound_none);
            holder.icon.setOnClickListener(null);
            holder.icon.setImageResource(R.drawable.ic_ringtone_disabled);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onSoundChosen(null);
                }
            });

            textColor = Aesthetic.get().textColorPrimary();
            backgroundColor = Aesthetic.get().colorPrimary();
        } else {
            SoundData sound = sounds.get(position - 1);
            holder.title.setText(sound.getName());
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    SoundData sound = sounds.get(position - 1);
                    if (sound.isPlaying(alarmio)) {
                        sound.stop(alarmio);
                        currentlyPlaying = -1;
                    } else {
                        sound.preview(alarmio);

                        if (currentlyPlaying >= 0) {
                            sounds.get(currentlyPlaying - 1).stop(alarmio);
                            notifyItemChanged(currentlyPlaying);
                        }

                        currentlyPlaying = position;
                    }

                    notifyItemChanged(position);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onSoundChosen(sounds.get(holder.getAdapterPosition() - 1));
                }
            });

            if (sound.isPlaying(alarmio)) {
                holder.icon.setImageResource(R.drawable.ic_pause);

                textColor = Aesthetic.get().colorPrimary();
                backgroundColor = Aesthetic.get().textColorPrimary();
            } else {
                holder.icon.setImageResource(R.drawable.ic_play);

                textColor = Aesthetic.get().textColorPrimary();
                backgroundColor = Aesthetic.get().colorPrimary();
            }
        }

        textColor.take(1).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                holder.title.setTextColor(integer);
                holder.icon.setColorFilter(integer);
            }
        });

        backgroundColor.take(1).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                holder.itemView.setBackgroundColor(integer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sounds.size() + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
        }
    }

}
