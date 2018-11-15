package com.example.ourapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Activity_camera extends AppCompatActivity {



    private ImageView picture;
    private Uri imageUri;
    //设置素描的常量请求

    private Bitmap mSourceBitmap;
    private Bitmap mConvertedBitmap;
    Uri clipPhotoUri;



    private static final int radius = 10;
    private static final int TYPE_CONVERT = 3;
    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Button takePhoto = (Button) findViewById(R.id.back_button);
        Button transform = (Button) findViewById(R.id.transform);
        //find two buttons we need ,the takePhoto and transform

        clipPhotoUri = Uri.parse(getIntent().getStringExtra("photoUri"));
        picture = (ImageView) findViewById(R.id.picture);
        //find the ImageView
        //在创建的时候就可以把bitmap数据取出来放到ImageView上面
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(PhotoClipperUtil.getPath(this,clipPhotoUri));
        //这里用的游标找到的，说明在sqlite里面有记录，注意

        picture.setImageBitmap(bitmap);


        transform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConvertTask().execute(new Integer[] { TYPE_CONVERT, radius });
            }
        });



    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
/*            case Take_photo:
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
                break;*/
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
        //初始化菜单，显示菜单
    }

    private class ConvertTask extends AsyncTask<Integer, Void, Bitmap> {
        @Override
        protected void onPostExecute(Bitmap result) {
            mDialog.dismiss();
            if (result != null) {
                mConvertedBitmap = result;
                picture.setImageBitmap(result);
            }

        }

        @Override
        protected void onPreExecute() {
            if (mDialog == null) {
                mDialog = new ProgressDialog(Activity_camera.this);
            }
            mDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            int type = params[0];
            int r = params[1];
            if (mSourceBitmap == null) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) picture
                        .getDrawable();
                mSourceBitmap = bitmapDrawable.getBitmap();
            } else if (mConvertedBitmap != null) {
                mConvertedBitmap.recycle();
                mConvertedBitmap = null;
            }

            Bitmap result = null;
            switch (type) {
                case TYPE_CONVERT:
                    result = testSketch.testGaussBlur(mSourceBitmap, r, r / 3);
                    break;
            }

            return result;
        }

    }
/*    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTransformedBitmap != null) ;
        mTransformedBitmap.recycle();
        mTransformedBitmap = null;
        //如果转换的 Bitmap还占用内存，清空
    }*/

}