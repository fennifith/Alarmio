/*
 * Copyright 2015 Alexandre Piveteau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.jfenn.alarmio.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.aesthetic.Aesthetic;

import androidx.viewpager.widget.ViewPager;
import io.reactivex.disposables.Disposable;
import me.jfenn.alarmio.interfaces.Subscribblable;
import me.jfenn.androidutils.DimenUtils;

public class PageIndicatorView extends View implements ViewPager.OnPageChangeListener, Subscribblable {

    private int actualPosition;
    private float offset;
    private int size;
    private ViewPager viewPager;

    private IndicatorEngine engine;

    private int textColorPrimary;
    private int textColorSecondary;

    private Disposable textColorPrimarySubscription;
    private Disposable textColorSecondarySubscription;

    public PageIndicatorView(Context context) {
        super(context);
        init();
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        engine = new IndicatorEngine();

        engine.onInitEngine(this);
        size = 2;
    }

    @Override
    public void subscribe() {
        textColorPrimarySubscription = Aesthetic.Companion.get()
                .textColorPrimary()
                .subscribe(integer -> {
                    textColorPrimary = integer;
                    engine.updateTextColors(PageIndicatorView.this);
                    invalidate();
                });

        textColorSecondarySubscription = Aesthetic.Companion.get()
                .textColorSecondary()
                .subscribe(integer -> {
                    textColorSecondary = integer;
                    engine.updateTextColors(PageIndicatorView.this);
                    invalidate();
                });
    }

    @Override
    public void unsubscribe() {
        if (textColorPrimarySubscription != null) {
            textColorPrimarySubscription.dispose();
            textColorSecondarySubscription = null;
        }

        if (textColorSecondarySubscription != null) {
            textColorSecondarySubscription.dispose();
            textColorSecondarySubscription = null;
        }
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

    public int getTotalPages() {
        return size;
    }

    public int getActualPosition() {
        return actualPosition;
    }

    public float getPositionOffset() {
        return offset;
    }

    public void notifyNumberPagesChanged() {
        size = viewPager.getAdapter().getCount();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        engine.onDrawIndicator(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(engine.getMeasuredWidth(), engine.getMeasuredHeight());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        actualPosition = position;
        offset = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * You must call this AFTER setting the Adapter for the ViewPager, or it won't display the right amount of points.
     *
     * @param viewPager
     */
    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
        size = viewPager.getAdapter().getCount();
        invalidate();
    }

    private static class IndicatorEngine {

        private PageIndicatorView indicator;

        private Paint selectedPaint;
        private Paint unselectedPaint;

        public int getMeasuredHeight() {
            return DimenUtils.dpToPx(8);
        }

        public int getMeasuredWidth() {
            return DimenUtils.dpToPx(8 * (indicator.getTotalPages() * 2 - 1));
        }

        public void onInitEngine(PageIndicatorView indicator) {
            this.indicator = indicator;

            selectedPaint = new Paint();
            unselectedPaint = new Paint();

            selectedPaint.setColor(indicator.textColorPrimary);
            unselectedPaint.setColor(indicator.textColorSecondary);
            selectedPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            unselectedPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        public void updateTextColors(PageIndicatorView indicator) {
            selectedPaint.setColor(indicator.textColorPrimary);
            unselectedPaint.setColor(indicator.textColorSecondary);
        }

        public void onDrawIndicator(Canvas canvas) {
            int height = indicator.getHeight();

            for (int i = 0; i < indicator.getTotalPages(); i++) {
                int x = DimenUtils.dpToPx(4) + DimenUtils.dpToPx(16 * i);
                canvas.drawCircle(x, height / 2f, DimenUtils.dpToPx(4), unselectedPaint);
            }

            int firstX;
            int secondX;

            firstX = DimenUtils.dpToPx(4 + indicator.getActualPosition() * 16);

            if (indicator.getPositionOffset() > .5f) {
                firstX += DimenUtils.dpToPx(16 * (indicator.getPositionOffset() - .5f) * 2);
            }

            secondX = DimenUtils.dpToPx(4 + indicator.getActualPosition() * 16);

            if (indicator.getPositionOffset() < .5f) {
                secondX += DimenUtils.dpToPx(16 * indicator.getPositionOffset() * 2);
            } else {
                secondX += DimenUtils.dpToPx(16);
            }

            canvas.drawCircle(firstX, DimenUtils.dpToPx(4), DimenUtils.dpToPx(4), selectedPaint);
            canvas.drawCircle(secondX, DimenUtils.dpToPx(4), DimenUtils.dpToPx(4), selectedPaint);
            canvas.drawRect(firstX, 0, secondX, DimenUtils.dpToPx(8), selectedPaint);
        }
    }
}