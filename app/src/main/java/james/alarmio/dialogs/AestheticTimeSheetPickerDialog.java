package james.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.functions.Consumer;
import james.alarmio.utils.ColorUtils;
import me.jfenn.timedatepickers.dialogs.TimeSheetPickerDialog;

public class AestheticTimeSheetPickerDialog extends TimeSheetPickerDialog {

    public AestheticTimeSheetPickerDialog(Context context) {
        super(context);
    }

    public AestheticTimeSheetPickerDialog(Context context, int hourOfDay, int minute) {
        super(context, hourOfDay, minute);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Aesthetic.get()
                .textColorPrimary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        getView().setPrimaryTextColor(integer);
                    }
                });

        Aesthetic.get()
                .textColorSecondary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        getView().setSecondaryTextColor(integer);
                    }
                });

        Aesthetic.get().colorPrimary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        getView().setBackgroundColor(integer, integer);
                    }
                });

        Aesthetic.get().colorAccent()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        getView().setSelectionColor(integer);
                        getView().setSelectedTextColor(ColorUtils.getPrimaryTextColor(getContext(), integer));
                    }
                });
    }
}
