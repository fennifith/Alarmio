package james.alarmio.utils.anim;

import androidx.annotation.Nullable;

public abstract class AnimatedValue<T> {

    public final static long DEFAULT_ANIMATION_DURATION = 400;

    private T targetValue;
    private T drawnValue;

    @Nullable
    private T defaultValue;

    private long start;

    public AnimatedValue(T value) {
        targetValue = drawnValue = value;
    }

    public void set(T value) {
        drawnValue = value;
    }

    public void setDefault(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setCurrent(T value) {
        drawnValue = targetValue = value;
    }

    public T val() {
        return drawnValue;
    }

    public T nextVal() {
        return nextVal(DEFAULT_ANIMATION_DURATION);
    }

    public T nextVal(long duration) {
        return nextVal(start, duration);
    }

    abstract T nextVal(long start, long duration);

    public T getTarget() {
        return targetValue;
    }

    public T getDefault() {
        return defaultValue != null ? defaultValue : targetValue;
    }

    public boolean isTarget() {
        return drawnValue == targetValue;
    }

    public boolean isDefault() {
        return defaultValue != null && drawnValue == defaultValue;
    }

    public boolean isTargetDefault() {
        return defaultValue != null && targetValue == defaultValue;
    }

    public void toDefault() {
        if (defaultValue != null)
            to(defaultValue);
    }

    public void to(T value) {
        targetValue = value;
        start = System.currentTimeMillis();
    }

    public void next(boolean animate) {
        next(animate, DEFAULT_ANIMATION_DURATION);
    }

    public void next(boolean animate, long duration) {
        drawnValue = animate ? nextVal(duration) : targetValue;
    }

}