package me.jfenn.alarmio.data.preference

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

import androidx.core.content.ContextCompat
import me.jfenn.alarmio.R

/**
 * A preference item allowing the user to select to
 * ignore battery optimizations to improve stability.
 */
@TargetApi(23)
class BatteryOptimizationPreferenceData : CustomPreferenceData(R.string.title_ignore_battery_optimizations) {

    override fun getValueName(holder: CustomPreferenceData.ViewHolder): String? = null

    override fun onClick(holder: CustomPreferenceData.ViewHolder) {
        val context = holder.context

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + context.applicationContext.packageName)
            checkIntentAndStart(context, intent)
        } else {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            if (checkIntentAndStart(context, intent))
                Toast.makeText(context, R.string.msg_battery_optimizations_switch_enable, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIntentAndStart(context: Context, intent: Intent): Boolean {
        intent.resolveActivity(context.packageManager)?.let {
            context.startActivity(intent)
            return true
        }

        return false
    }

}
