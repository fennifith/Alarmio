package me.jfenn.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;

import com.afollestad.aesthetic.Aesthetic;

import androidx.appcompat.app.AppCompatDialog;

public abstract class AestheticDialog extends AppCompatDialog {

    public AestheticDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Aesthetic.Companion.get()
                .colorPrimary()
                .take(1)
                .subscribe(integer -> findViewById(android.R.id.content).setBackgroundColor(integer));
    }
}
