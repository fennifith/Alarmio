package james.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

import james.alarmio.R;
import james.alarmio.adapters.TimeZonesAdapter;

public class TimeZoneChooserDialog extends AppCompatDialog {

    private OnTimeZoneListener listener;

    public TimeZoneChooserDialog(Context context) {
        super(context);
    }

    public void setListener(OnTimeZoneListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_time_zone);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        TimeZonesAdapter adapter = new TimeZonesAdapter(new ArrayList<>(Arrays.asList(TimeZone.getAvailableIDs())));
        adapter.setOnClickListener(new TimeZonesAdapter.OnClickListener() {
            @Override
            public void onClick(String timeZone) {
                if (listener != null)
                    listener.onTimeZoneChosen(timeZone);

                dismiss();
            }
        });

        recycler.setAdapter(adapter);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public interface OnTimeZoneListener {
        void onTimeZoneChosen(String timeZone);
    }

}
