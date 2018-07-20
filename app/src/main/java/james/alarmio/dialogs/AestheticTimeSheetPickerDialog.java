package james.alarmio.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import io.multimoon.colorful.ColorfulKt;
import james.alarmio.Alarmio;
import me.jfenn.timedatepickers.dialogs.TimeSheetPickerDialog;

public class AestheticTimeSheetPickerDialog extends TimeSheetPickerDialog {

    private Alarmio alarmio;

    public AestheticTimeSheetPickerDialog(Context context) {
        super(context);
        alarmio = (Alarmio) context.getApplicationContext();
    }

    public AestheticTimeSheetPickerDialog(Context context, int hourOfDay, int minute) {
        super(context, hourOfDay, minute);
        alarmio = (Alarmio) context.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPrimaryTextColor(alarmio.getTextColor());
        setSecondaryTextColor(alarmio.getTextColor(false, false));

        int colorPrimary = ColorfulKt.Colorful().getPrimaryColor().getColorPack().normal().asInt();
        setBackgroundColor(colorPrimary);
        setPrimaryBackgroundColor(colorPrimary);
        setSecondaryBackgroundColor(colorPrimary);

        int colorAccent = ColorfulKt.Colorful().getAccentColor().getColorPack().normal().asInt();
        setSelectionColor(colorAccent);
        setSelectionTextColor(((Alarmio) getContext().getApplicationContext()).getActivityTheme() == Alarmio.THEME_AMOLED ? Color.BLACK : Color.WHITE);
    }
}
