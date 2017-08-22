package james.alarmio.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public abstract class BaseSubscriptionView extends View {

    public BaseSubscriptionView(Context context) {
        this(context, null, 0);
    }

    public BaseSubscriptionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSubscriptionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void subscribe();

    public abstract void unsubscribe();

}
