package libs.source.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import timber.log.Timber;

/**
 * Description:
 *
 * @author admin
 * CreateDate: 2021/4/22
 */
public class AndUtils {
    public static boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) > 0.5;
    }

    public static void calcStatusBarViewPrimaryColor(@NonNull View view, final int defaultColor, final AsyncListener<Integer> asyncListener) {
        Timber.d("Palette:  defaultColor:  " +defaultColor);
        WindowManager wm = (WindowManager) view.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        //屏幕的宽度
        int width = outMetrics.widthPixels;
        float density = outMetrics.density;
        int height = (int) (48 * density);

        view.requestLayout();
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        view.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final int[] result = {defaultColor};
        if (bitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            view.draw(canvas);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    if (palette != null) {
                        result[0] = palette.getLightVibrantColor(defaultColor);
                        asyncListener.onResult(result[0]);
                        Timber.d("Palette:  " + result[0]);
                    }
                }
            });
        } else {
            asyncListener.onResult(result[0]);
        }
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        view.invalidate();
        view.requestLayout();

    }

    public interface AsyncListener<T> {
        void onResult(T data);
    }
}
