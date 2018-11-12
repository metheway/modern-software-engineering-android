package com.example.lenovo.cameraapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class Activity_camera extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static final int Take_photo = 1;
    private ImageView picture;
    private Uri imageUri;
    public static final int SKETCH = 2;
    public static final int TYPE_TRANSFORM = 1;
    //设置素描的常量请求
    //set variables Uri and ImageView as imageUri and picture

    //    private ImageView mImageView;
    private Button mTransform;
    private ProgressDialog dialog;
    private Bitmap mSourceBitmap;
    private Bitmap mTransformedBitmap;



    private static final int REQUEST_IMAGE_GET = 0 ;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static final String IMAGE_FILE_NAME = "icon.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button transform = (Button) findViewById(R.id.transform);
        //find two buttons we need ,the takePhoto and transform

        picture = (ImageView) findViewById(R.id.picture);
        //find the ImageView

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if the User click the button takePhoto
                String fileName = new Date().getTime() + ".jpg";
                File outputImage = new File(getExternalCacheDir(), fileName);
//                创建一个新的文件夹，用fileName命名，实际上就是时间戳来命名，但是jpg格式的,在sd卡下面按日期命名

                //String path = Environment.getExternalStorageDirectory()+File.separator+ "image";
                //File finalImage = new File(path, fileName);
                //if(!finalImage.getParentFile().exists()){
                //finalImage.getParentFile().mkdirs();
                //}
//              更新图片，如果输出图片存在的话，则删除
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = PhotoClipperUtil.getUriFromFile(Activity_camera.this,outputImage);
                requestPermission();
                //to ask the right to visit camera
            }

        });

        transform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent transformer = new Intent(Activity_camera.this,
                        Activity_Transform.class);
                transformer.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                //把imageUri加上发送到转换类，这样子可以直接调用图片到源文件
                startActivity(transformer);
                finish();
            }
        });

        picture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String radius = "10";
                int r = Integer.parseInt(radius);
                if (view == mTransform) {
                    new BackGroundTask(imageUri, mTransformedBitmap, picture,
                            dialog, mSourceBitmap, this).execute(new
                            Integer[]{TYPE_TRANSFORM, r});
                    //传入转换TYPE_TRANSFORM和r，在后台执行
                } else if (view == picture) {
                    if (mSourceBitmap != null) {
                        picture.setImageBitmap(mSourceBitmap);
                    }
                    //点击图片，如果还有源图片，那么显示，这样可以显示原来的图片。
                }
            }
        });

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //@Override
    //it it a method making a request to the user to get permission of using camera
    //but i haven't figure it out why overide
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA}, 1);
            //if not permitted by the user ,send a message to the user to get the right
        } else {
            opencamera();
            //if permitted by the user already ,then start open the camera directly
        }
    }

    //to get the response from the user
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    opencamera();
                    //if the user permmited to open the camera ,open it
                } else {
                    Toast.makeText(this, "权限被拒绝了", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void opencamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //build the intent of capture image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //add some info into the intent ,such as imageUri
        startActivityForResult(intent, Take_photo);
        //start to execute the intent and ask for result
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Take_photo:
//              if the intent is take_photo
                if (resultCode == RESULT_OK) {
//              if the resultCode is ok then
                    try {
                        Toast.makeText(MyApplication.getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().
                                openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                        //显示保存成功，而且用BitmapFactory解析imageUri，也就是意图里面的imageUri
                        //外界的程序可以通过getContentResolver()访问，这里暴露了接口，要和getContentProvider里面的接口相对应
                        //从imageUri取出数据流，用BitmapFactory转换成bitmap，然后放在视图picture里面
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
        //初始化菜单，显示菜单
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTransformedBitmap != null) ;
        mTransformedBitmap.recycle();
        mTransformedBitmap = null;
        //如果转换的 Bitmap还占用内存，清空
    }


/*    @Override
    public void onClick(View view) {
        String radius = "10";
        int r = Integer.parseInt(radius);
        if (view == mTransform) {
            new BackGroundTask(imageUri, mTransformedBitmap, picture,
                    dialog, mSourceBitmap, mEditView, this).execute(new
                    Integer[]{TYPE_TRANSFORM, r});
            //传入转换TYPE_TRANSFORM和r，在后台执行
        } else if (view == picture) {
            if (mSourceBitmap != null) {
                picture.setImageBitmap(mSourceBitmap);
            }
            //点击图片，如果还有源图片，那么显示，这样可以显示原来的图片。
        }

    }*/
}