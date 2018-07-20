package james.alarmio.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.multimoon.colorful.ColorfulKt;
import james.alarmio.Alarmio;
import james.alarmio.interfaces.Subscribblable;

public class ProgressLineView extends View implements Subscribblable {

    private Paint backgroundPaint;
    private Paint linePaint;

    private float progress;
    private float drawnProgress;

    public ProgressLineView(Context context) {
        this(context, null);
    }

    public ProgressLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.LTGRAY);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.DKGRAY);

        subscribe();
    }

    @Override
    public void subscribe() {
        linePaint.setColor(ColorfulKt.Colorful().getAccentColor().getColorPack().normal().asInt());
        linePaint.setAlpha(100);

        backgroundPaint.setColor(((Alarmio) getContext().getApplicationContext()).getTextColor());
        backgroundPaint.setAlpha(30);
        postInvalidate();
    }

    @Override
    public void unsubscribe() {
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
