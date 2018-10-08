package james.alarmio.utils.anim;

/**
 * The AnimatedInteger class animates an integer, with a granularity
 * of 1.00(...)1. That is, if the difference between the current and
 * target value is less than or equal to 1, it will be ignored and the
 * animation will be regarded as complete.
 */
public class AnimatedInteger extends AnimatedValue<Integer> {

    public AnimatedInteger(int value) {
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
    public Integer nextVal(long start, long duration) {
        int difference = (int) ((getTarget() - val()) * Math.sqrt((double) (System.currentTimeMillis() - start) / (duration)));
        if (Math.abs(getTarget() - val()) > 1 && System.currentTimeMillis() - start < duration)
            return val() + (getTarget() < val() ? Math.min(difference, -1) : Math.max(difference, 1));
        else return getTarget();
    }

}