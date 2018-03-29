package james.alarmio.utils;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ScrollableBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    public ScrollableBottomSheetBehavior() {
        super();
    }

    public ScrollableBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        super.onInterceptTouchEvent(parent, child, event);
        return false;
    }
}
