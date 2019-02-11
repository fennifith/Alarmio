package me.jfenn.alarmio.data.preference;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import androidx.appcompat.widget.AppCompatSpinner;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.PreferenceData;

public class SpinnerPreferenceData extends BasePreferenceData<SpinnerPreferenceData.ViewHolder> {

    private PreferenceData preference;
    private int title;
    private int options;

    public SpinnerPreferenceData(PreferenceData preference, int title, int options) {
        this.preference = preference;
        this.title = title;
        this.options = options;
    }

    @Override
    public BasePreferenceData.ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_spinner, parent, false));
    }

    @Override
    public void bindViewHolder(final ViewHolder holder) {
        holder.title.setText(title);
        holder.spinner.setAdapter(ArrayAdapter.createFromResource(holder.itemView.getContext(), options, R.layout.support_simple_spinner_dropdown_item));

        Integer option = preference.getValue(holder.itemView.getContext());
        if (option != null)
            holder.spinner.setSelection(option);

        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                preference.setValue(adapterView.getContext(), i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Aesthetic.Companion.get()
                .textColorSecondary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer textColorSecondary) throws Exception {
                        holder.spinner.setSupportBackgroundTintList(ColorStateList.valueOf(textColorSecondary));
                    }
                });

        Aesthetic.Companion.get()
                .colorCardViewBackground()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer colorForeground) throws Exception {
                        holder.spinner.setPopupBackgroundDrawable(new ColorDrawable(colorForeground));
                    }
                });
    }

    public class ViewHolder extends BasePreferenceData.ViewHolder {

        private TextView title;
        private AppCompatSpinner spinner;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            spinner = v.findViewById(R.id.spinner);
        }
    }

}
