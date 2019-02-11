package me.jfenn.alarmio.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.interfaces.Subscribblable;
import me.jfenn.slideactionview.SlideActionView;

public class AestheticSlideActionView extends SlideActionView implements Subscribblable {

    private Disposable textColorPrimarySubscription;
    private Disposable textColorPrimaryInverseSubscription;

    public AestheticSlideActionView(Context context) {
        super(context);
        subscribe();
    }

    public AestheticSlideActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        subscribe();
    }

    public AestheticSlideActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        subscribe();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AestheticSlideActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        subscribe();
    }

    @Override
    public void subscribe() {
        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setTouchHandleColor(integer);
                        setOutlineColor(integer);
                        setIconColor(integer);
                        postInvalidate();
                    }
                });

        textColorPrimaryInverseSubscription = Aesthetic.Companion.get()
                .textColorPrimaryInverse()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setBackgroundColor((100 << 24) | (integer & 0x00ffffff));
                    }
                });
    }

    @Override
    public void unsubscribe() {
        if (textColorPrimarySubscription != null)
            textColorPrimarySubscription.dispose();
        if (textColorPrimaryInverseSubscription != null)
            textColorPrimaryInverseSubscription.dispose();
    }
}
