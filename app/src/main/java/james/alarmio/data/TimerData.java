package james.alarmio.data;

public class TimerData {

    private int id;
    private long duration = 600000;
    private long endTime;

    public TimerData(int id) {
        this.id = id;
    }

}
