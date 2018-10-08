package james.alarmio.utils.anim;

/**
 * The AnimatedFloat class animates a float, to a granularity of
 * 0.1f. That is, if the difference between the target and current
 * value is less than 0.1, it will be ignored and the animation will
 * be regarded as complete.
 */
public class AnimatedFloat extends AnimatedValue<Float> {

    public AnimatedFloat(float value) {
        super(value);
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
    @Override
    public Float nextVal(long start, long duration) {
        float difference = (getTarget() - val()) * (float) Math.sqrt((double) (System.currentTimeMillis() - start) / (duration));
        if (Math.abs(getTarget() - val()) > .1f && System.currentTimeMillis() - start < duration)
            return val() + (getTarget() < val() ? Math.min(difference, -.1f) : Math.max(difference, .1f));
        else return getTarget();
    }

}