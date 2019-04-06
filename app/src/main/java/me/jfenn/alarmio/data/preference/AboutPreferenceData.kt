package me.jfenn.alarmio.data.preference

import me.jfenn.alarmio.BuildConfig
import me.jfenn.alarmio.R
import me.jfenn.attribouter.Attribouter

/**
 * A preference item that opens the application's about screen.
 */
class AboutPreferenceData : CustomPreferenceData(R.string.title_about) {

    override fun getValueName(holder: CustomPreferenceData.ViewHolder): String? = null

    override fun onClick(holder: CustomPreferenceData.ViewHolder) {
        Attribouter.from(holder.context)
                .withGitHubToken(BuildConfig.GITHUB_TOKEN)
                .show()
    }
}
