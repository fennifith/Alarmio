package me.jfenn.alarmio.data.preference

import me.jfenn.alarmio.BuildConfig
import me.jfenn.alarmio.R
import me.jfenn.attribouter.Attribouter

class AboutPreferenceData : CustomPreferenceData(R.string.title_about) {

    override fun getValueName(holder: CustomPreferenceData.ViewHolder): String? {
        return null
    }

    override fun onClick(holder: CustomPreferenceData.ViewHolder) {
        val context = holder.context

        Attribouter.from(context)
                .withGitHubToken(BuildConfig.GITHUB_TOKEN)
                .show()
    }
}
