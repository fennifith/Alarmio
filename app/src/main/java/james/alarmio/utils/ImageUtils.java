package james.alarmio.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import james.alarmio.data.PreferenceData;

public class ImageUtils {

    public static Bitmap drawableToBitmap(Drawable drawable) {
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

    public static void getBackgroundImage(ImageView imageView) {
        String backgroundUrl = PreferenceData.BACKGROUND_IMAGE.getValue(imageView.getContext());
        if (backgroundUrl != null && backgroundUrl.length() > 0) {
            if (backgroundUrl.startsWith("http"))
                Glide.with(imageView.getContext()).load(backgroundUrl).into(imageView);
            else if (backgroundUrl.contains("://"))
                Glide.with(imageView.getContext()).load(Uri.parse(backgroundUrl)).into(imageView);
            else Glide.with(imageView.getContext()).load(new File(backgroundUrl)).into(imageView);
        }
    }

}
