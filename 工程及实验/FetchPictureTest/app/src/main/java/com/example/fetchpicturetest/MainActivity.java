package com.example.fetchpicturetest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dlg != null) {
            dlg.dismiss();
        }
    }

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
        cropView = (MyCropView) findViewById(R.id.myCropView);
        select = (Button) findViewById(R.id.btn_gray_process);
        cut = (Button) findViewById(R.id.btn_cut_process);
        modify = (Button) findViewById(R.id.btn_modify_process);
        saveImage = (Button) findViewById(R.id.btn_save_process);
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
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    //从图库中选择图片
    public void setPic(){
        originalBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        cropView.setBmpPath(mCurrentPhotoPath);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri imgUri = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(imgUri, filePathColumn,
                            null, null, null);
                    cursor.moveToFirst();

                    int colIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mCurrentPhotoPath = cursor.getString(colIndex);
                    cursor.close();
                    setPic();
                }
                break;
        }
    }

    //选择剪切区域
    private void selectImageCut(){
        targetChose = true;
        try{
            Bitmap cropBitmap = cropView.getCroppedImage();
            choiceView.setImageBitmap(cropBitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_gray_process:
                Intent getPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getPictureIntent.setType("image/*");
                Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent chooserIntent = Intent.createChooser(getPictureIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {
                        pickPictureIntent
                });
                startActivityForResult(chooserIntent, REQUEST_OPEN_IMAGE);
                break;
            case R.id.btn_cut_process:
                //抠图是耗时的过程，子线程中运行，并dialog提示
                if (targetChose){
                    dlg.show();
                    dlg.setMessage("正在抠图...");
                    final RectF croppedBitmapData = cropView.getCroppedBitmapData();
                    final int croppedBitmapWidth = cropView.getCroppedBitmapWidth();
                    final int croppedBitmapHeight = cropView.getCroppedBitmapHeight();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap bitmap = cupBitmap(originalBitmap, (int) croppedBitmapData.left, (int) croppedBitmapData.top, croppedBitmapWidth, croppedBitmapHeight);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dlg.dismiss();
                                    hasCut = true;
                                    choiceView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }).start();

                }
                break;
            case R.id.btn_modify_process:
                selectImageCut();
                break;
            case R.id.btn_save_process:
                if (hasCut){
                    String s = saveImageToGalleryString(this, ((BitmapDrawable) (choiceView).getDrawable()).getBitmap());
                    Toast.makeText(this, "保存在"+s, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "请先扣图", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private Bitmap cupBitmap(Bitmap bitmap, int x, int y, int width, int height){
        Mat img = new Mat();
    //缩小图片尺寸
        // Bitmap bm = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
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
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
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
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

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
