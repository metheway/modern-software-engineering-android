package com.example.mergepicture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView firstView ;
    ImageView secondView ;
    Bitmap backGround;
    Bitmap secondBitmap;
    ImageView showView;
    Bitmap showBitmap;

    @Override
    public void onClick(View v) {
        showBitmap = mergeThumbnailBitmap(backGround,secondBitmap);
//        Drawable[] array = new Drawable[2];
//        array[0] = new BitmapDrawable(backGround);
//        array[1] = new BitmapDrawable(secondBitmap);
//        LayerDrawable la = new LayerDrawable(array);
//        la.setLayerInset(0,0,100,0,0);
//        la.setLayerInset(1,0,0,0,0);
//        showView.setImageDrawable(la);
//        firstView.setImageBitmap(backGround);
        showView.setImageBitmap(showBitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstView = this.findViewById(R.id.main_imgZooming);
        secondView = this.findViewById(R.id.main_imgZooming2);
        backGround = ((BitmapDrawable)firstView.getDrawable()).getBitmap();
        secondBitmap = ((BitmapDrawable)secondView.getDrawable()).getBitmap();
        showView = findViewById(R.id.main_imgZooming3);

        Button mainMerge = (Button)findViewById(R.id.main_merge);
        mainMerge.setOnClickListener(this);

    }
    //首先传入两张图片
       private Bitmap mergeThumbnailBitmap(Bitmap firstBitmap, Bitmap secondBitmap) {
        //以其中一张图片的大小作为画布的大小，或者也可以自己自定义
        Bitmap bitmap = Bitmap.createBitmap(firstView.getWidth(), firstView
                .getHeight(), firstBitmap.getConfig());
        //生成画布
        Canvas canvas = new Canvas(bitmap);
        //因为我传入的secondBitmap的大小是不固定的，所以我要将传来的secondBitmap调整到和画布一样的大小
//        float w = firstBitmap.getWidth();
//        float h = firstBitmap.getHeight();
        Matrix m = new Matrix();
        //确定secondBitmap大小比例
           float w = firstBitmap.getWidth();
           float h = firstBitmap.getHeight();

           m.setScale(  w / secondBitmap.getWidth(),   h / secondBitmap.getHeight()
           ,20,20);



        Paint paint = new Paint();
        //给画笔设定透明值，想将哪个图片进行透明化，就将画笔用到那张图片上
        paint.setAlpha(600);
        Matrix m2 = new Matrix();
        m2.setTranslate(500,50);
        canvas.setMatrix(m2);
        canvas.drawBitmap(firstBitmap,0,0, paint);
        canvas.drawBitmap(secondBitmap, m, paint);

        return bitmap;
    }


}
