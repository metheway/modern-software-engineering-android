package com.channelst.headimgclip;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Luojingjing on 2017/11/3.
 */

public class CommonUtil {
    private static Point deviceSize = null;

    public static Point getDeviceSize(Context context) {
        if (deviceSize == null) {
            deviceSize = new Point(0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                ((WindowManager) context
                        .getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay().getSize(deviceSize);
            } else {
                Display display = ((WindowManager) context
                        .getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
                deviceSize.x = display.getWidth();
                deviceSize.y = display.getHeight();
                display = null;
            }
        }
        return deviceSize;
    }

    /**
     * @param ctx
     * @param path
     * @return
     * @Title: getImageRotationByPath
     * @Description: 根据图片路径获得其旋转角度
     */
    public static int getImageRotationByPath(Context ctx, String path) {
        int rotation = 0;
        if (TextUtils.isEmpty(path)) {
            return rotation;
        }

        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.ORIENTATION},
                    MediaStore.Images.Media.DATA + " = ?",
                    new String[]{"" + path}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                rotation = cursor.getInt(0);
            } else {
                rotation = getImageRotationFromUrl(path);
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return rotation;
    }
    /**
     * @Description: 获取下载图片的旋转角度，从网络传输图片，只能用带方法
     * @param path
     *            :图片路径
     * @return 返回图片角度
     */
    public static int getImageRotationFromUrl(String path) {
        int orientation = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;
                    break;
                default:
                    orientation = 0;
            }
        } catch (Exception e) {

            orientation = 0;
        }
        return orientation;
    }
}
