package james.alarmio.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import androidx.annotation.Nullable;
import james.alarmio.data.PreferenceData;

public class ImageUtils {

    /**
     * Converts drawables to bitmaps.
     *
     * @param drawable          A drawable.
     * @return                  A bitmap.
     */
    public static Bitmap drawableToBitmap(@Nullable Drawable drawable) {
        if (drawable == null)
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);

        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null)
                return bitmapDrawable.getBitmap();
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        else
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

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
