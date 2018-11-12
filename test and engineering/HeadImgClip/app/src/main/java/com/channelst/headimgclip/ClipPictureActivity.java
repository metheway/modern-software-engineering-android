package com.channelst.headimgclip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * 自定义裁剪activity
 */
public class ClipPictureActivity extends AppCompatActivity {

    public static final String TAG = "ClipPictureActivity";
    public static final String IMAGE_PATH_ORIGINAL = "image_path_original";
    public static final String IMAGE_PATH_AFTER_CROP = "image_path_after_crop";
    @BindView(R.id.back_btn)
    Button backBtn;
    @BindView(R.id.save_btn)
    Button saveBtn;
    @BindView(R.id.title_txt)
    TextView titleTxt;
    @BindView(R.id.src_pic)
    ImageView srcPic;
    @BindView(R.id.src_layout)
    FrameLayout srcLayout;


    //裁剪图片的自定义view
    private ClipView clipview;
    //保存裁剪后图片的路径
    private String croppedImagePath;

    //缩放比例
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    /**
     * 动作标志：无
     */
    private static final int NONE = 0;
    /**
     * 动作标志：拖动
     */
    private static final int DRAG = 1;
    /**
     * 动作标志：缩放
     */
    private static final int ZOOM = 2;
    /**
     * 初始化动作标志
     */
    private int mode = NONE;

    /**
     * 记录起始坐标
     */
    private PointF start = new PointF();
    /**
     * 记录缩放时两指中间点坐标
     */
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Bitmap bitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_clippicture);
        ButterKnife.bind(this);

        initView();

    }

    /**
     * 初始化view
     * 当图片的imgView初始化完成后再初始化clipView
     */
    private void initView() {

        ViewTreeObserver observer = srcPic.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                srcPic.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initClipView();
            }
        });

    }

    /**
     * 初始化截图区域，并将源图按裁剪框比例缩放
     *
     */
    private void initClipView() {

        Intent intent = getIntent();
        final String originalImgPath = intent.getStringExtra(IMAGE_PATH_ORIGINAL);
        croppedImagePath = intent.getStringExtra(IMAGE_PATH_AFTER_CROP);

        // 首先判断源文件是否存在--防止垃圾数据的影响：
        // 一张图片在SD卡上已经被删除，但是媒体库中还有该数据。
        File file = new File(originalImgPath);
        if (!file.exists()) {
            Toast.makeText(this, "源文件在SD卡上不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //获取请求设置的图片宽高
        int reqWidth = srcPic.getWidth();
        int reqHeight = srcPic.getHeight();
        // 取bitmap的时候就压缩照片，同时解决了照片过大(>2048*2048px)的硬件加速问题(渲染不了)
        // 和图片过大(20M),内存溢出问题
        bitmap = decodeSampledBitmapFromFile(originalImgPath,
                reqWidth, reqHeight);

        if (bitmap == null) {
            Toast.makeText(this, "不是合法的图片文件，请重新选择图片", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }
        int rotation = CommonUtil.getImageRotationByPath(
                ClipPictureActivity.this, originalImgPath);
        // 取出照片角度，解决某些手机上照片显示角度问题
        if (rotation != 0) {
            Matrix m = new Matrix();
            m.setRotate(rotation);
            try {
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), m, true);
            } catch (OutOfMemoryError e) {
            }
        }

        // 因为某些不规范操作导致出现0bit的图片，使得bitmap==null，抛出NullPointerException
        if (bitmap == null) {
            Toast.makeText(this, "图片不存在或已被删除", Toast.LENGTH_SHORT).show();
            return;
        }

        //初始化截图区域自定义view
        clipview = new ClipView(ClipPictureActivity.this);
        clipview.addOnDrawCompleteListener(new ClipView.OnDrawListenerComplete() {

            public void onDrawComplete() {
                clipview.removeOnDrawCompleteListener();
                int radius = (int) clipview.getRadius();
                int midX = (int) clipview.getCircleCenterPX();
                int midY = (int) clipview.getCircleCenterPY();

                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                // 按裁剪框求缩放比例
                float scale = (radius * 3.0f) / imageWidth;

                // 起始中心点
                float imageMidX = imageWidth * scale / 2;
                float imageMidY = imageHeight * scale / 2;
                srcPic.setScaleType(ImageView.ScaleType.MATRIX);

                // 缩放
                matrix.postScale(scale, scale);
                // 平移
                matrix.postTranslate(midX - imageMidX, midY - imageMidY);

                srcPic.setImageMatrix(matrix);
                srcPic.setImageBitmap(bitmap);
            }
        });

        matrix.reset();
        srcLayout.addView(clipview, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    /**
     * 压缩图片,并设置图片宽高
     *
     * @param filename  源图片文件路径
     * @param reqWidth  需要将图片设置的宽度
     * @param reqHeight 需要将图片设置的高度
     * @return 返回压缩后并设置宽高的bitmap
     */
    public Bitmap decodeSampledBitmapFromFile(String filename,
                                              int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //这里decodeFile只是为了获取到原图的宽高,
        //这样option里面就保存了原图的宽高,后面在计算inSampleSize才能有值
        BitmapFactory.decodeFile(filename, options);

        //inSampleSize 这个值是一个int，当它小于1的时候，将会被当做1处理，
        //如果大于1，那么就会按照比例（1 / inSampleSize）缩小bitmap的宽和高、降低分辨率
        //这个参数也是为了压缩图片,减小分辨率,避免出现加载超大图片是出现oom
        options.inSampleSize = calculateInSampSize(options, reqWidth, reqHeight);

        //inJustDecodeBounds 如果将这个值置为true，
        //那么在解码的时候将不会返回bitmap，只会返回这个bitmap的尺寸。
        //但我们需要返回bitmap,所以设为false
        options.inJustDecodeBounds = false;

        //inPreferredConfig 这个值是设置色彩模式
        // 默认值是ARGB_8888，在这个模式下，一个像素点占用4bytes空间
        //一般对透明度不做要求的话，一般采用RGB_565模式，这个模式下一个像素点占用2bytes。
        //也就是说RGB_565模式可以节省一些内存空间
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            Bitmap val = BitmapFactory.decodeFile(filename, options);
            Log.i(TAG,"after decode image File:" + options.outWidth
                    + "|" + options.outHeight);
            return val;
        } catch (Exception e) {
            Log.e(TAG, "decodeSampledBitmapFromFile - Exception"
                    + e);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "decodeSampledBitmapFromFile - OutOfMemoryError"
                    + e);
        } finally {

        }
        return null;
    }

    /**
     * 根据原图的宽高和请求设置的宽高,计算出inSampleSize并返回
     * @param options   已经保存原图宽高的options
     * @param reqWidth  请求设置图片的宽度
     * @param reqHeight 请求设置图片的高度
     * @return  option的inSampleSize
     */
    public int calculateInSampSize(BitmapFactory.Options options,
                                   int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.i(TAG, "calculateInSampSize()  height : " + height
                + ", height : " + width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            inSampleSize = ((Double) Math.floor(height * 1.0f / reqHeight))
                    .intValue();
            int wSampSize = ((Double) Math.floor(width * 1.0f / reqWidth))
                    .intValue();
            if (inSampleSize < wSampSize) {
                inSampleSize = wSampSize;
            }
        }
        Log.i(TAG, "calculateInSampSize()  inSampleSize : " + inSampleSize);
        return inSampleSize;
    }


    /**
     * 点击事件处理
     * @param view
     */
    @OnClick({R.id.back_btn, R.id.save_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.save_btn:
                saveBitmap();
                break;
        }
    }


    /**
     * 保存图片
     * 首先获取裁剪框里的图片
     * 然后保存到裁剪文件croppedImagePath中
     */
    private void saveBitmap() {
        if (bitmap != null) {
            // 取出裁剪图片
            Bitmap clipBitmap = getBitmap();
            Log.i(TAG, "saveBitmap()  clipBitmap=" + clipBitmap);
            File file = new File(croppedImagePath);
            saveMyBitmap(file, clipBitmap);
        }

        Intent intent = new Intent();
        ClipPictureActivity.this.setResult(RESULT_OK, intent);
        finish();
    }


    /**
     * @return 裁剪后的图片
     * @description 获取裁剪框内截图
     */
    private Bitmap getBitmap() {

        try {
            // srcPic.getDrawingCache()获取View截图在某些情况下报错了。
            // 现在用一种新的获取view中图像的方法取代getDrawingCache()方法.
            // 另：在使用createBitmap()增加try..catch..以防止不断生成bitmap可能导致的oom
            int startX = (int) (clipview.getCircleCenterPX() - clipview.getRadius());
            int startY = (int) (clipview.getCircleCenterPY() - clipview.getRadius());
            Log.i(TAG, "getBitmap()：startX=" + startX
                    + ",startY=" + startY
                    + ",clipview.getClipWidth()=" + clipview.getClipWidth()
                    + ",clipview.getWidth()=" + clipview.getWidth()
                    + ",clipview.getCircleCenterPX()=" + clipview.getCircleCenterPX()
                    + ",clipview.getRadius()=" + clipview.getRadius()
                    + ",clipview.getCircleCenterPY()=" + clipview.getCircleCenterPY());
            Bitmap finalBitmap = Bitmap.createBitmap(
                    loadBitmapFromView(srcPic),
                    startX, startY, clipview.getClipWidth(),
                    clipview.getClipHeight());
            // 释放资源
            srcPic.destroyDrawingCache();
            Log.i(TAG, "getBitmap()  finalBitmap=" + finalBitmap);
            return getCircleBitmap(finalBitmap);
        } catch (OutOfMemoryError err) {
            Toast.makeText(this, "保存头像失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, err.getMessage());
            return null;
        } catch (Exception e) {
            Toast.makeText(this, "保存头像失败!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
            return null;
        }

    }


    /**
     * @description 获取圆形裁剪框内截图
     * @param bitmap src图片
     * @return
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        //在画布上绘制一个圆 -1是为了去掉白色的边框
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2 - 1, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        Log.i(TAG, "getCircleBitmap()     output=" + output);
        return output;
    }

    /**
     * @return void
     * @Title: saveMyBitmap
     * @Description: 保存bitmap对象到裁剪后的文件中
     */
    public static void saveMyBitmap(File file, Bitmap mBitmap) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据view的宽高,截图当前view显示的图像
     * @param v     需要截图的view
     * @return  截取view当前显示的图像,返回的bitmap
     */
    public Bitmap loadBitmapFromView(View v) {
        if (v == null) {
            return null;
        }
        try {
            Bitmap screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(screenshot);
            v.draw(c);
            return screenshot;
        } catch (OutOfMemoryError err) {
        }
        return null;
    }


    /**
     * @param bitmap    原图
     * @param widthPix  请求将图片缩放的宽度
     * @param heightPix 请求将图片缩放的高度
     * @return 返回大小为widthPix*heightPix的头像
     * @description 将bitmap的尺寸缩放到widthPix*heightPix
     * 如果对图片的宽高需要限定可以用这个方法处理
     */
    private Bitmap scaleBitmap(Bitmap bitmap, int widthPix, int heightPix) {

        Matrix sMatrix = new Matrix();
        // 图片缩放比
        double widthRatio = (widthPix * 1.0) / bitmap.getWidth();
        double heightRatio = (heightPix * 1.0) / bitmap.getHeight();
        sMatrix.postScale((float) widthRatio, (float) heightRatio);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), sMatrix, true);
        return resizeBmp;

    }


    @OnTouch(R.id.src_pic)
    public boolean onViewTouched(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //在第一个点被按下时触发
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(srcPic.getImageMatrix());
                // 设置开始点位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            //当屏幕上已经有点被按住，此时再按下其他点时触发。
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                // 最先放下的两个手指距大于10像素时
                if (oldDist > 10f) {
                    savedMatrix.set(srcPic.getImageMatrix());
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            //当屏幕上唯一的点被放开时触发
            case MotionEvent.ACTION_UP:
                break;
            // 当触点离开屏幕，但是屏幕上还有触点(手指)时触发
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            //当有点在屏幕上移动时触发
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {//只有一个触点时,通过两点之间的距离移动
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                } else if (mode == ZOOM) {//当有两个以上触点时
                    float newDist = spacing(event);//计算最先两点现在的距离
                    //因为ACTION_MOVE很灵敏,会因手指颤抖一直被触发,这里我们添加一个判断,
                    //当现在的两点距离比老的两点之间的距离的改变幅度>5个像素就执行缩放,
                    //这样可以减少非意愿的微小缩放,同时节省一点内存空间
                    boolean changeDist = (newDist>oldDist+5)?true:
                            (newDist<oldDist-5?true:false);
                    if (changeDist) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        view.setImageMatrix(matrix);//缩放或者拖动
        return true;
    }

    /**
     * 多点触控时，计算最先放下的两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     *
     * @param point
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

}
