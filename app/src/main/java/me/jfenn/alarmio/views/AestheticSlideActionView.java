package me.jfenn.alarmio.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import me.jfenn.alarmio.interfaces.Subscribblable;
import me.jfenn.slideactionview.SlideActionView;

public class AestheticSlideActionView extends SlideActionView implements Subscribblable {

    private Disposable textColorPrimarySubscription;
    private Disposable textColorPrimaryInverseSubscription;

    public AestheticSlideActionView(Context context) {
        super(context);
    }

    public AestheticSlideActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AestheticSlideActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AestheticSlideActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void subscribe() {
        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> {
                    setTouchHandleColor(integer);
                    setOutlineColor(integer);
                    setIconColor(integer);
                    postInvalidate();
                });

        textColorPrimaryInverseSubscription = Aesthetic.Companion.get()
                .textColorPrimaryInverse()
                .subscribe(integer -> setBackgroundColor((100 << 24) | (integer & 0x00ffffff)));
    }

    @Override
    public void unsubscribe() {
        if (textColorPrimarySubscription != null)
            textColorPrimarySubscription.dispose();
        if (textColorPrimaryInverseSubscription != null)
            textColorPrimaryInverseSubscription.dispose();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscribe();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unsubscribe();
    }
}
