package com.example.ourapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;

public class  ResultActivity extends AppCompatActivity {

    private String APP_ID ="wx93285cfd2b026fc0";
    private String path = (String)getIntent().getSerializableExtra("path");

    private IWXAPI wxApi = WXAPIFactory.createWXAPI(ResultActivity.this,APP_ID);
    private static final int ThUMB_SIZE = 150;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //user button

        ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent user = new Intent(ResultActivity.this,menu_user.class);

            }
        });

        //back_button

        ImageButton result_back = (ImageButton)findViewById(R.id.result_back);
        result_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //picture_result

        ImageView result_picture = (ImageView)findViewById(R.id.result_image);

        //result_picture.setImageBitmap(bm);




        Button result_share = (Button)findViewById(R.id.result_share);

        result_share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            image_share(path,0);
                        }
        });
    }
    public void image_share(String imgurl, int sendtype){
        File file = new File(imgurl);
        if(!file.exists()){
            Toast.makeText(ResultActivity.this, "图片不存在",Toast.LENGTH_LONG).show();
        }
        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(imgurl);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        Bitmap bmp = BitmapFactory.decodeFile(imgurl);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp,ThUMB_SIZE,ThUMB_SIZE,true);
        msg.setThumbImage(thumbBmp);
        bmp.recycle();
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = sendtype==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);

    }
}
