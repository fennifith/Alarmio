package james.alarmio.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.afollestad.aesthetic.Aesthetic;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.interfaces.Subscribblable;
import james.alarmio.utils.ConversionUtils;
import james.alarmio.utils.ImageUtils;
import james.alarmio.utils.anim.AnimatedFloat;

public class SlideActionView extends View implements Subscribblable, View.OnTouchListener {

    private float x = -1;
    private AnimatedFloat selected;
    private Map<Float, AnimatedFloat> ripples;

    private int handleRadius;
    private int expandedHandleRadius;
    private int selectionRadius;
    private int rippleRadius;

    private Paint normalPaint;
    private Paint outlinePaint;
    private Paint bitmapPaint;
    private int backgroundColor;

    private Bitmap leftImage, rightImage;

    private SlideActionListener listener;

    private Disposable textColorPrimarySubscription;
    private Disposable textColorPrimaryInverseSubscription;

    public SlideActionView(Context context) {
        super(context);
        init();
    }

    public SlideActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SlideActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        handleRadius = ConversionUtils.dpToPx(12);
        expandedHandleRadius = ConversionUtils.dpToPx(32);
        selectionRadius = ConversionUtils.dpToPx(42);
        rippleRadius = ConversionUtils.dpToPx(140);

        selected = new AnimatedFloat(0);
        ripples = new HashMap<>();

        normalPaint = new Paint();
        normalPaint.setStyle(Paint.Style.FILL);
        normalPaint.setColor(Color.GRAY);
        normalPaint.setAntiAlias(true);
        normalPaint.setDither(true);

        outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(Color.GRAY);
        outlinePaint.setAntiAlias(true);
        outlinePaint.setDither(true);

        bitmapPaint = new Paint();
        bitmapPaint.setStyle(Paint.Style.FILL);
        bitmapPaint.setColor(Color.GRAY);
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setDither(true);
        bitmapPaint.setFilterBitmap(true);

        setOnTouchListener(this);
        setFocusable(true);
        setClickable(true);
        subscribe();
    }

    public void setListener(SlideActionListener listener) {
        this.listener = listener;
    }

    public void setLeftImage(Drawable drawable) {
        leftImage = ImageUtils.drawableToBitmap(drawable);
        postInvalidate();
    }

    public void setRightImage(Drawable drawable) {
        rightImage = ImageUtils.drawableToBitmap(drawable);
        postInvalidate();
    }

    @Override
    public void subscribe() {
        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        normalPaint.setColor(integer);
                        outlinePaint.setColor(integer);
                        bitmapPaint.setColor(integer);
                        bitmapPaint.setColorFilter(new PorterDuffColorFilter(integer, PorterDuff.Mode.SRC_IN));
                        postInvalidate();
                    }
                });

        textColorPrimaryInverseSubscription = Aesthetic.Companion.get()
                .textColorPrimaryInverse()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        backgroundColor = (100 << 24) | (integer & 0x00ffffff);
                        postInvalidate();
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

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        selected.next(true);
        if (x < 0)
            x = (float) getWidth() / 2;

        normalPaint.setAlpha(150 - (int) (selected.val() * 100));
        int radius = (int) ((handleRadius * (1 - selected.val())) + (expandedHandleRadius * selected.val()));
        float drawnX = (x * selected.val()) + (((float) getWidth() / 2) * (1 - selected.val()));

        canvas.drawColor(backgroundColor);
        canvas.drawCircle(drawnX, (float) getHeight() / 2, radius, normalPaint);

        if (leftImage != null && rightImage != null) {
            bitmapPaint.setAlpha((int) (255 * Math.min(1f, Math.max(0f, (getWidth() - drawnX - selectionRadius) / getWidth()))));
            canvas.drawBitmap(leftImage, selectionRadius - (leftImage.getWidth() / 2), (getHeight() - leftImage.getHeight()) / 2, bitmapPaint);
            bitmapPaint.setAlpha((int) (255 * Math.min(1f, Math.max(0f, (drawnX - selectionRadius) / getWidth()))));
            canvas.drawBitmap(rightImage, getWidth() - selectionRadius - (leftImage.getWidth() / 2), (getHeight() - leftImage.getHeight()) / 2, bitmapPaint);
        }

        if (Math.abs((getWidth() / 2) - drawnX) > selectionRadius / 2) {
            if (drawnX * 2 < getWidth()) {
                float progress = Math.min(1f, Math.max(0f, ((getWidth() - ((drawnX + selectionRadius) * 2)) / getWidth())));
                progress = (float) Math.pow(progress, 0.2f);

                outlinePaint.setAlpha((int) (255 * progress));
                canvas.drawCircle(selectionRadius, getHeight() / 2, (selectionRadius / 2) + (rippleRadius * (1 - progress)), outlinePaint);
            } else {
                float progress = Math.min(1f, Math.max(0f, (((drawnX - selectionRadius) * 2) - getWidth()) / getWidth()));
                progress = (float) Math.pow(progress, 0.2f);

                outlinePaint.setAlpha((int) (255 * progress));
                canvas.drawCircle(getWidth() - selectionRadius, getHeight() / 2, (selectionRadius / 2) + (rippleRadius * (1 - progress)), outlinePaint);
            }
        }

        for (float x : ripples.keySet()) {
            AnimatedFloat scale = ripples.get(x);
            scale.next(true, 1600);
            normalPaint.setAlpha((int) (150 * (scale.getTarget() - scale.val()) / scale.getTarget()));
            canvas.drawCircle(x, getHeight() / 2, scale.val(), normalPaint);
            if (scale.isTarget())
                ripples.remove(x);
        }

        if (!selected.isTarget() || ripples.size() > 0)
            postInvalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && Math.abs(event.getX() - (getWidth() / 2)) < selectionRadius)
            selected.to(1f);
        else if (event.getAction() == MotionEvent.ACTION_UP && selected.getTarget() > 0) {
            selected.to(0f);
            if (event.getX() > getWidth() - (selectionRadius * 2)) {
                AnimatedFloat ripple = new AnimatedFloat(selectionRadius);
                ripple.to((float) rippleRadius);
                ripples.put((float) getWidth() - selectionRadius, ripple);
                if (listener != null)
                    listener.onSlideRight();

                postInvalidate();
            } else if (event.getX() < selectionRadius * 2) {
                AnimatedFloat ripple = new AnimatedFloat(selectionRadius);
                ripple.to((float) rippleRadius);
                ripples.put((float) selectionRadius, ripple);
                if (listener != null)
                    listener.onSlideLeft();

                postInvalidate();
            }

            return true;
        }

        if (selected.getTarget() > 0) {
            x = event.getX();
            postInvalidate();
        }

        return false;
    }

    public interface SlideActionListener {
        void onSlideLeft();

        void onSlideRight();
    }
}
