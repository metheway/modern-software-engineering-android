package com.example.lenovo.cameraapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    public static final int Take_photo =1;
    private ImageView picture;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button transform = (Button) findViewById(R.id.transform);
        picture = (ImageView) findViewById(R.id.picture);


        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = new Date().getTime()+".jpg";
                File outputImage = new File(getExternalCacheDir(),fileName);
                //String path = Environment.getExternalStorageDirectory()+File.separator+ "image";
                //File finalImage = new File(path, fileName);
                //if(!finalImage.getParentFile().exists()){
                    //finalImage.getParentFile().mkdirs();
                //}
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }  catch(IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT >=24){
                    imageUri = FileProvider.getUriForFile(Activity_camera.this,"com.example.lenovo.cameraapplication.fileprovider",outputImage);
                }
                else{
                    imageUri = Uri.fromFile(outputImage);
                }
                requestPermission();
            }

        });

        transform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent transformer = new Intent(Activity_camera.this,Activity_Transform.class);
                startActivity(transformer);
                finish();
            }
        });

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //@Override
    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.CAMERA},1);
        }else{
            opencamera();
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    opencamera();
                } else {
                    Toast.makeText(this, "权限被拒绝了", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
private void opencamera(){
        Intent intent= new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, Take_photo);
    }
   protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case Take_photo:
                if(resultCode == RESULT_OK){
                    try{
                        Toast.makeText(MyApplication.getContext(),"保存成功",Toast.LENGTH_SHORT).show();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
