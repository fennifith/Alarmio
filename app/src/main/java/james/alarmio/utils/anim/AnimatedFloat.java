package james.alarmio.utils.anim;

public class AnimatedFloat extends AnimatedValue<Float> {

    public AnimatedFloat(float value) {
        super(value);
    }

    @Override
    public Float nextVal(long start, long duration) {
        float difference = (getTarget() - val()) * (float) Math.sqrt((double) (System.currentTimeMillis() - start) / (duration));
        if (Math.abs(getTarget() - val()) > .1f && System.currentTimeMillis() - start < duration)
            return val() + (getTarget() < val() ? Math.min(difference, -.1f) : Math.max(difference, .1f));
        else return getTarget();
    }

}