package net.linkmate.app.poster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;

public class FastBlur {

  public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

    // Stack Blur v1.0 from
    // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
    //
    // Java Author: Mario Klingemann <mario at="" quasimondo.com="">
    // http://incubator.quasimondo.com
    // created Feburary 29, 2004
    // Android port : Yahel Bouaziz <yahel at="" kayenko.com="">
    // http://www.kayenko.com
    // ported april 5th, 2012

    // This is a compromise between Gaussian Blur and Box blur
    // It creates much better looking blurs than Box Blur, but is
    // 7x faster than my Gaussian Blur implementation.
    //
    // I called it Stack Blur because this describes best how this
    // filter works internally: it creates a kind of moving stack
    // of colors whilst scanning through the image. Thereby it
    // just has to add one new block of color to the right side
    // of the stack and remove the leftmost color. The remaining
    // colors on the topmost layer of the stack are either added on
    // or reduced by one, depending on if they are on the right or
    // on the left side of the stack.
    //
    // If you are using this algorithm in your code please add
    // the following line:
    //
    // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

    Bitmap bitmap;
    if (canReuseInBitmap) {
      bitmap = sentBitmap;
    } else {
      bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
    }

    if (radius < 1) {
      return (null);
    }

    int w = bitmap.getWidth();
    int h = bitmap.getHeight();

    int[] pix = new int[w * h];
    bitmap.getPixels(pix, 0, w, 0, 0, w, h);

    int wm = w - 1;
    int hm = h - 1;
    int wh = w * h;
    int div = radius + radius + 1;

    int r[] = new int[wh];
    int g[] = new int[wh];
    int b[] = new int[wh];
    int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
    int vmin[] = new int[Math.max(w, h)];

    int divsum = (div + 1) >> 1;
    divsum *= divsum;
    int dv[] = new int[256 * divsum];
    for (i = 0; i < 256 * divsum; i++) {
      dv[i] = (i / divsum);
    }

    yw = yi = 0;

    int[][] stack = new int[div][3];
    int stackpointer;
    int stackstart;
    int[] sir;
    int rbs;
    int r1 = radius + 1;
    int routsum, goutsum, boutsum;
    int rinsum, ginsum, binsum;

    for (y = 0; y < h; y++) {
      rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
      for (i = -radius; i <= radius; i++) {
        p = pix[yi + Math.min(wm, Math.max(i, 0))];
        sir = stack[i + radius];
        sir[0] = (p & 0xff0000) >> 16;
        sir[1] = (p & 0x00ff00) >> 8;
        sir[2] = (p & 0x0000ff);
        rbs = r1 - Math.abs(i);
        rsum += sir[0] * rbs;
        gsum += sir[1] * rbs;
        bsum += sir[2] * rbs;
        if (i > 0) {
          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];
        } else {
          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];
        }
      }
      stackpointer = radius;

      for (x = 0; x < w; x++) {

        r[yi] = dv[rsum];
        g[yi] = dv[gsum];
        b[yi] = dv[bsum];

        rsum -= routsum;
        gsum -= goutsum;
        bsum -= boutsum;

        stackstart = stackpointer - radius + div;
        sir = stack[stackstart % div];

        routsum -= sir[0];
        goutsum -= sir[1];
        boutsum -= sir[2];

        if (y == 0) {
          vmin[x] = Math.min(x + radius + 1, wm);
        }
        p = pix[yw + vmin[x]];

        sir[0] = (p & 0xff0000) >> 16;
        sir[1] = (p & 0x00ff00) >> 8;
        sir[2] = (p & 0x0000ff);

        rinsum += sir[0];
        ginsum += sir[1];
        binsum += sir[2];

        rsum += rinsum;
        gsum += ginsum;
        bsum += binsum;

        stackpointer = (stackpointer + 1) % div;
        sir = stack[(stackpointer) % div];

        routsum += sir[0];
        goutsum += sir[1];
        boutsum += sir[2];

        rinsum -= sir[0];
        ginsum -= sir[1];
        binsum -= sir[2];

        yi++;
      }
      yw += w;
    }
    for (x = 0; x < w; x++) {
      rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
      yp = -radius * w;
      for (i = -radius; i <= radius; i++) {
        yi = Math.max(0, yp) + x;

        sir = stack[i + radius];

        sir[0] = r[yi];
        sir[1] = g[yi];
        sir[2] = b[yi];

        rbs = r1 - Math.abs(i);

        rsum += r[yi] * rbs;
        gsum += g[yi] * rbs;
        bsum += b[yi] * rbs;

        if (i > 0) {
          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];
        } else {
          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];
        }

        if (i < hm) {
          yp += w;
        }
      }
      yi = x;
      stackpointer = radius;
      for (y = 0; y < h; y++) {
        // Preserve alpha channel: ( 0xff000000 & pix[yi] )
        pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

        rsum -= routsum;
        gsum -= goutsum;
        bsum -= boutsum;

        stackstart = stackpointer - radius + div;
        sir = stack[stackstart % div];

        routsum -= sir[0];
        goutsum -= sir[1];
        boutsum -= sir[2];

        if (x == 0) {
          vmin[y] = Math.min(y + r1, hm) * w;
        }
        p = x + vmin[y];

        sir[0] = r[p];
        sir[1] = g[p];
        sir[2] = b[p];

        rinsum += sir[0];
        ginsum += sir[1];
        binsum += sir[2];

        rsum += rinsum;
        gsum += ginsum;
        bsum += binsum;

        stackpointer = (stackpointer + 1) % div;
        sir = stack[stackpointer];

        routsum += sir[0];
        goutsum += sir[1];
        boutsum += sir[2];

        rinsum -= sir[0];
        ginsum -= sir[1];
        binsum -= sir[2];

        yi += w;
      }
    }

    bitmap.setPixels(pix, 0, w, 0, 0, w, h);

    return (bitmap);
  }


  public static Bitmap reSize(Bitmap bitmaporg) {
    int widthold = bitmaporg.getWidth();//eg:1024
    int heightold = bitmaporg.getHeight();//eg:819
    //获取屏幕的宽高
    DisplayMetrics dm = new DisplayMetrics();
    int screenWidth = 1920;  //eg:1440
    int screenHeigh = 1080;  //eg:2392
    float density = dm.density;  // 密度（0.75 / 1.0 / 1.5） //eg: 1dp=3.5px
    //int densityDpi = dm.densityDpi;  // 密度DPI（120 / 160 / 240）
    float scale = (float) screenWidth / ((density + 0.2f) * widthold);
    Matrix matrix = new Matrix();
    if (scale < 1) {
      //将大图缩小至适屏
      float scaleWidth = (float) scale;
      float scaleHeight = (float) scale;
      matrix.postScale(scaleWidth, scaleHeight);
    } else {
      float scaleWidth = 1;
      float scaleHeight = 1;
      matrix.postScale(scaleWidth, scaleHeight);
    }


    Bitmap bitmapnew = Bitmap.createBitmap(bitmaporg, 0, 0, widthold, heightold, matrix, true);
    int widtnew = bitmapnew.getWidth();
    int heightnew = bitmapnew.getHeight();
    return bitmapnew;

  }


  public static Bitmap blurBitmap(Context context, Bitmap image, float blurRadius) {
//    Bitmap inputBitmap = Bitmap.createScaledBitmap(image, Math.round(((float) image.getWidth()) * 1053609165), Math.round(((float) image.getHeight()) * 0.4f), false);
    Bitmap inputBitmap = Bitmap.createScaledBitmap(image, image.getWidth(),image.getHeight(), false);
    Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
    RenderScript rs = RenderScript.create(context);
    ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
    Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
    Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
    blurScript.setRadius(blurRadius);
    blurScript.setInput(tmpIn);
    blurScript.forEach(tmpOut);
    tmpOut.copyTo(outputBitmap);
    return outputBitmap;
  }

  public static String subRangeString(String body,String str1,String str2) {
    while (true) {
      int index1 = body.indexOf(str1);
      if (index1 != -1) {
        int index2 = body.indexOf(str2, index1);
        if (index2 != -1) {
//                    Log.e("Tag", "下标1为：" + index1 + "  下标2为:" + index2);
          String str3 = body.substring(0, index1) + body.substring(index2 +    str2.length(), body.length());
          String str4 = body.substring(index1+str1.length(), index2);
//                    Log.e("Tag", "str3为：" + str3);
          body = str4;
        }else {
          return body;
        }
      }else {
        return body;
      }
    }
  }

}
