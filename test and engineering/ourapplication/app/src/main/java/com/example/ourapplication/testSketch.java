package com.example.ourapplication;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class testSketch {

    public static Bitmap testGaussBlur(Bitmap src, int r, int fai) {

        int width = src.getWidth();
        int height = src.getHeight();

        int[] pixels = Sketch.discolor(src);
        int[] copixels = Sketch.simpleReverseColor(pixels);
        Sketch.simpleGaussBlur(copixels, width, height, r, fai);
        Sketch.simpleColorDodge(pixels, copixels);
        //直接在颜色减淡部分更改下有用不看，看能不能变色
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height,
                Config.RGB_565);
        return bitmap;

    }

}
