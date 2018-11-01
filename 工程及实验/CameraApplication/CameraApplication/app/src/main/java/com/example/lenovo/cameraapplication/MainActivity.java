package com.example.lenovo.cameraapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    int SELECT_PIC ;
    int SELECT_CLIPPER_PIC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button photo = (Button)findViewById(R.id.Photo_main);
        Button picture = (Button)findViewById(R.id.Pictur_main);

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_photo = new Intent(MainActivity.this, Activity_camera.class);
                startActivity(intent_photo);
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "";
                goAlbume(path);

                Intent intent_picture = new Intent(MainActivity.this, Activity_Transform.class);
                intent_picture.putExtra("picture",path);
                startActivity(intent_picture);
            }
        });
    }
    private void goAlbume(String path){

        Intent take_photo = new Intent(Intent.ACTION_GET_CONTENT);
        take_photo.addCategory(Intent.CATEGORY_OPENABLE);
        take_photo.setType("image/*");
        startActivityForResult(take_photo,SELECT_PIC);
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (data == null) {
//            return;
//        }
//        switch (requestCode) {
//            case SELECT_PIC:
//                //获取图片后裁剪图片
//                clipperBigPic(this, data.getData());
//                break;
//            case SELECT_CLIPPER_PIC:
//                //获取图片后保存图片到本地，是否需要保存看情况而定
//                //saveBitmap(data);
//                //showImage(mGoAlarmIv);  //显示图片
//
//                break;
//        }
//    }



    private void clipperBigPic(Context context, Uri uri) {
        if (null == uri) {
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String url = PhotoClipperUtil.getPath(context, uri);
            intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
        }
        //发送裁剪命令
        intent.putExtra("crop", true);
        //X方向上的比例
        intent.putExtra("aspectX", 1);
        //Y方向上的比例
        intent.putExtra("aspectY", 1);
        //裁剪区的宽
        intent.putExtra("outputX", 124);
        //裁剪区的高
        intent.putExtra("outputY", 124);
        //是否保留比例
        intent.putExtra("scale", true);
        //返回数据
        intent.putExtra("return-data", true);
        //输出图片格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        //裁剪图片保存位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
        startActivityForResult(intent, SELECT_CLIPPER_PIC);
    }

    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /**
     * 临时图片保存路径
     * @return
     */
    private File getTempFile() {
        String fileName = new Date().getTime()+".jpg";
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}