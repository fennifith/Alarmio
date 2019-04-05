package me.jfenn.alarmio.views;

import android.content.Context;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import me.jfenn.alarmio.interfaces.Subscribblable;
import me.jfenn.sunrisesunsetview.SunriseSunsetView;

public class AestheticSunriseView extends SunriseSunsetView implements Subscribblable {

    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;

    public AestheticSunriseView(Context context) {
        super(context);
        subscribe();
        setClickable(false);
        setFocusable(false);
    }

    public AestheticSunriseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        subscribe();
        setClickable(false);
        setFocusable(false);
    }

    public AestheticSunriseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        subscribe();
        setClickable(false);
        setFocusable(false);
    }

    @Override
    public void subscribe() {
        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> {
                    setSunsetColor(((byte) 200 << 24) | (integer & 0x00FFFFFF));
                    setFutureColor(((byte) 20 << 24) | (integer & 0x00FFFFFF));
                    postInvalidate();
                });

        colorAccentSubscription = Aesthetic.Companion.get()
                .colorAccent()
                .subscribe(integer -> setSunriseColor(((byte) 200 << 24) | (integer & 0x00FFFFFF)));
    }

    @Override
    public void unsubscribe() {
        textColorPrimarySubscription.dispose();
        colorAccentSubscription.dispose();
    }
}
