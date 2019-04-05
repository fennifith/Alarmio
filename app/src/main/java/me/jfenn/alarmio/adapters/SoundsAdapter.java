package me.jfenn.alarmio.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.SoundData;
import me.jfenn.alarmio.interfaces.SoundChooserListener;

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
        if (position == 0) {
            holder.title.setText(R.string.title_sound_none);
            holder.icon.setOnClickListener(null);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onSoundChosen(null);
            });

            setPlaying(holder, false, false);
            holder.icon.setImageResource(R.drawable.ic_ringtone_disabled);
        } else {
            SoundData sound = sounds.get(position - 1);
            holder.title.setText(sound.getName());
            holder.icon.setOnClickListener(v -> {
                int position1 = holder.getAdapterPosition();
                SoundData sound1 = sounds.get(position1 - 1);
                if (sound1.isPlaying(alarmio) || currentlyPlaying == position1) {
                    sound1.stop(alarmio);
                    currentlyPlaying = -1;
                } else {
                    sound1.preview(alarmio);

                    if (currentlyPlaying >= 0) {
                        sounds.get(currentlyPlaying - 1).stop(alarmio);
                        notifyItemChanged(currentlyPlaying);
                    }

                    currentlyPlaying = position1;
                }

                setPlaying(holder, currentlyPlaying == position1, true);
            });

            holder.itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onSoundChosen(sounds.get(holder.getAdapterPosition() - 1));
            });

            setPlaying(holder, sound.isPlaying(alarmio), false);
        }
    }

    private void setPlaying(final ViewHolder holder, final boolean isPlaying, final boolean isAnimated) {
        (isPlaying ? Aesthetic.Companion.get().colorPrimary() : Aesthetic.Companion.get().textColorPrimary()).take(1).subscribe(integer -> {
            if (isAnimated) {
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), holder.title.getTextColors().getDefaultColor(), integer);
                animator.setDuration(300);
                animator.addUpdateListener(valueAnimator -> {
                    int color = (int) valueAnimator.getAnimatedValue();
                    holder.title.setTextColor(color);
                    holder.icon.setColorFilter(color);
                });
                animator.start();
            } else {
                holder.title.setTextColor(integer);
                holder.icon.setColorFilter(integer);
            }
        });

        Aesthetic.Companion.get().textColorPrimary().take(1).subscribe(integer -> {
            if (isAnimated) {
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), isPlaying ? Color.TRANSPARENT : integer, isPlaying ? integer : Color.TRANSPARENT);
                animator.setDuration(300);
                animator.addUpdateListener(valueAnimator -> holder.itemView.setBackgroundColor((int) valueAnimator.getAnimatedValue()));
                animator.start();
            } else holder.itemView.setBackgroundColor(isPlaying ? integer : Color.TRANSPARENT);
        });

        if (isAnimated) {
            AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(alarmio, isPlaying ? R.drawable.ic_play_to_pause : R.drawable.ic_pause_to_play);
            if (drawable != null) {
                holder.icon.setImageDrawable(drawable);
                drawable.start();
                return;
            }
        }

        holder.icon.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
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
