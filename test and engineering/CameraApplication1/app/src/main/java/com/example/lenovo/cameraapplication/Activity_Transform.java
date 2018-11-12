package com.example.lenovo.cameraapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class Activity_Transform /*extends AppCompatActivity implements View.OnClickListener*/{

/*    private ImageView mImageView;
    private EditText mEditView;
    private Button mTransform;

    private ProgressDialog dialog;
    private Bitmap mSourceBitmap;
    private Bitmap mTransformedBitmap;

    private String imageUri;
    public static final int TYPE_TRANSFORM = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transform);

        mImageView =(ImageView)findViewById(R.id.show_view);
        mImageView.setOnClickListener(this);

        mEditView = (EditText)findViewById(R.id.transform_radius);

        mTransform = (Button)findViewById(R.id.trans_trans);
        imageUri = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
        //得到传入文件的uri，然后用来放到源视图里面
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
        //初始化菜单，显示菜单
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTransformedBitmap != null);
        mTransformedBitmap.recycle();
        mTransformedBitmap = null;
        //如果转换的 Bitmap还占用内存，清空
    }

    @Override
    public void onClick(View view) {
        String radius = "10";
        int r = Integer.parseInt(radius);
        if(view == mTransform){
            new BackGroundTask(imageUri,mTransformedBitmap,mImageView,
                    dialog,mSourceBitmap,mEditView,this).execute(new
                    Integer[]{TYPE_TRANSFORM,r});
        //传入转换TYPE_TRANSFORM和r，在后台执行
        }else if(view == mImageView){
            if(mSourceBitmap != null){
                mImageView.setImageBitmap(mSourceBitmap);
            }
            //点击图片，如果还有源图片，那么显示，这样可以显示原来的图片。
        }

    }*/

}
