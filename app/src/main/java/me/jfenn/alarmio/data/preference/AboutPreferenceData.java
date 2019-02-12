package me.jfenn.alarmio.data.preference;

import android.content.Context;

import me.jfenn.alarmio.R;
import me.jfenn.attribouter.Attribouter;

public class AboutPreferenceData extends CustomPreferenceData {

    public AboutPreferenceData() {
        super(R.string.title_about);
    }

    @Override
    public String getValueName(ViewHolder holder) {
        return null;
    }

    @Override
    public void onClick(ViewHolder holder) {
        Context context = holder.getContext();

        Attribouter attribouter = Attribouter.from(context);
        int githubAuthKey = context.getResources().getIdentifier("githubAuthKey", "string", context.getPackageName());
        if (githubAuthKey != 0)
            attribouter = attribouter.withGitHubToken(context.getString(githubAuthKey));

        attribouter.show();
    }
}
