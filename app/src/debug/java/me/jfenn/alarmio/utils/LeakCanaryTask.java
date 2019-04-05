package me.jfenn.alarmio.utils;

import android.util.Log;

import me.jfenn.alarmio.Alarmio;

public final class LeakCanaryTask implements DebugUtils.SetupTask {

    @Override
    public void setup(Alarmio alarmio) {
        Log.d("LeakCanary", "started leakcanary");

        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectNetwork()
                .penaltyLog()
                .penaltyDeath()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());*/

        LeakLoggerService.setupLeakCanary(alarmio);
    }
}
