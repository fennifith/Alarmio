package me.jfenn.alarmio.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FragmentLayout extends FrameLayout {

    private Rect tempRect, windowRect;

    public FragmentLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public FragmentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FragmentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        tempRect = new Rect();
        windowRect = new Rect();
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        windowRect.set(insets);
        super.fitSystemWindows(insets);
        return false;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        tempRect.set(windowRect);
        super.fitSystemWindows(tempRect);
    }

}
