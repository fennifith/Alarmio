package me.jfenn.alarmio.data.preference;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.CompoundButtonCompat;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.PreferenceData;

public class BooleanPreferenceData extends BasePreferenceData<BooleanPreferenceData.ViewHolder> {

    private PreferenceData preference;
    private int title;
    private int description;

    public BooleanPreferenceData(PreferenceData preference, @StringRes int title, @StringRes int description) {
        this.preference = preference;
        this.title = title;
        this.description = description;
    }

    @Override
    public BasePreferenceData.ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_boolean, parent, false));
    }

    @Override
    public void bindViewHolder(final ViewHolder holder) {
        holder.title.setText(title);
        holder.description.setText(description);
        holder.toggle.setOnCheckedChangeListener(null);

        Boolean value = preference.getValue(holder.itemView.getContext());
        holder.toggle.setChecked(value != null ? value : false);
        holder.toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preference.setValue(compoundButton.getContext(), b);
            }
        });

        Aesthetic.Companion.get()
                .colorAccent()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(final Integer colorAccent) throws Exception {
                        Aesthetic.Companion.get()
                                .textColorPrimary()
                                .take(1)
                                .subscribe(new Consumer<Integer>() {
                                    @Override
                                    public void accept(Integer textColorPrimary) throws Exception {
                                        ColorStateList colorStateList = new ColorStateList(
                                                new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                                                new int[]{
                                                        Color.argb(100, Color.red(textColorPrimary), Color.green(textColorPrimary), Color.blue(textColorPrimary)),
                                                        colorAccent
                                                }
                                        );

                                        CompoundButtonCompat.setButtonTintList(holder.toggle, colorStateList);
                                        holder.toggle.setTextColor(textColorPrimary);
                                    }
                                });
                    }
                });
    }

    public class ViewHolder extends BasePreferenceData.ViewHolder {

        private TextView title;
        private TextView description;
        private SwitchCompat toggle;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            description = v.findViewById(R.id.description);
            toggle = v.findViewById(R.id.toggle);
        }

    }

}
