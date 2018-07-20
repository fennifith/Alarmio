package james.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;

import james.alarmio.Alarmio;

public abstract class AestheticDialog extends AppCompatDialog {

    private Alarmio alarmio;

    public AestheticDialog(Context context) {
        super(context);
        alarmio = (Alarmio) context.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //findViewById(android.R.id.content).setBackgroundColor(ColorfulKt.Colorful().getPrimaryColor().getColorPack().normal().asInt());
    }

    Alarmio getAlarmio() {
        return alarmio;
    }
}
