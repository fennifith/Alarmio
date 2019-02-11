package me.jfenn.alarmio.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.afollestad.aesthetic.Aesthetic;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.interfaces.Subscribblable;
import me.jfenn.alarmio.utils.FormatUtils;

public class DigitalClockView extends View implements ViewTreeObserver.OnGlobalLayoutListener, Subscribblable {

    private Paint paint;

    private TimeZone timezone;

    private Disposable textColorPrimarySubscription;

    public DigitalClockView(Context context) {
        this(context, null, 0);
    }

    public DigitalClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DigitalClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        timezone = TimeZone.getDefault();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        subscribe();

        new UpdateThread(this).start();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void setTimezone(String timezone) {
        this.timezone = TimeZone.getTimeZone(timezone);
    }

    @Override
    public void subscribe() {
        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        paint.setColor(integer);
                        invalidate();
                    }
                });
    }

    @Override
    public void unsubscribe() {
        textColorPrimarySubscription.dispose();
    }

    @Override
    public void onGlobalLayout() {
        paint.setTextSize(48f);
        Rect bounds = new Rect();
        paint.getTextBounds("00:00:00", 0, 8, bounds);
        paint.setTextSize((48f * getMeasuredWidth()) / (bounds.width() * 2));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TimeZone defaultZone = TimeZone.getDefault();
        TimeZone.setDefault(timezone);
        canvas.drawText(FormatUtils.format(getContext(), Calendar.getInstance().getTime()), canvas.getWidth() / 2, (canvas.getHeight() - paint.ascent()) / 2, paint);
        TimeZone.setDefault(defaultZone);
    }

    private static class UpdateThread extends Thread {

        private WeakReference<DigitalClockView> viewReference;

        private UpdateThread(DigitalClockView view) {
            viewReference = new WeakReference<>(view);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        DigitalClockView view = viewReference.get();
                        if (view != null)
                            view.invalidate();
                    }
                });
            }
        }
    }
}
