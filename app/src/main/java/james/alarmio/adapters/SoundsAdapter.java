package james.alarmio.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.multimoon.colorful.ColorfulKt;
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
        if (position == 0) {
            holder.title.setText(R.string.title_sound_none);
            holder.icon.setOnClickListener(null);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onSoundChosen(null);
                }
            });

            setPlaying(holder, false, false);
            holder.icon.setImageResource(R.drawable.ic_ringtone_disabled);
        } else {
            SoundData sound = sounds.get(position - 1);
            holder.title.setText(sound.getName());
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    SoundData sound = sounds.get(position - 1);
                    if (sound.isPlaying(alarmio) || currentlyPlaying == position) {
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

                    setPlaying(holder, currentlyPlaying == position, true);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onSoundChosen(sounds.get(holder.getAdapterPosition() - 1));
                }
            });

            setPlaying(holder, sound.isPlaying(alarmio), false);
        }
    }

    private void setPlaying(final ViewHolder holder, final boolean isPlaying, final boolean isAnimated) {
        int color = isPlaying ? ColorfulKt.Colorful().getPrimaryColor().getColorPack().normal().asInt()
                : alarmio.getTextColor();
        int textColor = alarmio.getTextColor(true, true);

        if (isAnimated) {
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), holder.title.getTextColors().getDefaultColor(), color);
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int color = (int) valueAnimator.getAnimatedValue();
                    holder.title.setTextColor(color);
                    holder.icon.setColorFilter(color);
                }
            });
            animator.start();

            ValueAnimator animator2 = ValueAnimator.ofObject(new ArgbEvaluator(), isPlaying ? Color.TRANSPARENT : textColor, isPlaying ? textColor : Color.TRANSPARENT);
            animator2.setDuration(300);
            animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    holder.itemView.setBackgroundColor((int) valueAnimator.getAnimatedValue());
                }
            });
            animator2.start();

            AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(alarmio, isPlaying ? R.drawable.ic_play_to_pause : R.drawable.ic_pause_to_play);
            if (drawable != null) {
                holder.icon.setImageDrawable(drawable);
                drawable.start();
                return;
            }
        } else {
            holder.title.setTextColor(color);
            holder.icon.setColorFilter(color);
            holder.itemView.setBackgroundColor(isPlaying ? textColor : Color.TRANSPARENT);
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
