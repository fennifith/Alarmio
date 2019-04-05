package me.jfenn.alarmio.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.aesthetic.Aesthetic;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import me.jfenn.alarmio.interfaces.Subscribblable;

public class ProgressLineView extends View implements Subscribblable {

    private Paint backgroundPaint;
    private Paint linePaint;

    private float progress;
    private float drawnProgress;

    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;

    public ProgressLineView(Context context) {
        super(context);
        init();
    }

    public ProgressLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.LTGRAY);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.DKGRAY);
    }

    @Override
    public void subscribe() {
        colorAccentSubscription = Aesthetic.Companion.get()
                .colorAccent()
                .subscribe(integer -> {
                    linePaint.setColor(integer);
                    linePaint.setAlpha(100);
                    postInvalidate();
                });

        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> {
                    backgroundPaint.setColor(integer);
                    backgroundPaint.setAlpha(30);
                    postInvalidate();
                });
    }

    @Override
    public void unsubscribe() {
        colorAccentSubscription.dispose();
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

    public void update(float progress) {
        this.progress = progress;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawnProgress != progress)
            drawnProgress = ((drawnProgress * 4) + progress) / 5;

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        canvas.drawRect(0, 0, canvas.getWidth() * drawnProgress, canvas.getHeight(), linePaint);

        if ((drawnProgress - progress) * canvas.getWidth() != 0)
            postInvalidate();
    }
}
