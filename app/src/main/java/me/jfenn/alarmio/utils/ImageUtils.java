package me.jfenn.alarmio.utils;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import me.jfenn.alarmio.data.PreferenceData;

public class ImageUtils {

    /**
     * Gets the current user-defined background image from SharedPreferences and applies
     * it to the passed view.
     *
     * @param imageView         The ImageView to apply the background image to.
     */
    public static void getBackgroundImage(ImageView imageView) {
        String backgroundUrl = PreferenceData.BACKGROUND_IMAGE.getValue(imageView.getContext());

        if (backgroundUrl != null && backgroundUrl.length() > 0) {
            if (backgroundUrl.startsWith("http"))
                Glide.with(imageView.getContext()).load(backgroundUrl).into(imageView);
            else if (backgroundUrl.contains("://")) {
                if (backgroundUrl.startsWith("content://")) {
                    String path = Uri.parse(backgroundUrl).getLastPathSegment();
                    if (path != null && path.contains(":"))
                        path = "/storage/" + path.replaceFirst(":", "/");
                    else path = Uri.parse(backgroundUrl).getPath();

                    //      "a haiku"
                    //I don't like storage
                    //I'm sorry, poor developer
                    //this is all my fault
                    //          - james fenn, 2018

                    Glide.with(imageView.getContext()).load(new File(path)).into(imageView);
                } else Glide.with(imageView.getContext()).load(Uri.parse(backgroundUrl)).into(imageView);
            } else Glide.with(imageView.getContext()).load(new File(backgroundUrl)).into(imageView);
        }
    }

}
