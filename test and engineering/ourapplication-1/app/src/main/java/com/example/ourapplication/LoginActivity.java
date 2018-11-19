package com.example.ourapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {
    protected static final int ERROR = 1;
    protected static final int SUCCESS = 2;
    BufferedReader bufferReader;
    private EditText account;
    private EditText password;
    private Button register;
    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case SUCCESS:
                    Toast.makeText(LoginActivity.this,(String)msg.obj,Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    Toast.makeText(LoginActivity.this,"发送失败", Toast.LENGTH_LONG).show();
                    break;
            }
        };
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        account = (EditText)findViewById(R.id.account);
        password=(EditText)findViewById(R.id.password);
        register = (Button)findViewById(R.id.register);
    }

    public void regist(View view) {
        Intent intent=new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void login(View view){
        final String qq=account.getText().toString().trim();
        final String pwd=password.getText().toString().trim();
        if(TextUtils.isEmpty(qq)){
            Toast.makeText(this,"用户名为空登录失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(pwd)){
            Toast.makeText(this,"密码为空登陆失败", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(){

            //            Map<String, Object> listItem = new HashMap<String, Object>();
//            List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
            public void run(){
                Looper.prepare();
                try{
                    String path="http://www.vayhee.cn/infomanage/LoginAction?username="+qq+"&pswd="+pwd;
//                    String path="http://10.0.2.2:8080/infomanage/LoginAction?username="+qq+"&pswd="+pwd;
                    URL url=new URL(path);
                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
//                    conn.setRequestProperty("User-Agent","Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; KB974487)");
                    int code=conn.getResponseCode();
                    if(code==200){
                        InputStream is=conn.getInputStream();
                        String result=StreamTools.readInputStream(is);
                        if(!result.equals("用户名不或密码错误，登陆失败")){
                            JSONObject demoJson = new JSONObject(result);
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            //存储登陆的用户名信息
                            intent.putExtra("用户名",demoJson.getString("用户名"));
                            //System.out.println(demoJson.getString("用户名"));

                            //bundle.putSerializable("hh", (Serializable) msg.obj);
                            //intent.putExtras(bundle);
                            Toast.makeText(LoginActivity.this,"登陆成功", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        }else{
                            Message msg=Message.obtain();
                            msg.what=SUCCESS;
                            msg.obj=result;
                            handler.sendMessage(msg);
                        }
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
            };
        }.start();

    }

}

