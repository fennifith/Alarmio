package me.jfenn.alarmio.data.preference;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import me.jfenn.alarmio.R;

@TargetApi(23)
public class BatteryOptimizationPreferenceData extends CustomPreferenceData {

    public BatteryOptimizationPreferenceData() {
        super(R.string.title_ignore_battery_optimizations);
    }

    @Override
    public String getValueName(ViewHolder holder) {
        return null;
    }

    @Override
    public void onClick(ViewHolder holder) {
        Context context = holder.getContext();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
            checkIntentAndStart(context, intent);
        } else {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if (checkIntentAndStart(context, intent))
                Toast.makeText(context, R.string.msg_battery_optimizations_switch_enable, Toast.LENGTH_LONG).show();
        }
    }

    private static boolean checkIntentAndStart(Context context, Intent intent) {
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        } else return false;
    }

}
