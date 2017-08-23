package james.alarmio.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.afollestad.aesthetic.Aesthetic;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import james.alarmio.utils.ConversionUtils;

public class DaySwitch extends BaseSubscriptionView implements View.OnClickListener {

    private Paint accentPaint;
    private Paint textPaint;

    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;
    private Disposable textColorPrimaryInverseSubscription;

    private float checked;
    private boolean isChecked;
    private int textColorPrimary;
    private int textColorPrimaryInverse;
    private String text;
    private OnCheckedChangeListener listener;

    public DaySwitch(Context context) {
        this(context, null);
    }

    public DaySwitch(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DaySwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);

        accentPaint = new Paint();
        accentPaint.setColor(Color.BLACK);
        accentPaint.setStyle(Paint.Style.FILL);
        accentPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(ConversionUtils.dpToPx(18));
        textPaint.setTextAlign(Paint.Align.CENTER);

        subscribe();
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public void setChecked(boolean isChecked) {
        if (isChecked != this.isChecked) {
            this.isChecked = isChecked;
            textPaint.setColor(isChecked ? textColorPrimaryInverse : textColorPrimary);

            ValueAnimator animator = ValueAnimator.ofFloat(isChecked ? 0 : 1, isChecked ? 1 : 0);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    checked = (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            animator.start();
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void subscribe() {
        colorAccentSubscription = Aesthetic.get()
                .colorAccent()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        accentPaint.setColor(integer);
                        invalidate();
                    }
                });

        textColorPrimarySubscription = Aesthetic.get()
                .textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textColorPrimary = integer;
                        if (!isChecked) {
                            textPaint.setColor(integer);
                            invalidate();
                        }
                    }
                });

        textColorPrimaryInverseSubscription = Aesthetic.get()
                .textColorPrimaryInverse()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        textColorPrimaryInverse = integer;
                        if (isChecked) {
                            textPaint.setColor(integer);
                            invalidate();
                        }
                    }
                });
    }

    @Override
    public void unsubscribe() {
        colorAccentSubscription.dispose();
        textColorPrimarySubscription.dispose();
        textColorPrimaryInverseSubscription.dispose();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = ConversionUtils.dpToPx(18);
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, checked * size, accentPaint);
        if (text != null)
            canvas.drawText(text, canvas.getWidth() / 2, ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)), textPaint);
    }

    @Override
    public void onClick(View view) {
        setChecked(!isChecked);
        if (listener != null)
            listener.onCheckedChanged(this, isChecked);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(DaySwitch daySwitch, boolean b);
    }
}
