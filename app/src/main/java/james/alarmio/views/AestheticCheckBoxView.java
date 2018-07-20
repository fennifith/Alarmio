package james.alarmio.views;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import james.alarmio.Alarmio;
import james.alarmio.interfaces.Subscribblable;

public class AestheticCheckBoxView extends AppCompatCheckBox implements Subscribblable {

    private Alarmio alarmio;

    public AestheticCheckBoxView(Context context) {
        super(context);
        alarmio = (Alarmio) getContext().getApplicationContext();
        subscribe();
    }

    public AestheticCheckBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        alarmio = (Alarmio) getContext().getApplicationContext();
        subscribe();
    }

    public AestheticCheckBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        alarmio = (Alarmio) getContext().getApplicationContext();
        subscribe();
    }

    @Override
    public void subscribe() {
        /*int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};

        ColorStateList colorStateList = new ColorStateList(
                states,
                new int[]{
                        Color.argb(255, 128, 128, 128),
                        ColorfulKt.Colorful().getPrimaryColor().getColorPack().normal().asInt()
                }
        );

        CompoundButtonCompat.setButtonTintList(AestheticCheckBoxView.this, colorStateList);

        setTextColor(alarmio.getTextColor());*/
    }

    @Override
    public void unsubscribe() {
    }
}
