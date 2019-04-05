package me.jfenn.alarmio.utils;

import james.crasher.Crasher;
import me.jfenn.alarmio.Alarmio;

public class CrasherTask implements DebugUtils.SetupTask {

    @Override
    public void setup(Alarmio alarmio) {
        Crasher crasher = new Crasher(alarmio);
        crasher.setColor(0xff212121);
        crasher.setEmail("dev@jfenn.me");
    }
}
