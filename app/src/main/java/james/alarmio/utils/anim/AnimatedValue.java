package james.alarmio.utils.anim;

import androidx.annotation.Nullable;

/**
 * AnimatedValue is a class which stores a value to be animated
 * over a given duration of time. It provides methods to set a
 * target value, default value, and current value.
 *
 * As this method is meant to be used alongside a view's Canvas,
 * the animation should not occur in "steps", but rather as a
 * function of System.currentTimeMillis.
 *
 * @param <T>       The type of value to be animated.
 */
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

    /**
     * Set the current value to be drawn.
     *
     * @param value         The current value.
     */
    public void set(T value) {
        drawnValue = value;
    }

    /**
     * Set the default value to return to.
     *
     * @param defaultValue  The default value.
     */
    public void setDefault(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Set the current (and target) value.
     *
     * @param value         The current value.
     */
    public void setCurrent(T value) {
        drawnValue = targetValue = value;
    }

    /**
     * Get the current value to be drawn.
     *
     * @return              The current value.
     */
    public T val() {
        return drawnValue;
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @return              The next value.
     */
    public T nextVal() {
        return nextVal(DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @param duration      The duration, in milliseconds, that
     *                      the animation should take.
     * @return              The next value.
     */
    public T nextVal(long duration) {
        return nextVal(start, duration);
    }

    /**
     * Get the next value about to be drawn, without setting
     * the current value to it.
     *
     * @param start         The time at which the animation started,
     *                      in milliseconds.
     * @param duration      The duration, in milliseconds, that
     *                      the animation should take.
     * @return              The next value.
     */
    abstract T nextVal(long start, long duration);

    /**
     * Get the target value that is currently being animated to.
     *
     * @return              The target value.
     */
    public T getTarget() {
        return targetValue;
    }

    /**
     * Get the default value that the animation should return to.
     *
     * @return              The default value.
     */
    public T getDefault() {
        return defaultValue != null ? defaultValue : targetValue;
    }

    /**
     * Determine if the target value has been drawn (implying that
     * the animation is complete).
     *
     * @return              True if the target value has supposedly
     *                      been drawn.
     */
    public boolean isTarget() {
        return drawnValue == targetValue;
    }

    /**
     * Determine if the default value has been drawn.
     *
     * @return              True if the default value has supposedly
     *                      been drawn.
     */
    public boolean isDefault() {
        return defaultValue != null && drawnValue == defaultValue;
    }

    /**
     * Determine if the default value has been set AND is the current
     * target.
     *
     * @return              True if the default value is the current
     *                      target.
     */
    public boolean isTargetDefault() {
        return defaultValue != null && targetValue == defaultValue;
    }

    /**
     * Animate to the default value.
     */
    public void toDefault() {
        if (defaultValue != null)
            to(defaultValue);
    }

    /**
     * Set the value to be animated towards.
     *
     * @param value         The target value.
     */
    public void to(T value) {
        targetValue = value;
        start = System.currentTimeMillis();
    }

    /**
     * Update the current value.
     *
     * @param animate       Whether to animate the change.
     */
    public void next(boolean animate) {
        next(animate, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Update the current value.
     *
     * @param animate       Whether to animate the change.
     * @param duration      The duration, in milliseconds, to animate
     *                      across.
     */
    public void next(boolean animate, long duration) {
        drawnValue = animate ? nextVal(duration) : targetValue;
    }

}