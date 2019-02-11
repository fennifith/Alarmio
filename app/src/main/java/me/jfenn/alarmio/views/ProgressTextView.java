package me.jfenn.alarmio.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.afollestad.aesthetic.Aesthetic;

import androidx.annotation.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.interfaces.Subscribblable;
import me.jfenn.androidutils.DimenUtils;

public class ProgressTextView extends View implements Subscribblable {

    private Paint linePaint, circlePaint, referenceCirclePaint, backgroundPaint, textPaint;
    private long progress, maxProgress, referenceProgress;

    private String text;

    private int padding;

    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;
    private Disposable textColorSecondarySubscription;

    public ProgressTextView(Context context) {
        this(context, null);
    }

    public ProgressTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        padding = DimenUtils.dpToPx(4);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(padding);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);

        referenceCirclePaint = new Paint();
        referenceCirclePaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(padding);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(DimenUtils.spToPx(34));
        textPaint.setFakeBoldText(true);

        subscribe();
    }

    @Override
    public void subscribe() {
        colorAccentSubscription = Aesthetic.Companion.get()
                .colorAccent()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        linePaint.setColor(integer);
                        circlePaint.setColor(integer);
                        invalidate();
                    }
                });

        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textPaint.setColor(integer);
                        referenceCirclePaint.setColor(integer);
                        invalidate();
                    }
                });

        textColorSecondarySubscription = Aesthetic.Companion.get()
                .textColorSecondary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        backgroundPaint.setColor(integer);
                        backgroundPaint.setAlpha(50);
                        invalidate();
                    }
                });
    }

    @Override
    public void unsubscribe() {
        colorAccentSubscription.dispose();
        textColorPrimarySubscription.dispose();
        textColorSecondarySubscription.dispose();
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public void setProgress(long progress) {
        setProgress(progress, false);
    }

    public void setProgress(long progress, boolean animate) {
        if (animate) {
            ValueAnimator animator = ValueAnimator.ofFloat(this.progress, progress);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ProgressTextView.this.progress = (long) (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            animator.start();
        } else {
            this.progress = progress;
            invalidate();
        }
    }

    public void setMaxProgress(long maxProgress) {
        setMaxProgress(maxProgress, false);
    }

    public void setMaxProgress(long maxProgress, boolean animate) {
        if (animate) {
            ValueAnimator animator = ValueAnimator.ofFloat(this.maxProgress, maxProgress);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ProgressTextView.this.maxProgress = (long) (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            animator.start();
        } else {
            this.maxProgress = maxProgress;
            invalidate();
        }
    }

    public void setReferenceProgress(long referenceProgress) {
        this.referenceProgress = referenceProgress;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredWidth();
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int size = Math.min(canvas.getWidth(), canvas.getHeight());
        int sidePadding = padding * 3;
        canvas.drawCircle(size / 2, size / 2, (size / 2) - sidePadding, progress > maxProgress && maxProgress > 0 ? linePaint : backgroundPaint);

        if (maxProgress > 0) {
            float angle = (360f * progress / maxProgress);
            float referenceAngle = (360f * referenceProgress / maxProgress);

            Path path = new Path();
            path.arcTo(new RectF(sidePadding, sidePadding, size - sidePadding, size - sidePadding), -90, angle, true);
            canvas.drawPath(path, linePaint);

            canvas.drawCircle((size / 2) + ((float) Math.cos((angle - 90) * Math.PI / 180) * ((size / 2) - sidePadding)), (size / 2) + ((float) Math.sin((angle - 90) * Math.PI / 180) * ((size / 2) - sidePadding)), 2 * padding, circlePaint);
            if (referenceProgress != 0)
                canvas.drawCircle((size / 2) + ((float) Math.cos((referenceAngle - 90) * Math.PI / 180) * ((size / 2) - sidePadding)), (size / 2) + ((float) Math.sin((referenceAngle - 90) * Math.PI / 180) * ((size / 2) - sidePadding)), 2 * padding, referenceCirclePaint);
        }

        if (text != null)
            canvas.drawText(text, size / 2, (size / 2) - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);
    }

}
