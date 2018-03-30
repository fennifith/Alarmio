package james.alarmio.data.preference;

import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import james.alarmio.R;
import james.alarmio.data.PreferenceData;

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
    public void bindViewHolder(ViewHolder holder) {
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
