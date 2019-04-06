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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import me.jfenn.alarmio.interfaces.Subscribblable;
import me.jfenn.alarmio.utils.FormatUtils;

public class DigitalClockView extends View implements ViewTreeObserver.OnGlobalLayoutListener, Subscribblable {

    private Paint paint;
    private UpdateThread thread;

    private TimeZone timezone;

    private Disposable textColorPrimarySubscription;

    public DigitalClockView(Context context) {
        super(context);
        init();
    }

    public DigitalClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DigitalClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        timezone = TimeZone.getDefault();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public void setTimezone(@NonNull String timezone) {
        this.timezone = TimeZone.getTimeZone(timezone);
    }

    @Override
    public void subscribe() {
        getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (thread == null) thread = new UpdateThread(this);
        thread.start();

        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> {
                    paint.setColor(integer);
                    invalidate();
                });
    }

    @Override
    public void unsubscribe() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        thread.interrupt();
        thread = null;

        textColorPrimarySubscription.dispose();
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

                new Handler(Looper.getMainLooper()).post(() -> {
                    DigitalClockView view = viewReference.get();
                    if (view != null)
                        view.invalidate();
                });
            }
        }
    }
}
