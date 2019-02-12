package me.jfenn.alarmio.utils;

import android.content.Context;

import james.crasher.Crasher;

public class CrashUtils {

    public static void setup(Context context) {
        Crasher crasher = new Crasher(context);
        crasher.setColor(0xff212121);
        crasher.setEmail("dev@jfenn.me");
    }

}
