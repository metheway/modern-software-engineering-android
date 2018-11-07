package com.example.cutpicture;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GET = 0 ;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static final String IMAGE_FILE_NAME = "icon.jpg";

    private ImageView main_icon;
    private PhotoPopupWindow mPhotoPopupWindow;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_icon =(ImageView)findViewById(R.id.main_icon);
        Button main_btn = (Button)findViewById(R.id.main_btn);
        main_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mPhotoPopupWindow = new PhotoPopupWindow(MainActivity.this,
                        new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                //权限申请，相册
                                if(ContextCompat.checkSelfPermission(MainActivity.this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                                        PackageManager.PERMISSION_GRANTED){
                                    //权限还没有授予，需要在这里申请
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},200);
                                }else{
                                    mPhotoPopupWindow.dismiss();
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    //判断系统中有没有处理这个intent的活动
                                    if(intent.resolveActivity(getPackageManager())!= null){
                                        startActivityForResult(intent,REQUEST_IMAGE_GET);
                                    }else{
                                        Toast.makeText(MainActivity.this,"没有找到图片查看器",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        },
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //选择的是拍照的话，判断有没有权限
                        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission
                                .CAMERA)!=PackageManager.PERMISSION_GRANTED
                                ){
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission
                                    .CAMERA
                            },300);
                        }else{
                            //权限已经申请了，那么直接拍照
                            mPhotoPopupWindow.dismiss();
                            imageCapture();
                        }
                    }
                });
                View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main,null);
                //动态加载页面
                mPhotoPopupWindow.showAtLocation(rootView,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //回调成功
        if(resultCode == RESULT_OK){
            switch (requestCode){
                //小图切割
                case REQUEST_SMALL_IMAGE_CUTTING:
                    if(data != null){
                        setPicToView(data);
                    }
                    break;
                //大图切割
                case REQUEST_BIG_IMAGE_CUTTING:
                    Bitmap bitmap = BitmapFactory.decodeFile(mImageUri.getEncodedPath());
                    main_icon.setImageBitmap(bitmap);
                    //这里已经设置了图片？为什么不起效果
                    break;
                    //相册选取
                case REQUEST_IMAGE_GET:
                    try {
                        //很显然，相册里面选图肯定是大图
                        startBigPhotoZoom(data.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    //相机照的图片也是大图
                    File temp = new File(Environment.getExternalStorageDirectory() + "/" +
                    IMAGE_FILE_NAME);
                    startBigPhotoZoom(temp);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 200 :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mPhotoPopupWindow.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    //判断系统中是否有处理该intent的活动
                    if(intent.resolveActivity(getPackageManager())!= null){
                        startActivityForResult(intent,REQUEST_IMAGE_GET);
                    }else{
                        Toast.makeText(MainActivity.this,"未找到图片查看器",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    mPhotoPopupWindow.dismiss();
                }
                break;
            case 300:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mPhotoPopupWindow.dismiss();
                    imageCapture();
                }else{
                    mPhotoPopupWindow.dismiss();
                }
                break;
            default:
                break;
        }
    }

    private void imageCapture() {
        //拍照
        Intent intent ;
        Uri pictureUri;
        File pictureFile = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);
        //判断当前系统
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(this,
                    "com.example.cutpicture.fileProvider",pictureFile);
        }else{
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        //去拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT,pictureUri);
        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
    }
    /**
     * 大图模式切割图片
     * 直接创建一个文件将切割后的图片写入
     */
    public void startBigPhotoZoom(File inputFile){
        //创建大图文件夹
        Uri imageUri = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //如果已经装上了sd卡
            String storage = Environment.getExternalStorageDirectory().getPath();
            File dirFile = new File(storage + "/bigIcon");
            if(!dirFile.mkdirs()){
                Log.e("TAG","文件夹创建失败");
            }else{
                Log.e("TAG","文件夹创建成功");
            }
            File file =new File(dirFile,System.currentTimeMillis() + ".jpg");
            imageUri = Uri.fromFile(file);
            mImageUri = imageUri;
        }
        //开始切割
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(MainActivity.this,inputFile),"image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop","true");
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);//裁剪框的比例
        intent.putExtra("outputX",600);//输出图片的大小
        intent.putExtra("outputY",600);
        intent.putExtra("scale",true);
        intent.putExtra("return-data",false);//返回数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);//这个是输出图片的uri,原来代码是imageUri
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent,REQUEST_BIG_IMAGE_CUTTING);
    }
    //重写
    public void startBigPhotoZoom(Uri uri){
        Uri imageUri = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String storage = Environment.getExternalStorageDirectory().getPath();
            File dirFile = new File(storage + "\bigIcon");
            if(!dirFile.exists()){
                if(!dirFile.mkdirs()){
                    Log.e("TAG","文件夹创建失败");
                }else{
                    Log.e("TAG","文件夹创建成功");
                }
            }
            File file = new File(dirFile,System.currentTimeMillis()+ ".jpg");
            imageUri = Uri.fromFile(file);
            mImageUri = imageUri;//将uri传出方便显示到视图中
        }
        //开始切割
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop","true");
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",600);
        intent.putExtra("outputY",600);
        intent.putExtra("scale",1);
        intent.putExtra("return-data",false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);//输出文件的uri
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent,REQUEST_BIG_IMAGE_CUTTING);
    }


    private Uri getImageContentUri(Context context, File inputFile) {
        String filePath = inputFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "?",
                new String[]{filePath},null
        );
        if(cursor != null && cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri,""+ id);
        }else{
            if(inputFile.exists()){
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA,filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values
                );
            }else{
                return null;
            }
        }
    }
    //创建大图
    /**
     * 小图模式切割图片
     * 此方式直接返回截图后的 bitmap，由于内存的限制，返回的图片会比较小
     */
    public void startSmallPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1); // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300); // 输出图片大小
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING);
    }

    /**
     * 小图模式中，保存图片后，设置到视图中
     * 将图片保存设置到视图中
     */
    private void setPicToView(Intent data){
        Bundle extras = data.getExtras();
        if(extras != null){
            Bitmap photo = extras.getParcelable("data");
            //创建smallIcon文件夹
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                String storage = Environment.getExternalStorageDirectory().getPath();
                File dirFile = new File(storage + "/smallIcon");
                if(!dirFile.exists()){
                    if(!dirFile.mkdirs()){
                        Log.e("TAG","文件夹创建失败");
                    }{
                        Log.e("TAG","文件夹创建成功");
                    }
                }
                File file = new File(dirFile,System.currentTimeMillis()+".jpg");
                //保存图片
                try {
                    FileOutputStream outputStream;
                    outputStream = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            main_icon.setImageBitmap(photo);
        }
    }
}
