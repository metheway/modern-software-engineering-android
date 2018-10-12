package com.example.fetchpicturetest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    static final int REQUEST_OPEN_IMAGE = 1;
    private static final String TAG = "My cut";

    MyCropView cropView;
    String mCurrentPhotoPath;
    boolean targetChose = false;
    ProgressDialog dlg;
    private Button select;
    private Button cut;
    private Button modify;
    private Button saveImage;
    private Bitmap originalBitmap;
    private ImageView choiceView;
    private boolean hasCut =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //我的切片视图
        cropView = (MyCropView) findViewById(R.id.myCropView);
        //选择切割视图
        select = (Button) findViewById(R.id.btn_gray_process);
        //切割转换
        cut = (Button) findViewById(R.id.btn_cut_process);
        //修改
        modify = (Button) findViewById(R.id.btn_modify_process);
        //保存图像
        saveImage = (Button) findViewById(R.id.btn_save_process);
        //选择图像
        choiceView = (ImageView) findViewById(R.id.croppedImageView);
        select.setOnClickListener(this);
        cut.setOnClickListener(this);
        saveImage.setOnClickListener(this);
        modify.setOnClickListener(this);
        dlg = new ProgressDialog(this);

    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dlg != null) {
            dlg.dismiss();
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. " +
                    "Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found " +
                    "inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
            }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {//返回用户请求的结果，请求返回的结果根据请求的不同响应也是不同的
            case REQUEST_OPEN_IMAGE://如果请求的是REQUEST_OPEN_IMAGE，也就是select的那个意图
                if (resultCode == RESULT_OK) {//如果用户是选了是的话
                    Uri imgUri = data.getData();
                    //得到图片的uri，开始访问
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    //找到文件路径的列，MediaStore是所有文件共享的
                    Cursor cursor = getContentResolver().query(imgUri, filePathColumn,
                            null, null, null);
                    cursor.moveToFirst();
                    //得到光标，移动到第一行
                    int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mCurrentPhotoPath = cursor.getString(colIndex);
                    //得到路径的字符串
                    cursor.close();
                    setPic();
                    //从图库中选择bmp图片传递给cropView，也就是上方的视图，同时也设置了原始的bmp图
                }
                break;
            default:
                break;
        }
    }
    //将选择的图片放到上方的那个显示框也就是切割视图cropView
    public void setPic(){

        originalBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        //原始bmp图 为从mCurrentPhotoPath里面解析文件的图，也就是用户选择的视图
/*        cropView.setBmpPath(mCurrentPhotoPath);
        //设置用户选择的视图的路径给切割视图，作为bmp放在上面*/
        //这里应该设置一下尺寸，不能直接放进去

    }

    //选择剪切区域，显示，这里设置的是choiceView还有切割的bmp图像，其实就是显示已经选择的切割部分
    private void selectImageCut(){
        targetChose = true;//目标选择标记
        //实际上这里应该设置缩放选择切面的框框
        try{
            Bitmap cropBitmap = cropView.getCroppedImage();//得到切割视图里面的切割bmp
            choiceView.setImageBitmap(cropBitmap);//将切割的bmp图像设置在选择视图里
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_gray_process:
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
                    Toast.makeText(this,"you denied the request",Toast.LENGTH_SHORT).show();
                } else {
                    selectFromAlbum();
                }
                //如果用户允许选择相册，那么进入，不允许，那么给个Toast提示。
                break;
            case R.id.btn_cut_process:
                //抠图是耗时的过程，子线程中运行，并dialog提示
                if (targetChose){//如果选择中了图，那么执行线程
                    dlg.show();
                    dlg.setMessage("正在抠图...");
                    final RectF croppedBitmapData = cropView.getCroppedBitmapData();
                    final int croppedBitmapWidth = cropView.getCroppedBitmapWidth();
                    final int croppedBitmapHeight = cropView.getCroppedBitmapHeight();
                    //得到切割视图的bmp数据
                    //得到切割视图的tmp高和宽
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap bitmap = cupBitmap(originalBitmap,
                                    (int) croppedBitmapData.left, (int) croppedBitmapData.top,
                                    croppedBitmapWidth, croppedBitmapHeight);
                            //从原始的bmp中选出切割后的bmp，而其中遮板的边缘是从cropView里面选择出来的
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dlg.dismiss();
                                    //只是显示清空
                                    hasCut = true;
                                    //标记已经切割了
                                    choiceView.setImageBitmap(bitmap);
                                    //下方的选择视图上放上切割后的bmp
                                }
                            });
                        }
                    }).start();
                    //首先判断有没有选中，其次是要根据cropView就是切割视图来进行抠图，所以切割视图应该在此前完成
                    //cropView的选择
                }
                break;
            case R.id.btn_modify_process:
                selectImageCut();//如果选择的是修改，那么进入选择图片切面的函数
                break;
            case R.id.btn_save_process:
                if (hasCut){//如果已经切割了，那么就可以保存
                    String s = saveImageToGalleryString(this, ((BitmapDrawable)
                            (choiceView).getDrawable()).getBitmap());
                    Toast.makeText(this, "保存在"+s, Toast.LENGTH_SHORT).show();
                }else {//如果还没切割，那么提示
                    Toast.makeText(this, "请先扣图", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    //如果用户点击确定，实施方法，如果点击取消，则返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectFromAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void selectFromAlbum() {
        //如果选择了选图功能，从相册选图，需要向用户申请权限
        Intent getPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        //Create an intent with a given action and for a given data url创建一个得到动作内容的意图
        // 这个是隐式的intent，不明确启动哪个活动，需要action和category都匹配才能响应
        getPictureIntent.setType("image/*");
        //设置获取图片意图的类型，image/*是图片，audio/*是音频，video/*是视频，但是它没有用这个
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //捡取图片的意图，从相册选取图片，URI是getContentUri("external")
        //图片商店里面的图片，获取内容的URI，我不知道7.0以后这样获取是真是的uri吗，但是可行
        Intent chooserIntent = Intent.createChooser(getPictureIntent, "Select Image");
        //选择的意图，就是选取照片范围的意图，这个意图可以加上手工选取的部分
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {
                pickPictureIntent
                //增加了外部文件的意图
                //这里在选择意图里面放进了意图数组，数组包含了捡取意图
                //意思就是既可以选择相册，又可以选择文件
        });
        startActivityForResult(chooserIntent, REQUEST_OPEN_IMAGE);//请求打开图像，从相册
        //用请求requestcode是已经定义好了的1，选择意图并着捡取图片的意图一起启动
    }


    private Bitmap cupBitmap(Bitmap bitmap, int x, int y, int width, int height){
        Mat img = new Mat();
    //缩小图片尺寸
        bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);

        //bitmap->mat
        Utils.bitmapToMat(bitmap, img);
    //转成CV_8UC3格式
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);
    //设置抠图范围的左上角和右下角
        Rect rect = new Rect(x,y,width,height);
    //生成遮板
        Mat firstMask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        //这是实现抠图的重点，难点在于rect的区域，为了选取抠图区域，我借鉴了某大神的自定义裁剪View，返回坐标和宽高
        Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel,5, Imgproc.GC_INIT_WITH_RECT);
        Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

    //抠图
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        img.copyTo(foreground, firstMask);

    //mat->bitmap
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(foreground,bitmap1);
        return bitmap1;
    }
    //保存在系统图库
    public static String saveImageToGalleryString(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
