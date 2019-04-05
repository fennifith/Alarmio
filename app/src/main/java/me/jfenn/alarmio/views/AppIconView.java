package me.jfenn.alarmio.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import me.jfenn.alarmio.R;

public class AppIconView extends View {

    private Bitmap bgBitmap;
    private Bitmap fgSecondsBitmap;
    private Bitmap fgMinutesBitmap;
    private Bitmap fgHoursBitmap;
    private Bitmap fgBitmap;
    private Paint paint;
    private int size;
    private float bgRotation = 1;
    private float rotation = 1;
    private float bgScale;
    private float fgScale;

    private Path path;
    private ValueAnimator animator;

    public AppIconView(Context context) {
        this(context, null);
    }

    public AppIconView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#212121"));
        paint.setDither(true);

        animator = ValueAnimator.ofFloat(bgScale, 0.8f);
        animator.setInterpolator(new OvershootInterpolator());
        animator.setDuration(750);
        animator.setStartDelay(250);
        animator.addUpdateListener(animator -> {
            bgScale = (float) animator.getAnimatedValue();
            invalidate();
        });
        animator.start();

        animator = ValueAnimator.ofFloat(rotation, 0);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1000);
        animator.setStartDelay(250);
        animator.addUpdateListener(animator -> {
            fgScale = animator.getAnimatedFraction() * 0.8f;
            rotation = (float) animator.getAnimatedValue();
            invalidate();
        });
        animator.start();

        animator = ValueAnimator.ofFloat(bgRotation, 0);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1250);
        animator.setStartDelay(250);
        animator.addUpdateListener(animator -> {
            bgRotation = (float) animator.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private Bitmap getBitmap(int size, @DrawableRes int resource) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), resource, options), size, size);
    }

    private Matrix getFgMatrix(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
        matrix.postScale(fgScale, fgScale);
        return matrix;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int size = Math.min(canvas.getWidth(), canvas.getHeight());
        if (this.size != size || fgBitmap == null || path == null) {
            this.size = size;
            bgBitmap = getBitmap(size, R.mipmap.ic_launcher_bg);
            fgSecondsBitmap = getBitmap(size, R.mipmap.ic_launcher_fg_seconds);
            fgMinutesBitmap = getBitmap(size, R.mipmap.ic_launcher_fg_minutes);
            fgHoursBitmap = getBitmap(size, R.mipmap.ic_launcher_fg_hours);
            fgBitmap = getBitmap(size, R.mipmap.ic_launcher_fg);
            path = new Path();
            path.arcTo(new RectF(0, 0, size, size), 0, 359);
            path.close();
        }

        Matrix matrix = new Matrix();
        matrix.postScale(bgScale * 0.942986f, bgScale * 0.942986f, size / 2, size / 2);

        Path path = new Path();
        this.path.transform(matrix, path);
        canvas.drawPath(path, paint);

        matrix = getFgMatrix(bgBitmap);
        matrix.postRotate(bgRotation * 120);
        matrix.postTranslate(bgBitmap.getWidth() / 2, bgBitmap.getHeight() / 2);
        canvas.drawBitmap(bgBitmap, matrix, paint);

        matrix = getFgMatrix(fgSecondsBitmap);
        matrix.postRotate(-rotation * 720);
        matrix.postTranslate(fgSecondsBitmap.getWidth() / 2, fgSecondsBitmap.getHeight() / 2);
        canvas.drawBitmap(fgSecondsBitmap, matrix, paint);

        matrix = getFgMatrix(fgMinutesBitmap);
        matrix.postRotate(-rotation * 360);
        matrix.postTranslate(fgMinutesBitmap.getWidth() / 2, fgMinutesBitmap.getHeight() / 2);
        canvas.drawBitmap(fgMinutesBitmap, matrix, paint);

        matrix = getFgMatrix(fgHoursBitmap);
        matrix.postRotate(-rotation * 60);
        matrix.postTranslate(fgHoursBitmap.getWidth() / 2, fgHoursBitmap.getHeight() / 2);
        canvas.drawBitmap(fgHoursBitmap, matrix, paint);

        matrix = getFgMatrix(fgBitmap);
        matrix.postTranslate(fgBitmap.getWidth() / 2, fgBitmap.getHeight() / 2);
        canvas.drawBitmap(fgBitmap, matrix, paint);
    }

    public void addListener(Animator.AnimatorListener listener) {
        if (animator != null)
            animator.addListener(listener);
    }

    public void removeListener(Animator.AnimatorListener listener) {
        if (animator != null)
            animator.removeListener(listener);
    }
}
