package james.alarmio.utils.anim;

public class AnimatedInteger extends AnimatedValue<Integer> {

    public AnimatedInteger(int value) {
        super(value);
    }

    @Override
    public Integer nextVal(long start, long duration) {
        int difference = (int) ((getTarget() - val()) * Math.sqrt((double) (System.currentTimeMillis() - start) / (duration)));
        if (Math.abs(getTarget() - val()) > 1 && System.currentTimeMillis() - start < duration)
            return val() + (getTarget() < val() ? Math.min(difference, -1) : Math.max(difference, 1));
        else return getTarget();
    }

}