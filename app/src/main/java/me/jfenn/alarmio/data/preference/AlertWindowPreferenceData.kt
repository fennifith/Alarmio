package me.jfenn.alarmio.data.preference

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.CompoundButtonCompat
import com.afollestad.aesthetic.Aesthetic

import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.dialogs.AlertDialog

/**
 * A preference item allowing the user to grant the
 * overlay permissions / alert window to ignore background
 * restrictions when starting an activity.
 *
 * @see https://developer.android.com/guide/components/activities/background-starts
 */
@TargetApi(23)
class AlertWindowPreferenceData : BasePreferenceData<AlertWindowPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_boolean, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.title.setText(R.string.info_background_permissions_title)
        holder.description.visibility = View.GONE
        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isClickable = false

        holder.toggle.isChecked = Settings.canDrawOverlays(holder.context)
        holder.itemView.setOnClickListener {
            if (!Settings.canDrawOverlays(holder.context)) {
                showAlert(holder)
            } else showActivity(holder.context)
        }

        Aesthetic.get()
                .colorAccent()
                .take(1)
                .subscribe { colorAccent ->
                    Aesthetic.get()
                            .textColorPrimary()
                            .take(1)
                            .subscribe { textColorPrimary ->
                                CompoundButtonCompat.setButtonTintList(holder.toggle, ColorStateList(
                                        arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                                        intArrayOf(Color.argb(100, Color.red(textColorPrimary), Color.green(textColorPrimary), Color.blue(textColorPrimary)), colorAccent)
                                ))

                                holder.toggle.setTextColor(textColorPrimary)
                            }
                }
    }

    private fun showAlert(holder: ViewHolder) {
        AlertDialog(holder.context)
                .setTitle(holder.context.getString(R.string.info_background_permissions_title))
                .setContent(holder.context.getString(R.string.info_background_permissions_body))
                .setListener { _, ok ->
                    if (ok)
                        showActivity(holder.context)
                }
                .show()
    }

    private fun showActivity(context: Context) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.applicationContext.packageName}")))
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val description: TextView = v.findViewById(R.id.description)
        val toggle: SwitchCompat = v.findViewById(R.id.toggle)
    }

}
