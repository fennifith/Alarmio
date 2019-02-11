package me.jfenn.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import me.jfenn.alarmio.R;

public class AlertDialog extends AestheticDialog implements View.OnClickListener {

    private String title, content;
    private Listener listener;

    public AlertDialog(Context context) {
        super(context);
    }

    public AlertDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public AlertDialog setContent(String content) {
        this.content = content;
        return this;
    }

    public AlertDialog setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alert);

        TextView titleView = findViewById(R.id.title);
        TextView bodyView = findViewById(R.id.body);
        TextView okView = findViewById(R.id.ok);
        TextView cancelView = findViewById(R.id.cancel);

        if (title != null)
            titleView.setText(title);
        else titleView.setVisibility(View.GONE);

        bodyView.setText(content);

        okView.setOnClickListener(this);
        if (listener != null)
            cancelView.setOnClickListener(this);
        else cancelView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (listener != null)
            listener.onDismiss(this, v.getId() == R.id.ok);
    }

    public interface Listener {
        void onDismiss(AlertDialog dialog, boolean ok);
    }
}
