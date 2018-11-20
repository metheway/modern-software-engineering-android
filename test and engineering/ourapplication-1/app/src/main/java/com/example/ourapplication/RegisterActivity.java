package com.example.ourapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    protected static final int ERROR = 1;
    protected static final int SUCCESS = 2;
    private EditText et_qq;
    private EditText et_pwd;
    private EditText et_apwd;
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case SUCCESS:
                    Toast.makeText(RegisterActivity.this,(String)msg.obj, Toast.LENGTH_LONG).show();
                    if(msg.obj.equals("注册成功")){
                        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }
                    break;
                case ERROR:
                    Toast.makeText(RegisterActivity.this,"失败", Toast.LENGTH_LONG).show();
                    break;
            }
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_qq = findViewById(R.id.et_qq);
        et_pwd=findViewById(R.id.et_pwd);
        et_apwd=findViewById(R.id.et_apwd);

    }



    public void regin(View view) {

        final String qq=et_qq.getText().toString().trim();
        final String pwd=et_pwd.getText().toString().trim();
        final String apwd=et_apwd.getText().toString().trim();
        if(TextUtils.isEmpty(qq)){
            Toast.makeText(this,"用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(pwd)){
            Toast.makeText(this,"密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!TextUtils.equals(pwd, apwd)){
            Toast.makeText(this,"两次输入密码不同", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pwd.length()<6){
            Toast.makeText(this,"密码位数小于6安全等级太低", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(){
            public void run(){
                try{
//                    String path="http://10.0.2.2:8080/infomanage/RegisterAction?username="+qq+"&pswd="+pwd;
                    String path="http://www.vayhee.cn/infomanage/RegisterAction?username="+qq+"&pswd="+pwd;
                    URL url=new URL(path);
                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
//                    conn.setRequestProperty("User-Agent","Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; KB974487)");
                    int code=conn.getResponseCode();
                    System.out.print(code);
                    if(code==200){
                        InputStream is=conn.getInputStream();
                        String result=StreamTools.readInputStream(is);
                        Message msg=Message.obtain();
                        msg.what=SUCCESS;
                        msg.obj=result;
                        handler.sendMessage(msg);
                    }else{
                        Message msg=Message.obtain();
                        msg.what=ERROR;
                        handler.sendMessage(msg);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Message msg=Message.obtain();
                    msg.what=ERROR;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }
}
