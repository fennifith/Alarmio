package me.jfenn.alarmio.data.preference

import android.content.Intent

import me.jfenn.alarmio.activities.FileChooserActivity
import me.jfenn.alarmio.data.PreferenceData

/**
 * A preference item that allows the user to select
 * an image from a file (the resulting preference
 * contains a valid image path / URI).
 */
class ImageFilePreferenceData(private val preference: PreferenceData, name: Int) : CustomPreferenceData(name) {

    override fun getValueName(holder: CustomPreferenceData.ViewHolder): String = ""

    override fun onClick(holder: CustomPreferenceData.ViewHolder) {
        holder.context.startActivity(Intent(holder.context, FileChooserActivity::class.java).apply {
            putExtra(FileChooserActivity.EXTRA_PREFERENCE, preference)
            putExtra(FileChooserActivity.EXTRA_TYPE, "image/*")
        })
    }
}
