package james.alarmio.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.functions.Consumer;

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
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        findViewById(android.R.id.content).setBackgroundColor(integer);
                    }
                });
    }
}
